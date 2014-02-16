package net.illusor.swipeplayer.services;

import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.domain.AudioPlaylist;

public class SoundServicePlaylist
{
    private AudioPlaylist playlist;
    private PlaybackStrategy playbackStrategy = new SequentialPlaybackStrategy();

    public void Playlist(AudioPlaylist audioFiles)
    {
        this.playlist = audioFiles;
        this.playbackStrategy.setPlaylist(audioFiles);
    }

    public void setPlaybackStrategy(PlaybackStrategy playbackStrategy)
    {
        playbackStrategy.setPlaylist(this.playlist);
        this.playbackStrategy = playbackStrategy;
    }

    public AudioFile getNext(AudioFile current)
    {
        AudioFile file = this.playbackStrategy.getNext(current);
        return file;
    }

    public AudioFile getPrevious(AudioFile current)
    {
        AudioFile file = this.playbackStrategy.getPrevious(current);
        return file;
    }
}
