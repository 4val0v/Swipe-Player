package net.illusor.swipeplayer.services;

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

public class SoundService extends Service
{
    public static final String INTENT_CODE_STOP = "stop";

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
        if (intent != null && intent.getAction() == INTENT_CODE_STOP)
        {
            if (this.mediaPlayer != null)
                this.stop();
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
        if (this.mediaPlayer != null)
            this.stop();

        int gain = this.audioManager.requestAudioFocus(this.audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (gain != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            return;

        this.startService(new Intent(this, SoundService.class));
        this.registerReceiver(this.noisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
        this.startForeground(753951, this.notificationHelper.getAudioNotification(file));

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
                    stop();
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

    private void pause()
    {
        this.mediaPlayer.pause();
        this.mediaPlayerPauseTime = this.mediaPlayer.getCurrentPosition();
    }

    private void resume()
    {
        this.resume(this.mediaPlayerPauseTime);
    }

    private void resume(int milliseconds)
    {
        this.mediaPlayer.seekTo(milliseconds);
        this.mediaPlayer.start();
    }

    private void stop()
    {
        this.unregisterReceiver(this.noisyReceiver);
        this.mediaPlayer.release();
        this.mediaPlayer = null;
        this.audioManager.abandonAudioFocus(this.audioFocusChangeListener);
        this.stopForeground(false);
        //this.stopSelf();
    }

    private void setVolume(float volume)
    {
        this.mediaPlayer.setVolume(volume, volume);
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

        public void seek(int milliseconds)
        {
            this.soundService.resume(milliseconds);
        }
    }
}
