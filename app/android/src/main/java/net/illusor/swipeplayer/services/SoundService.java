/*Copyright 2013 Nikita Kobzev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

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

/**
 * Service, performing audio playback
 */
public class SoundService extends Service
{
    //codes for system notification intent codes
    public static final String INTENT_CODE_STOP = "net.illusor.swipeplayer.services.SoundService.STOP";
    public static final String INTENT_CODE_PAUSE = "net.illusor.swipeplayer.services.SoundService.PAUSE";
    public static final String INTENT_CODE_RESUME = "net.illusor.swipeplayer.services.SoundService.PLAY";
    public static final String INTENT_CODE_NEXT = "net.illusor.swipeplayer.services.SoundService.NEXT";
    public static final String INTENT_CODE_PREVIOUS = "net.illusor.swipeplayer.services.SoundService.PREVIOUS";

    //system notification codes
    private static final int NOTIFICATION_CODE_STATUS = 753951;
    private static final int NOTIFICATION_CODE_ERROR = 753950;

    private final AudioPlayer audioPlayer = new AudioPlayer();
    private final NoisyReceiver noisyReceiver = new NoisyReceiver();
    private final NotificationHelper notificationHelper = new NotificationHelper(this);
    private final AudioFocusChangeListener audioFocusChangeListener = new AudioFocusChangeListener();
    private AudioBroadcastHandler audioBroadcastHandler = new AudioBroadcastHandler();
    private AudioManager audioManager;
    private boolean serviceStarted;

    @Override
    public void onCreate()
    {
        super.onCreate();
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

    /**
     * Starts the audio file playback
     * @param audioFile Audio file to play
     */
    void play(AudioFile audioFile)
    {
        int gain = this.audioManager.requestAudioFocus(this.audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (gain != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            return;

        if (!this.serviceStarted)
        {
            this.startService(new Intent(this, SoundService.class));
            this.registerReceiver(this.noisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));
            this.audioManager.registerMediaButtonEventReceiver(new ComponentName(this, MediaButtonReceiver.class));
            this.serviceStarted = true;
        }

        this.startPlaybackThread(audioFile);
    }

    /**
     * Stops playback of the audio file
     */
    void stop()
    {
        this.audioPlayer.stop();
        this.audioBroadcastHandler.sendPlaybackStop();

        if (this.serviceStarted)
        {
            this.unregisterReceiver(this.noisyReceiver);
            this.audioManager.abandonAudioFocus(this.audioFocusChangeListener);
            this.audioManager.unregisterMediaButtonEventReceiver(new ComponentName(this, MediaButtonReceiver.class));
            this.serviceStarted = false;
        }

        this.stopForeground(true);
        this.stopSelf();

        NotificationManager service = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        service.notify(NOTIFICATION_CODE_STATUS, this.notificationHelper.getStoppedNotification());
    }

    /**
     * Starts actual audio playback
     * @param audioFile Audio file to play
     */
    private void startPlaybackThread(final AudioFile audioFile)
    {
        //we start playback on the separate thread because of AudioPlayerSequentialPlaylist implementation details
        //see AudioPlayerSequentialPlaylist.onError() impl; in case of playback errors AudioPlayerSequentialPlaylist calls
        //SoundService().play() again and again, causing possible StackOverflow if the playlist contains a lot of corrupted files in a row
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    audioPlayer.play(audioFile);
                    audioBroadcastHandler.sendPlayAudioFile(audioFile);

                    Notification notification = notificationHelper.getPlayingNotification(audioFile);
                    startForeground(NOTIFICATION_CODE_STATUS, notification);
                }
                catch (IOException e)
                {
                    NotificationManager service = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    service.notify(NOTIFICATION_CODE_ERROR, notificationHelper.getErrorNotification(audioFile));
                }
            }
        }).start();
    }

    /**
     * Starts playback of the next file into the playlist
     */
    private void playNext()
    {
        if (this.audioPlayer.getState() != AudioPlayerState.Stopped)
        {
            AudioFile audioFile = this.audioPlayer.getPlaylist().getNext();
            if (audioFile != null)
                this.play(audioFile);
        }
    }

    /**
     * Starts playback of the previous file into the playlist
     */
    private void playPrevious()
    {
        if (this.audioPlayer.getState() != AudioPlayerState.Stopped)
        {
            AudioFile audioFile = this.audioPlayer.getPlaylist().getPrevious();
            if (audioFile != null)
                this.play(audioFile);
        }
    }

    /**
     * Pauses the playback
     */
    private void pause()
    {
        if (this.audioPlayer.getState() == AudioPlayerState.Playing)
        {
            this.audioPlayer.pause();
            this.audioBroadcastHandler.sendPlaybackPause();
            Notification notification = this.notificationHelper.getPausedNotification(this.audioPlayer.getAudioFile());
            this.startForeground(NOTIFICATION_CODE_STATUS, notification);
        }
    }

    /**
     * Resumes the playback
     */
    private void resume()
    {
        if (this.audioPlayer.getState() == AudioPlayerState.Paused)
        {
            this.audioPlayer.resume();
            this.audioBroadcastHandler.sendPlaybackResume();
            Notification notification = this.notificationHelper.getPlayingNotification(this.audioPlayer.getAudioFile());
            this.startForeground(NOTIFICATION_CODE_STATUS, notification);
        }
    }

    /**
     * Handles changes of audio focus
     */
    private class AudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener
    {
        private int lostFocusReason;//why we lost focus last time?
        private AudioPlayerState audioPlayerState;//which state did we have when we lost focus

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
                            audioPlayer.setVolume(1.0f);
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

    /**
     * {@link SoundService} communication interface
     */
    public class SoundServiceBinder extends Binder
    {
        private SoundService soundService;

        private SoundServiceBinder(SoundService soundService)
        {
            this.soundService = soundService;
        }

        /**
        * Starts the audio file playback
        * @param audioFile Audio file to play
        */
        public void play(AudioFile audioFile)
        {
            this.soundService.play(audioFile);
        }

        /**
         * Pauses the playback
         */
        public void pause()
        {
            this.soundService.pause();
        }

        /**
         * Resumes the playback
         */
        public void resume()
        {
            this.soundService.resume();
        }

        /**
         * Stops playback of the audio file
         */
        public void stop()
        {
            this.soundService.stop();
        }

        /**
         * Starts audio file rewind
         */
        public void startRewind()
        {
            this.soundService.audioPlayer.startRewind();
        }

        /**
         * Commits audio file rewind
         * @param milliseconds time where the file should be rewound to
         */
        public void finishRewind(int milliseconds)
        {
            this.soundService.audioPlayer.finishRewind(milliseconds);
        }

        /**
         * Gets currently played audio file
         * @return Current audio file
         */
        public AudioFile getAudioFile()
        {
            return this.soundService.audioPlayer.getAudioFile();
        }

        /**
         * Gets the state of audio player
         * @return State of the player
         */
        public AudioPlayerState getState()
        {
            return this.soundService.audioPlayer.getState();
        }

        /**
         * Get overall playback progress (from 0 to getDuration())
         * @return Current playback time
         */
        public int getPosition()
        {
            return this.soundService.audioPlayer.getPosition();
        }

        /**
         * Gets current audio file duration
         * @return Current audio file duration (milliseconds)
         */
        public int getDuration()
        {
            return this.soundService.audioPlayer.getDuration();
        }

        /**
         * Sets a list of files as the {@link SoundService} playlist
         * @param playlist Playlist files
         */
        public void setPlaylist(List<AudioFile> playlist)
        {
            AudioPlayerPlaylist behavior = new AudioPlayerSequentialPlaylist(playlist, this.soundService);
            this.soundService.audioPlayer.setPlaylist(behavior);
        }
   }
}
