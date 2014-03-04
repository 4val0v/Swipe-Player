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
