package net.illusor.swipeplayer.domain;

import junit.extensions.ExceptionTestCase;

import java.io.File;
import java.io.FileFilter;

public class AudioFileExceptionTest extends ExceptionTestCase
{
    private String randomFolder, randomFile;

    public AudioFileExceptionTest()
    {
        super("AudioFileExceptionTest", IllegalStateException.class);
    }

    public void setUp() throws Exception
    {
        super.setUp();

        String javaHome = System.getProperty("java.home");
        File javaHomeDir = new File(javaHome);
        File[] files = javaHomeDir.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File file)
            {
                return file.isFile();
            }
        });
        File[] folders = javaHomeDir.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File file)
            {
                return file.isDirectory();
            }
        });

        this.randomFile = files[0].getCanonicalPath();
        this.randomFolder = folders[0].getCanonicalPath();
    }

    public void testAudioFile()
    {
        new AudioFile(this.randomFolder, "Title", "Artist", 300);
    }

    public void testAudioFolder()
    {
        new AudioFile(this.randomFile, false);
    }
}
