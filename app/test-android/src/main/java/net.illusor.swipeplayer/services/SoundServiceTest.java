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
