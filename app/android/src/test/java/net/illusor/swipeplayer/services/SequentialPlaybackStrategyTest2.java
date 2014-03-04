/*Copyright 2014 Nikita Kobzev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package net.illusor.swipeplayer.services;

import junit.framework.TestCase;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.domain.AudioPlaylist;
import net.illusor.swipeplayer.domain.RepeatMode;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class SequentialPlaybackStrategyTest2 extends TestCase
{
    private AudioPlaylist playlist;
    private AudioFile f1, f2, f3, f4, f5;

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
        files.add(f4);
        files.add(f5);
        this.playlist = new AudioPlaylist(path, files);
    }

    public void testGetNextNoRepeat()
    {
        SequentialPlaybackStrategy strategy = new SequentialPlaybackStrategy();
        strategy.setRepeatMode(RepeatMode.None);
        strategy.setPlaylist(this.playlist);

        AudioFile f2expected = strategy.getNext(f1);
        assertEquals(f2, f2expected);

        AudioFile f4expected = strategy.getNext(f2);
        assertEquals(f4, f4expected);

        AudioFile nullExpected = strategy.getNext(f4);
        assertNull(nullExpected);
    }

    public void testGetPreviousNoRepeat()
    {
        SequentialPlaybackStrategy strategy = new SequentialPlaybackStrategy();
        strategy.setRepeatMode(RepeatMode.None);
        strategy.setPlaylist(this.playlist);

        AudioFile f4expected = strategy.getPrevious(f5);
        assertEquals(f4, f4expected);

        AudioFile f2expected = strategy.getPrevious(f4);
        assertEquals(f2, f2expected);

        AudioFile nullExpected = strategy.getPrevious(f2);
        assertNull(nullExpected);
    }

    public void testGetNextRepeat()
    {
        SequentialPlaybackStrategy strategy = new SequentialPlaybackStrategy();
        strategy.setRepeatMode(RepeatMode.Playlist);
        strategy.setPlaylist(this.playlist);

        AudioFile f2expected = strategy.getNext(f4);
        assertEquals(f2, f2expected);

        AudioFile f4expected = strategy.getNext(f2);
        assertEquals(f4, f4expected);
    }

    public void testGetPreviousRepeat()
    {
        SequentialPlaybackStrategy strategy = new SequentialPlaybackStrategy();
        strategy.setRepeatMode(RepeatMode.Playlist);
        strategy.setPlaylist(this.playlist);

        AudioFile f4expected = strategy.getPrevious(f2);
        assertEquals(f4, f4expected);

        AudioFile f2expected = strategy.getPrevious(f4);
        assertEquals(f2, f2expected);
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
        this.f4 = new AudioFile(files[3].getPath(), "Audio 4", "Artist 4", 300);
        this.f5 = new AudioFile(files[4].getPath(), "Audio 5", "Artist 5", 300);

        this.f1.setValid(false);
        this.f3.setValid(false);
        this.f5.setValid(false);
    }
}
