package net.illusor.swipeplayer.services;

import android.media.AudioManager;
import android.media.MediaPlayer;
import net.illusor.swipeplayer.domain.AudioFile;

import java.io.IOException;

class AudioPlayer
{
    private MediaPlayer mediaPlayer;
    private AudioFile audioFile;
    private AudioPlayerPlaylist audioPlaylist;
    private boolean wasPlayingWhenRewindStarted;
    private int pausedPosition;
    private float volume = 1;

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
                        audioPlaylist.onPlaybackComplete();
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

    public void stop()
    {
        if (this.mediaPlayer.isPlaying())
            this.mediaPlayer.stop();

        this.mediaPlayer.release();
        this.mediaPlayer = null;
    }

    public void pause()
    {
        this.pausedPosition = this.mediaPlayer.getCurrentPosition();
        this.mediaPlayer.pause();
    }

    public void resume()
    {
        this.mediaPlayer.start();
    }

    public void startRewind()
    {
        this.wasPlayingWhenRewindStarted = this.mediaPlayer.isPlaying();
    }

    public void finishRewind(int milliseconds)
    {
        this.mediaPlayer.seekTo(milliseconds);
        if (this.wasPlayingWhenRewindStarted)
            this.mediaPlayer.start();
    }

    public void setVolume(float volume)
    {
        this.volume = volume;
        if (this.mediaPlayer != null)
            this.mediaPlayer.setVolume(volume, volume);
    }

    public AudioFile getAudioFile()
    {
        return audioFile;
    }

    public AudioPlayerState getState()
    {
        if (mediaPlayer == null)
            return AudioPlayerState.Stopped;
        else if (mediaPlayer.isPlaying())
            return AudioPlayerState.Playing;
        else
            return AudioPlayerState.Paused;
    }

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

    public int getDuration()
    {
        return this.audioFile == null ? 0 : (int)this.audioFile.getDuration();
    }

    public AudioPlayerPlaylist getPlaylist()
    {
        return this.audioPlaylist;
    }

    public void setPlaylist(AudioPlayerPlaylist playlist)
    {
        this.audioPlaylist = playlist;
    }
}
