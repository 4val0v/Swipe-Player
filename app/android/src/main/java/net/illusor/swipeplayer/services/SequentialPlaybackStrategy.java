package net.illusor.swipeplayer.services;

import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.domain.AudioPlaylist;

import java.util.List;

public class SequentialPlaybackStrategy implements PlaybackStrategy
{
    private List<AudioFile> audioFiles;

    @Override
    public AudioFile getNext(AudioFile current)
    {
        if (this.audioFiles == null || this.audioFiles.size() == 0)
            return null;

        int playlistSize = this.audioFiles.size();
        int nextIndex = this.audioFiles.indexOf(current) + 1;
        if (nextIndex < 0 || nextIndex >= playlistSize)
            nextIndex = 0;

        //look for the next suitable file to play
        int count = 0;
        AudioFile newFile;
        do
        {
            newFile = this.audioFiles.get(nextIndex);
            count++;
            nextIndex++;
            if (nextIndex >= playlistSize)
                nextIndex = 0;
        } while ((!newFile.exists() || !newFile.isValid()) && count < playlistSize);

        if (newFile.exists() && newFile.isValid() && !newFile.equals(current))
            return newFile;
        else
            return null;
    }

    @Override
    public AudioFile getPrevious(AudioFile current)
    {
        if (this.audioFiles == null || this.audioFiles.size() == 0)
            return null;

        int playlistSize = this.audioFiles.size();
        int nextIndex = this.audioFiles.indexOf(current) - 1;
        if (nextIndex < 0 || nextIndex >= playlistSize)
            nextIndex = playlistSize - 1;

        //look for the next suitable file to play
        int count = 0;
        AudioFile newFile;
        do
        {
            newFile = this.audioFiles.get(nextIndex);
            count++;
            nextIndex--;
            if (nextIndex < 0)
                nextIndex = playlistSize - 1;
        } while ((!newFile.exists() || !newFile.isValid()) && count < playlistSize);

        if (newFile.exists() && newFile.isValid() && !newFile.equals(current))
            return newFile;
        else
            return null;
    }

    @Override
    public void setPlaylist(AudioPlaylist playlist)
    {
        this.audioFiles = playlist == null ? null : playlist.getAudioFiles();
    }
}
