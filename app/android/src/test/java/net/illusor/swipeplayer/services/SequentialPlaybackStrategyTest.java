package net.illusor.swipeplayer.services;

import junit.framework.TestCase;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.domain.AudioPlaylist;
import net.illusor.swipeplayer.domain.RepeatMode;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class SequentialPlaybackStrategyTest extends TestCase
{
    private AudioPlaylist playlist;
    private AudioFile f1, f2, f3;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        this.createTempFiles();

        File path = new File("/playlist");
        List<AudioFile> files = new ArrayList<>(3);
        files.add(f1);
        files.add(f2);
        files.add(f3);
        this.playlist = new AudioPlaylist(path, files);
    }

    public void testGetNextNoRepeat()
    {
        SequentialPlaybackStrategy strategy = new SequentialPlaybackStrategy();
        strategy.setRepeatMode(RepeatMode.None);
        strategy.setPlaylist(this.playlist);

        AudioFile f2expected = strategy.getNext(f1);
        assertEquals(f2, f2expected);

        AudioFile f3expected = strategy.getNext(f2);
        assertEquals(f3, f3expected);

        AudioFile nullExpected = strategy.getNext(f3);
        assertNull(nullExpected);
    }

    public void testGetPreviousNoRepeat()
    {
        SequentialPlaybackStrategy strategy = new SequentialPlaybackStrategy();
        strategy.setRepeatMode(RepeatMode.None);
        strategy.setPlaylist(this.playlist);

        AudioFile f2expected = strategy.getPrevious(f3);
        assertEquals(f2, f2expected);

        AudioFile f1expected = strategy.getPrevious(f2);
        assertEquals(f1, f1expected);

        AudioFile nullExpected = strategy.getPrevious(f1);
        assertNull(nullExpected);
    }

    public void testGetNextRepeat()
    {
        SequentialPlaybackStrategy strategy = new SequentialPlaybackStrategy();
        strategy.setRepeatMode(RepeatMode.Playlist);
        strategy.setPlaylist(this.playlist);

        AudioFile f2expected = strategy.getNext(f1);
        assertEquals(f2, f2expected);

        AudioFile f3expected = strategy.getNext(f2);
        assertEquals(f3, f3expected);

        AudioFile f1expected = strategy.getNext(f3);
        assertEquals(f1, f1expected);
    }

    public void testGetPreviousRepeat()
    {
        SequentialPlaybackStrategy strategy = new SequentialPlaybackStrategy();
        strategy.setRepeatMode(RepeatMode.Playlist);
        strategy.setPlaylist(this.playlist);

        AudioFile f2expected = strategy.getPrevious(f3);
        assertEquals(f2, f2expected);

        AudioFile f1expected = strategy.getPrevious(f2);
        assertEquals(f1, f1expected);

        AudioFile f3expected = strategy.getPrevious(f1);
        assertEquals(f3, f3expected);
    }

    private void createTempFiles()
    {
        String userHome = System.getProperty("java.home");
        File userHomeDir = new File(userHome);
        File[] files = userHomeDir.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                return pathname.isFile();
            }
        });

        this.f1 = new AudioFile(files[0].getPath(), "Audio 1", "Artist 1", 300);
        this.f2 = new AudioFile(files[1].getPath(), "Audio 2", "Artist 2", 300);
        this.f3 = new AudioFile(files[2].getPath(), "Audio 3", "Artist 3", 300);
    }
}
