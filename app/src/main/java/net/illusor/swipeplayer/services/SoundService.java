package net.illusor.swipeplayer.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.helpers.NotificationHelper;

import java.io.IOException;
import java.util.List;

public class SoundService extends Service
{
    public static final String INTENT_CODE_STOP = "net.illusor.swipeplayer.services.SoundService.STOP";
    public static final String INTENT_CODE_PAUSE = "net.illusor.swipeplayer.services.SoundService.PAUSE";
    public static final String INTENT_CODE_RESUME = "net.illusor.swipeplayer.services.SoundService.PLAY";

    private static final int NOTIFICATION_CODE = 753951;

    private List<AudioFile> playlist;
    private AudioFile currentPlaying;

    private AudioManager audioManager;
    private MediaPlayer mediaPlayer;
    private NoisyReceiver noisyReceiver;
    private NotificationHelper notificationHelper;
    private AudioFocusChangeListener audioFocusChangeListener;
    private int mediaPlayerPauseTime;

    @Override
    public void onCreate()
    {
        super.onCreate();
        this.audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        this.audioFocusChangeListener = new AudioFocusChangeListener();
        this.noisyReceiver = new NoisyReceiver();
        this.notificationHelper = new NotificationHelper(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (intent != null && intent.getAction() != null)
        {
            switch (intent.getAction())
            {
                case INTENT_CODE_STOP:
                {
                    this.stop();
                    break;
                }
                case INTENT_CODE_PAUSE:
                {
                    this.pause();
                    break;
                }
                case INTENT_CODE_RESUME:
                {
                    this.resume();
                    break;
                }
            }
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return new SoundServiceBinder(this);
    }

    private void play(AudioFile file)
    {
        this.stop();

        int gain = this.audioManager.requestAudioFocus(this.audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (gain != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            return;

        this.currentPlaying = file;

        this.startService(new Intent(this, SoundService.class));
        this.registerReceiver(this.noisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        this.startForeground(NOTIFICATION_CODE, this.notificationHelper.getPlayingNotification(file));

        try
        {
            this.mediaPlayer = new MediaPlayer();
            this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            this.mediaPlayer.setDataSource(file.getAbsolutePath());
            this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {
                @Override
                public void onPrepared(MediaPlayer player)
                {
                    player.start();
                }
            });
            this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer player)
                {
                    playCompleted();
                }
            });
            this.mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener()
            {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i2)
                {
                    return false;
                }
            });
            this.mediaPlayer.prepareAsync();
        }
        catch (IOException e)
        {
            stop();
            Log.e(this.getClass().getName(), e.getMessage());
        }
    }

    private void pauseSilent()
    {
        if (this.mediaPlayer != null)
        {
            this.mediaPlayer.pause();
            this.mediaPlayerPauseTime = this.mediaPlayer.getCurrentPosition();
        }
    }

    private void pause()
    {
        if (this.mediaPlayer != null)
        {
            this.pauseSilent();
            this.startForeground(NOTIFICATION_CODE, this.notificationHelper.getPausedNotification(this.currentPlaying));
        }
    }

    private void resume()
    {
        this.resume(this.mediaPlayerPauseTime);
        this.startForeground(NOTIFICATION_CODE, this.notificationHelper.getPlayingNotification(this.currentPlaying));
    }

    private void resume(int milliseconds)
    {
        if (this.mediaPlayer != null)
        {
            this.mediaPlayer.seekTo(milliseconds);
            this.mediaPlayer.start();
        }
    }

    private void stop()
    {
        if (this.mediaPlayer != null)
        {
            this.unregisterReceiver(this.noisyReceiver);
            this.mediaPlayer.release();
            this.mediaPlayer = null;
            this.audioManager.abandonAudioFocus(this.audioFocusChangeListener);

            this.stopForeground(true);
            this.stopSelf();

            NotificationManager service = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            service.notify(NOTIFICATION_CODE, this.notificationHelper.getStoppedNotification());
        }
    }

    private void setVolume(float volume)
    {
        this.mediaPlayer.setVolume(volume, volume);
    }

    private void playCompleted()
    {
        this.stop();

        if (this.playlist == null || this.playlist.size() == 0)
            return;

        int playlistSize = this.playlist.size();
        int nextIndex = this.playlist.indexOf(this.currentPlaying) + 1;
        if (nextIndex < 0 || nextIndex >= playlistSize)
            nextIndex = 0;

        int count = 0;
        AudioFile newFile;
        do
        {
            newFile = this.playlist.get(nextIndex);
            count++;
            nextIndex++;
            if (nextIndex >= playlistSize)
                nextIndex = 0;
        } while (!newFile.exists() && count < playlistSize);

        if (newFile.exists())
            this.play(newFile);
        else
            this.stop();
    }

    private class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener
    {
        private int lostFocusReason;

        @Override
        public void onAudioFocusChange(int i)
        {
            switch (i)
            {
                case AudioManager.AUDIOFOCUS_LOSS:
                {
                    this.lostFocusReason = i;
                    stop();
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                {
                    this.lostFocusReason = i;
                    pause();
                    break;
                }
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                {
                    this.lostFocusReason = i;
                    setVolume(0.2f);
                    break;
                }
                case AudioManager.AUDIOFOCUS_GAIN:
                {
                    switch (this.lostFocusReason)
                    {
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                            resume();
                            break;
                        case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                            setVolume(1);
                            break;
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

        public void startSeek()
        {
            this.soundService.pauseSilent();
        }

        public void endSeek(int milliseconds)
        {
            this.soundService.resume(milliseconds);
        }

        public void setPlaylist(List<AudioFile> playlist)
        {
            this.soundService.playlist = playlist;
        }

        public SoundServiceState getServiceState()
        {
            if (mediaPlayer == null)
                return SoundServiceState.Stopped;
            else if (mediaPlayer.isPlaying())
                return SoundServiceState.Playing;
            else
                return SoundServiceState.Paused;
        }
   }

    public enum SoundServiceState
    {
        Playing,
        Paused,
        Stopped
    }
}
