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

import android.media.AudioManager;
import android.media.MediaPlayer;
import net.illusor.swipeplayer.domain.AudioFile;

import java.io.IOException;

/**
 * Performs audio playback/stop/pause/rewind features
 */
class AudioPlayer
{
    private MediaPlayer mediaPlayer;
    private AudioFile audioFile;//current playing music file
    private PlaybackListener playbackListener;
    private boolean wasPlayingWhenRewindStarted;//if player was playing when rewind started
    private int pausedPosition;//time mark, where player was paused
    private float volume = 1;

    /**
     * Starts the music file playback
     * @param audioFile Music file to play
     * @throws IOException
     */
    public void play(AudioFile audioFile) throws IOException
    {
        try
        {
            if (this.mediaPlayer != null)
                this.stop();

            this.audioFile = audioFile;

            final AudioPlayer audioPlayer = this;
            this.mediaPlayer = new MediaPlayer();
            this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            this.mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {
                @Override
                public void onPrepared(MediaPlayer player)
                {
                    try
                    {
                        player.setVolume(volume, volume);
                        player.start();
                    }
                    catch (IllegalStateException ignore)
                    {
                        //occurres when user starts "monkey-swiping" music tracks
                    }
                }
            });
            this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer player)
                {
                    if (playbackListener != null)
                        playbackListener.onComplete(audioPlayer.audioFile);
                    else
                        stop();
                }
            });
            this.mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener()
            {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int cause, int extra)
                {
                    mediaPlayer.reset();

                    if (playbackListener != null)
                        playbackListener.onError(audioPlayer.audioFile);

                    return true;
                }
            });
            this.mediaPlayer.prepareAsync();
        }
        catch (IOException e)
        {
            if (playbackListener != null)
                playbackListener.onError(audioFile);

            throw e;
        }
        catch (IllegalStateException ignore)
        {
            //occures when user starts "monkey-swiping" music tracks
        }
        catch (NullPointerException ignore)
        {
            //occures when user starts "monkey-swiping" music tracks
        }
    }

    /**
     * Stops the playback
     */
    public void stop()
    {
        this.mediaPlayer.release();
        this.mediaPlayer = null;
        this.audioFile = null;
    }

    /**
     * Pauses the playback
     */
    public void pause()
    {
        this.pausedPosition = this.mediaPlayer.getCurrentPosition();
        this.mediaPlayer.pause();
    }

    /**
     * Resumes the playback
     */
    public void resume()
    {
        this.mediaPlayer.start();
    }

    /**
     * Starts audio file rewind
     */
    public void startRewind()
    {
        this.wasPlayingWhenRewindStarted = this.mediaPlayer.isPlaying();
    }

    /**
     * Commits audio file rewind
     * @param milliseconds time where the file should be rewound to
     */
    public void finishRewind(int milliseconds)
    {
        this.mediaPlayer.seekTo(milliseconds);
        if (this.wasPlayingWhenRewindStarted)
            this.mediaPlayer.start();
    }

    /**
     * Sets the playback volume
     * @param volume value form 0.0 to 1.0
     */
    public void setVolume(float volume)
    {
        this.volume = volume;
        if (this.mediaPlayer != null)
            this.mediaPlayer.setVolume(volume, volume);
    }

    /**
     * Gets currently played audio file
     * @return Current audio file
     */
    public AudioFile getAudioFile()
    {
        return audioFile;
    }

    /**
     * Gets the state of audio player
     * @return State of the player
     */
    public AudioPlayerState getState()
    {
        try
        {
            if (this.mediaPlayer == null)
                return AudioPlayerState.Stopped;
            else if (this.mediaPlayer.isPlaying())
                return AudioPlayerState.Playing;
            else
                return AudioPlayerState.Paused;
        }
        catch (IllegalStateException ex)
        {
            return AudioPlayerState.Paused;//means this.mediaPlayer is in "Error" state;
        }
    }

    /**
     * Get overall playback progress (from 0 to getDuration())
     * @return Current playback time
     */
    public int getPosition()
    {
        try
        {
            if (this.mediaPlayer == null)
                return 0;
            if (!this.mediaPlayer.isPlaying())
                return this.pausedPosition;
            return this.mediaPlayer.getCurrentPosition();
        }
        catch (IllegalStateException e)
        {
            return 0;
        }
    }

    /**
     * Gets current audio file duration
     * @return Current audio file duration (milliseconds)
     */
    public int getDuration()
    {
        return this.audioFile == null ? 0 : (int)this.audioFile.getDuration();
    }

    /**
     * Sets the AudioPlayer playback callback
     * @param listener callback object
     */
    public void setPlaybackListener(PlaybackListener listener)
    {
        this.playbackListener = listener;
    }

    /**
     * Callback interface of the {@link AudioPlayer}
     */
    interface PlaybackListener
    {
        /**
         * Fires when playback of an audio file has completed
         * @param audioFile Audio file completed
         */
        void onComplete(AudioFile audioFile);

        /**
         * Fires when playback of an audio file caused an error
         * @param audioFile Erroneous audio file
         */
        void onError(AudioFile audioFile);
    }
}
