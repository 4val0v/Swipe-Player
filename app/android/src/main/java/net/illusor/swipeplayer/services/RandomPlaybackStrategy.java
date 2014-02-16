package net.illusor.swipeplayer.services;

import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.domain.AudioPlaylist;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class RandomPlaybackStrategy implements PlaybackStrategy
{
    private static final int historySize = 50;

    private AudioPlaylist playlist;
    private List<String> playedFiles = new ArrayList<>();

    @Override
    public AudioFile getNext(AudioFile current)
    {
        int count = this.playlist.getAudioFiles().size();
        int index = (int)(Math.random() * count);
        AudioFile audioFile = this.playlist.getAudioFiles().get(index);

        if (this.playedFiles.size() >= historySize)
            this.playedFiles.remove(0);

        this.playedFiles.add(audioFile.getAbsolutePath());

        return audioFile;
    }

    @Override
    public AudioFile getPrevious(AudioFile current)
    {
        int index = playedFiles.indexOf(current.getAbsolutePath());
        if (index <= 0) return null;

        String previous = this.playedFiles.get(index - 1);
        AudioFile audioFile = this.getAudioFile(previous);

        return audioFile;
    }

    @Override
    public void setPlaylist(AudioPlaylist playlist)
    {
        if (this.playlist != null && this.playlist.getPath() != playlist.getPath())
            this.playedFiles.clear();

        this.playlist = playlist;
    }

    private AudioFile getAudioFile(String path)
    {
        for (AudioFile audioFile : this.playlist.getAudioFiles())
        {
            if (audioFile.getAbsolutePath().equals(path))
                return audioFile;
        }
        return null;
    }
}
