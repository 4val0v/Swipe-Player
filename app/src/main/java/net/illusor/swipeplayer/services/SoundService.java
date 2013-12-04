package net.illusor.swipeplayer.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.*;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.helpers.NotificationHelper;

import java.io.IOException;
import java.util.List;

public class SoundService extends Service
{
    public static final String INTENT_CODE_STOP = "net.illusor.swipeplayer.services.SoundService.STOP";
    public static final String INTENT_CODE_PAUSE = "net.illusor.swipeplayer.services.SoundService.PAUSE";
    public static final String INTENT_CODE_RESUME = "net.illusor.swipeplayer.services.SoundService.PLAY";
    public static final String INTENT_CODE_NEXT = "net.illusor.swipeplayer.services.SoundService.NEXT";
    public static final String INTENT_CODE_PREVIOUS = "net.illusor.swipeplayer.services.SoundService.PREVIOUS";

    private static final int NOTIFICATION_CODE_STATUS = 753951;
    private static final int NOTIFICATION_CODE_ERROR = 753950;

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
                case INTENT_CODE_NEXT:
                    this.playNext();
                    break;
                case INTENT_CODE_PREVIOUS:
                    this.playPrevious();
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
            //this.audioManager.registerMediaButtonEventReceiver(new ComponentName(this, MediaButtonReceiver.class));
            this.serviceStarted = true;
        }

        try
        {
            this.audioPlayer.play(audioFile);
            this.audioBroadcastHandler.sendPlayAudioFile(audioFile);

            Notification notification = this.notificationHelper.getPlayingNotification(audioFile);
            this.startForeground(NOTIFICATION_CODE_STATUS, notification);
        }
        catch (IOException e)
        {
            NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            service.notify(NOTIFICATION_CODE_ERROR, notificationHelper.getErrorNotification(audioFile));
        }
    }

    void stop()
    {
        this.audioPlayer.stop();
        this.audioBroadcastHandler.sendPlaybackStop();

        if (this.serviceStarted)
        {
            this.unregisterReceiver(this.noisyReceiver);
            this.audioManager.abandonAudioFocus(this.audioFocusChangeListener);
            //this.audioManager.unregisterMediaButtonEventReceiver(new ComponentName(this, MediaButtonReceiver.class));
            this.serviceStarted = false;
        }

        this.stopForeground(true);
        this.stopSelf();

        NotificationManager service = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        service.notify(NOTIFICATION_CODE_STATUS, this.notificationHelper.getStoppedNotification());
    }

    private void playNext()
    {
        try
        {
            if (this.serviceStarted)
                this.audioPlayer.playNext();
        }
        catch (IOException e)
        {
            this.showErrorNotification(this.audioPlayer.getAudioFile());
        }
    }

    private void playPrevious()
    {
        try
        {
            if (this.serviceStarted)
                this.audioPlayer.playPrevious();
        }
        catch (IOException e)
        {
            this.showErrorNotification(this.audioPlayer.getAudioFile());
        }
    }

    private void pause()
    {
        this.audioPlayer.pause();
        this.audioBroadcastHandler.sendPlaybackPause();
        Notification notification = this.notificationHelper.getPausedNotification(this.audioPlayer.getAudioFile());
        this.startForeground(NOTIFICATION_CODE_STATUS, notification);
    }

    private void resume()
    {
        this.audioPlayer.resume();
        this.audioBroadcastHandler.sendPlaybackResume();
        Notification notification = this.notificationHelper.getPlayingNotification(this.audioPlayer.getAudioFile());
        this.startForeground(NOTIFICATION_CODE_STATUS, notification);
    }

    private void showErrorNotification(AudioFile audioFile)
    {
        NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        service.notify(NOTIFICATION_CODE_ERROR, notificationHelper.getErrorNotification(audioFile));
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

        public void playNext()
        {
            this.soundService.playNext();
        }

        public void playPrevious()
        {
            this.soundService.playPrevious();
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
            AudioPlayerPlaylist behavior = new AudioPlayerSequentialPlaylist(playlist, this.soundService.audioPlayer, this.soundService);
            this.soundService.audioPlayer.setPlaylist(behavior);
        }
   }
}
