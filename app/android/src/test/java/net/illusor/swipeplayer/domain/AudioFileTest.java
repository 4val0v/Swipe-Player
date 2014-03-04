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

package net.illusor.swipeplayer.domain;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileFilter;

public class AudioFileTest extends TestCase
{
    private String randomFolder, randomFile;

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
        new AudioFile(this.randomFile, "Title", "Artist", 300);
    }

    public void testAudioFolder()
    {
        new AudioFile(this.randomFolder, false);
    }
}
