package net.illusor.swipeplayer.domain;

import java.io.File;
import java.util.List;

public class AudioPlaylist
{
    private final File path;
    private final List<AudioFile> audioFiles;

    public AudioPlaylist(File path, List<AudioFile> audioFiles)
    {
        this.path = path;
        this.audioFiles = audioFiles;
    }

    public File getPath()
    {
        return path;
    }

    public List<AudioFile> getAudioFiles()
    {
        return audioFiles;
    }
}
