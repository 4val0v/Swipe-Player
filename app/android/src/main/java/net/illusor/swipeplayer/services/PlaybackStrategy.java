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
 * Provides control over the playlist and "peek logic" for previous/next track playback
 */
public interface PlaybackStrategy
{
    /**
     * Gets the next audio file for playback
     * @param current Currently played audio file
     * @return Next audio file in the queue
     */
    public AudioFile getNext(AudioFile current);

    /**
     * Gets the previous audio file for playback
     * @param current Currently played audio file
     * @return Previous audio file in the queue
     */
    public AudioFile getPrevious(AudioFile current);

    /**
     * Sets the playlist to use as the playback queue
     * @param playlist Playback audio files
     */
    public void setPlaylist(AudioPlaylist playlist);
}
