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
    private AudioPlayerPlaylist audioPlaylist;//current playlist instance
    private boolean wasPlayingWhenRewindStarted;//if player was playing when rewind started
    private int pausedPosition;//time mark, where player was pauser
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
            this.audioFile = audioFile;

            if (this.mediaPlayer != null)
                this.stop();

            final AudioPlayer audioPlayer = this;
            this.mediaPlayer = new MediaPlayer();
            this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            this.mediaPlayer.setDataSource(audioFile.getAbsolutePath());
            this.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
            {
                @Override
                public void onPrepared(MediaPlayer player)
                {
                    player.setVolume(volume, volume);
                    player.start();
                }
            });
            this.mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer player)
                {
                    if (audioPlaylist != null)
                        audioPlaylist.onPlaybackComplete(audioPlayer.audioFile);
                    else
                        stop();
                }
            });
            this.mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener()
            {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int cause, int extra)
                {
                    if (audioPlaylist != null)
                        audioPlaylist.onError(audioPlayer.audioFile);

                    mediaPlayer.release();
                    return true;
                }
            });
            this.mediaPlayer.prepareAsync();
        }
        catch (IOException e)
        {
            if (audioPlaylist != null)
                audioPlaylist.onError(audioFile);

            throw e;
        }
    }

    /**
     * Stops the playback
     */
    public void stop()
    {
        if (this.mediaPlayer.isPlaying())
            this.mediaPlayer.stop();

        this.mediaPlayer.release();
        this.mediaPlayer = null;
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
        if (mediaPlayer == null)
            return AudioPlayerState.Stopped;
        else if (mediaPlayer.isPlaying())
            return AudioPlayerState.Playing;
        else
            return AudioPlayerState.Paused;
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
     * Get playlist assigned to the player
     * @return Playlist instance
     */
    public AudioPlayerPlaylist getPlaylist()
    {
        return this.audioPlaylist;
    }

    /**
     * Assigns a playlist to the player
     * @param playlist playlist instance
     */
    public void setPlaylist(AudioPlayerPlaylist playlist)
    {
        this.audioPlaylist = playlist;
    }
}
