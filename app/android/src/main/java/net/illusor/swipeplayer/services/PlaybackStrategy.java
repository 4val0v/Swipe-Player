/*Copyright 2013 Nikita Kobzev

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

import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.domain.AudioPlaylist;

import java.util.List;

/**
 * Provides playback management for a list of audio files
 */
public interface PlaybackStrategy
{
    /**
     * Gets the next element into the playlist
     * @return Next audio file to play
     * @param current
     */
    public AudioFile getNext(AudioFile current);

    /**
     * Gets the previous element into the playlist
     * @return Previous audio file to play
     * @param current
     */
    public AudioFile getPrevious(AudioFile current);

    public void setPlaylist(AudioPlaylist playlist);
}
