package net.illusor.swipeplayer.domain;

import javax.xml.datatype.Duration;
import java.io.File;

public class AudioFile extends File
{
    private String title, artist;
    private Duration duration;

    public AudioFile(String path)
    {
        super(path);

        if (this.isFile())
            throw new IllegalStateException("This constructor is for audio directories. Do not use it to declare files");
    }

    public AudioFile(String path, String title, String artist, Duration duration)
    {
        super(path);

        if (this.isDirectory())
            throw new IllegalStateException("This constructor is for audio files. Do not use it to declare directories");

        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    public String getTitle()
    {
        return title;
    }

    public String getArtist()
    {
        return artist;
    }

    public Duration getDuration()
    {
        return duration;
    }
}
