package net.illusor.swipeplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.helpers.NotificationHelper;

import java.util.List;

public class SoundService extends Service
{
    public static final String INTENT_CODE_STOP = "net.illusor.swipeplayer.services.SoundService.STOP";
    public static final String INTENT_CODE_PAUSE = "net.illusor.swipeplayer.services.SoundService.PAUSE";
    public static final String INTENT_CODE_RESUME = "net.illusor.swipeplayer.services.SoundService.PLAY";

    private static final int NOTIFICATION_CODE = 753951;

    private final AudioPlayer audioPlayer = new AudioPlayer();
    private final NoisyReceiver noisyReceiver = new NoisyReceiver();
    private final NotificationHelper notificationHelper = new NotificationHelper(this);
    private final AudioFocusChangeListener audioFocusChangeListener = new AudioFocusChangeListener();
    private AudioBroadcastHandler audioBroadcastHandler;
    private AudioManager audioManager;
    private boolean serviceStarted;

    @Override
    public void onCreate()
    {
        super.onCreate();
        this.audioBroadcastHandler = new AudioBroadcastHandler(this);
        this.audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
    }

    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null && intent.getAction() != null)
        {
            switch (intent.getAction())
            {
                case INTENT_CODE_STOP:
                    this.stop();
                    break;
                case INTENT_CODE_PAUSE:
                    this.pause();
                    break;
                case INTENT_CODE_RESUME:
                    this.resume();
                    break;
            }
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return new SoundServiceBinder(this);
    }

    void play(AudioFile audioFile)
    {
        int gain = this.audioManager.requestAudioFocus(this.audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (gain != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            return;

        if (!this.serviceStarted)
        {
            this.startService(new Intent(this, SoundService.class));
            this.registerReceiver(this.noisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            this.serviceStarted = true;
        }

        this.audioPlayer.play(audioFile);
        this.audioBroadcastHandler.sendPlayAudioFile(audioFile);

        Notification notification = this.notificationHelper.getPlayingNotification(audioFile);
        this.startForeground(NOTIFICATION_CODE, notification);
    }

    void stop()
    {
        this.audioPlayer.stop();
        this.audioBroadcastHandler.sendPlaybackStop();

        if (this.serviceStarted)
        {
            this.unregisterReceiver(this.noisyReceiver);
            this.audioManager.abandonAudioFocus(this.audioFocusChangeListener);
            this.serviceStarted = false;
        }

        this.stopForeground(true);
        this.stopSelf();

        NotificationManager service = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        service.notify(NOTIFICATION_CODE, this.notificationHelper.getStoppedNotification());
    }

    private void pause()
    {
        this.audioPlayer.pause();
        this.audioBroadcastHandler.sendPlaybackPause();
        Notification notification = this.notificationHelper.getPausedNotification(this.audioPlayer.getAudioFile());
        this.startForeground(NOTIFICATION_CODE, notification);
    }

    private void resume()
    {
        this.audioPlayer.resume();
        this.audioBroadcastHandler.sendPlaybackResume();
        Notification notification = this.notificationHelper.getPlayingNotification(this.audioPlayer.getAudioFile());
        this.startForeground(NOTIFICATION_CODE, notification);
    }

    private class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener
    {
        private int lostFocusReason;
        private AudioPlayerState audioPlayerState;

        @Override
        public void onAudioFocusChange(int i)
        {
            switch (i)
            {
                case AudioManager.AUDIOFOCUS_LOSS:
                {
                    this.lostFocusReason = i;
                    this.audioPlayerState = audioPlayer.getState();
                    stop();
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                {
                    this.lostFocusReason = i;
                    this.audioPlayerState = audioPlayer.getState();
                    pause();
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                {
                    this.lostFocusReason = i;
                    audioPlayer.setVolume(0.2f);
                    break;
                }
                case AudioManager.AUDIOFOCUS_GAIN:
                {
                    switch (this.lostFocusReason)
                    {
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        {
                            if (this.audioPlayerState == AudioPlayerState.Playing)
                                resume();
                            break;
                        }
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        {
                            audioPlayer.setVolume(0.2f);
                            break;
                        }
                    }
                    break;
                }
            }
        }
    }

    private class NoisyReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
                pause();
        }
    }

    public class SoundServiceBinder extends Binder
    {
        private SoundService soundService;

        public SoundServiceBinder(SoundService soundService)
        {
            this.soundService = soundService;
        }

        public void play(AudioFile file)
        {
            this.soundService.play(file);
        }

        public void pause()
        {
            this.soundService.pause();
        }

        public void resume()
        {
            this.soundService.resume();
        }

        public void startRewind()
        {
            this.soundService.audioPlayer.startRewind();
        }

        public void finishRewind(int milliseconds)
        {
            this.soundService.audioPlayer.finishRewind(milliseconds);
        }

        public AudioFile getAudioFile()
        {
            return this.soundService.audioPlayer.getAudioFile();
        }

        public AudioPlayerState getState()
        {
            return this.soundService.audioPlayer.getState();
        }

        public int getPosition()
        {
            return this.soundService.audioPlayer.getPosition();
        }

        public int getDuration()
        {
            return this.soundService.audioPlayer.getDuration();
        }

        public void setPlaylist(List<AudioFile> playlist)
        {
            AudioPlayerOnCompleteBehavior behavior = new AudioPlayerNextTrackBehavior(playlist, this.soundService);
            this.soundService.audioPlayer.setOnCompleteBehavior(behavior);
        }
   }
}
