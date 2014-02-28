package net.illusor.swipeplayer.services;

import android.content.Intent;
import android.test.ServiceTestCase;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.domain.AudioPlaylist;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class SoundServiceTest extends ServiceTestCase<SoundService>
{
    private AudioPlaylist playlist;

    public SoundServiceTest()
    {
        super(SoundService.class);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();

        String javaDir = System.getProperty("java.home");
        File javaHome = new File(javaDir);

        File[] files = javaHome.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File file)
            {
                return file.isFile();
            }
        });
        List<AudioFile> audioFiles = new ArrayList<>(files.length);
        for (int i = 0; i < files.length; i++)
        {
            AudioFile audioFile = new AudioFile(files[i].getCanonicalPath(), files[i].getName(), "", 100500);
            audioFiles.add(audioFile);
        }

        this.playlist = new AudioPlaylist(javaHome, audioFiles);
    }

    public void testServiceBindable() throws Exception
    {
        Intent intent = new Intent(this.getContext(), SoundService.class);
        SoundService.SoundServiceBinder binder = (SoundService.SoundServiceBinder)this.bindService(intent);

        assertNotNull(binder);
        assertEquals(AudioPlayerState.Stopped, binder.getState());
        assertNull(binder.getAudioFile());

        binder.setPlaylist(this.playlist);
    }
}
