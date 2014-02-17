package net.illusor.swipeplayer.domain;

import java.io.File;
import java.util.List;

/**
 * Set of audio files for playback
 */
public class AudioPlaylist
{
    private final File path;
    private final List<AudioFile> audioFiles;

    public AudioPlaylist(File path, List<AudioFile> audioFiles)
    {
        this.path = path;
        this.audioFiles = audioFiles;
    }

    /**
     * Gets the directory, where the audio files are located
     * @return audio directory
     */
    public File getPath()
    {
        return path;
    }

    /**
     * Actual audio files for playback
     * @return List of audio files
     */
    public List<AudioFile> getAudioFiles()
    {
        return audioFiles;
    }
}
