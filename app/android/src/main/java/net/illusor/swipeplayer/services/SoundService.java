/*Copyright 2014 Nikita Kobzev

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
import net.illusor.swipeplayer.domain.AudioPlaylist;
import net.illusor.swipeplayer.domain.RepeatMode;
import net.illusor.swipeplayer.helpers.NotificationHelper;
import net.illusor.swipeplayer.widget.SwipeWidgetHelper;

import java.io.IOException;

/**
 * Service, performing audio playback
 */
public class SoundService extends Service
{
    //intent codes for remote service control
    public static final String INTENT_CODE_STOP = "net.illusor.swipeplayer.services.SoundService.STOP";
    public static final String INTENT_CODE_PAUSE = "net.illusor.swipeplayer.services.SoundService.PAUSE";
    public static final String INTENT_CODE_RESUME = "net.illusor.swipeplayer.services.SoundService.PLAY";
    public static final String INTENT_CODE_NEXT = "net.illusor.swipeplayer.services.SoundService.NEXT";
    public static final String INTENT_CODE_PREVIOUS = "net.illusor.swipeplayer.services.SoundService.PREVIOUS";
    public static final String INTENT_CODE_WIDGET_UPDATE = "net.illusor.swipeplayer.services.SoundService.WIDGET_UPDATE";

    //system notification codes
    private static final int NOTIFICATION_CODE_STATUS = 753951;
    private static final int NOTIFICATION_CODE_ERROR = 753950;

    private final AudioPlayer audioPlayer = new AudioPlayer();
    private final NotificationHelper notificationHelper = new NotificationHelper(this);
    private final SwipeWidgetHelper widgetHelper = new SwipeWidgetHelper(this);
    private final PlaybackStrategy playlist = new SequentialPlaybackStrategy();
    private final AudioBroadcastHandler audioBroadcastHandler = new AudioBroadcastHandler();
    private final AudioStateTracker audioStateTracker = new AudioStateTracker(this);
    private AudioManager audioManager;
    private boolean serviceStarted;

    @Override
    public void onCreate()
    {
        super.onCreate();
        this.audioManager = (AudioManager)this.getSystemService(Context.AUDIO_SERVICE);
        this.audioPlayer.setPlaybackListener(new PlaybackListener());
    }

    @Override
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
                case INTENT_CODE_WIDGET_UPDATE:
                    this.updateWidgetState();
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
        int gain = this.audioManager.requestAudioFocus(this.audioStateTracker, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if (gain != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            return;

        this.audioStateTracker.reset();

        if (!this.serviceStarted)
        {
            this.startService(new Intent(this, SoundService.class));
            this.audioManager.registerMediaButtonEventReceiver(new ComponentName(this, MediaButtonReceiver.class));
            this.audioStateTracker.registerReceiver();
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
        this.widgetHelper.setStopped();

        if (this.serviceStarted)
        {
            this.audioStateTracker.unregisterReceiver();
            this.audioManager.abandonAudioFocus(this.audioStateTracker);
            this.audioManager.unregisterMediaButtonEventReceiver(new ComponentName(this, MediaButtonReceiver.class));
            this.serviceStarted = false;
        }

        this.stopForeground(true);
        this.stopSelf();

        NotificationManager service = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        service.notify(NOTIFICATION_CODE_STATUS, this.notificationHelper.getStoppedNotification());
    }

    /**
     * Gets currently played audio file
     * @return Current audio file
     */
    AudioFile getAudioFile()
    {
        return this.audioPlayer.getAudioFile();
    }

    /**
     * Starts actual audio playback
     * @param audioFile Audio file to play
     */
    private void startPlaybackThread(final AudioFile audioFile)
    {
        //we start playback on the separate thread because of PlaybackListener implementation details
        //see PlaybackListener.onError() impl; in case of playback errors PlaybackListener calls
        //play() again and again, causing possible StackOverflow if the playlist contains a lot of corrupted files in a row
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    audioPlayer.play(audioFile);
                    audioBroadcastHandler.sendPlayAudioFile(audioFile);
                    widgetHelper.setPlaying(audioFile);

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
            AudioFile audioFile = this.playlist.getNext(this.audioPlayer.getAudioFile());
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
            AudioFile audioFile = this.playlist.getPrevious(this.audioPlayer.getAudioFile());
            if (audioFile != null)
                this.play(audioFile);
        }
    }

    /**
     * Pauses the playback
     */
    void pause()
    {
        if (this.audioPlayer.getState() == AudioPlayerState.Playing)
        {
            this.audioPlayer.pause();
            this.audioBroadcastHandler.sendPlaybackPause();

            AudioFile audioFile = this.audioPlayer.getAudioFile();
            this.widgetHelper.setPaused(audioFile);

            Notification notification = this.notificationHelper.getPausedNotification(audioFile);
            this.startForeground(NOTIFICATION_CODE_STATUS, notification);
        }
    }

    /**
     * Resumes the playback
     */
    void resume()
    {
        if (this.audioPlayer.getState() == AudioPlayerState.Paused)
        {
            this.audioPlayer.resume();
            this.audioBroadcastHandler.sendPlaybackResume();

            AudioFile audioFile = this.audioPlayer.getAudioFile();
            this.widgetHelper.setPlaying(audioFile);

            Notification notification = this.notificationHelper.getPlayingNotification(audioFile);
            this.startForeground(NOTIFICATION_CODE_STATUS, notification);
        }
    }

    void setVolume(float volume)
    {
        this.audioPlayer.setVolume(volume);
    }

    AudioPlayerState getState()
    {
        return this.audioPlayer.getState();
    }

    /**
     * Updates state of the screen widgets
     */
    private void updateWidgetState()
    {
        switch (this.audioPlayer.getState())
        {
            case Playing:
            {
                this.widgetHelper.setPlaying(this.audioPlayer.getAudioFile());
                break;
            }
            case Paused:
            {
                this.widgetHelper.setPaused(this.audioPlayer.getAudioFile());
                break;
            }
            case Stopped:
            {
                this.widgetHelper.setStopped();
                this.stopSelf();
                break;
            }
        }
    }

    private class PlaybackListener implements AudioPlayer.PlaybackListener
    {
        @Override
        public void onComplete(AudioFile audioFile)
        {
            AudioFile file = playlist.getNext(audioFile);
            if (file != null)
                play(file);
            else
                stop();
        }

        @Override
        public void onError(AudioFile audioFile)
        {
            audioFile.setValid(false);
            this.onComplete(audioFile);
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
            return this.soundService.getAudioFile();
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
        public void setPlaylist(AudioPlaylist playlist)
        {
            this.soundService.playlist.setPlaylist(playlist);
        }

        /**
         * Sets the playback cycling mode
         * @param mode playback mode
         */
        public void setRepeatMode(RepeatMode mode)
        {
            this.soundService.playlist.setRepeatMode(mode);
        }
   }
}
