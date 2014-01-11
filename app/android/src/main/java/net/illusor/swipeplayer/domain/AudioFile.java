package net.illusor.swipeplayer.domain;

import java.io.File;

/**
 * Entity representing a music file
 */
public class AudioFile extends File
{
    private boolean isValid = true;//sometimes it occurs a music file is broken and can not be played
    private String title, artist;
    private long duration;
    private boolean hasSubDirectories;

    /**
     * Constructor used to create instances of {@link AudioFile}, which represent <b>folders</b>, containing music files
     * @param path Folder path
     * @param hasSubDirectories If folder has any subdirectories with music files into them
     */
    public AudioFile(String path, boolean hasSubDirectories)
    {
        super(path);

        if (this.isFile())
            throw new IllegalStateException("This constructor is for audio directories. Do not use it to declare files");

        this.title = this.getName();
        this.hasSubDirectories = hasSubDirectories;
    }

    /**
     * Constructor used to create instances of {@link AudioFile}, which represent <b>actual music files</b>
     * @param path Path to file
     * @param title Music file title
     * @param artist Music file artist
     * @param duration Music file duration
     */
    public AudioFile(String path, String title, String artist, long duration)
    {
        super(path);

        if (this.isDirectory())
            throw new IllegalStateException("This constructor is for audio files. Do not use it to declare directories");

        this.title = title;
        this.artist = artist;
        this.duration = duration;
    }

    /**
     * If audio directory has any subdirectories with music files into them
     * @return <b>true</b> if there are subdirectories<br><b>false</b> if not
     */
    public boolean hasSubDirectories()
    {
        return this.hasSubDirectories;
    }

    /**
     * Music file title
     * @return
     */
    public String getTitle()
    {
        return title;
    }

    /**
     * Music file artist
     * @return
     */
    public String getArtist()
    {
        return artist;
    }

    /**
     * Music file duration
     * @return
     */
    public long getDuration()
    {
        return duration;
    }

    /**
     * If current music file is considered to be broken
     * @return <b>true</b> if broken<br><b>false</b> if not
     */
    public boolean isValid()
    {
        return isValid;
    }

    /**
     * Marks current music file as broken, of not
     * @param valid status of the file
     */
    public void setValid(boolean valid)
    {
        isValid = valid;
    }
}
