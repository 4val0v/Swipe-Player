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

import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.domain.AudioPlaylist;
import net.illusor.swipeplayer.domain.RepeatMode;

import java.util.List;

/**
 * Provides "Sequential" peek logic for previous/next peek of audio files
 */
public class SequentialPlaybackStrategy implements PlaybackStrategy
{
    private AudioPlaylist playlist;
    private RepeatMode repeatMode = RepeatMode.None;

    @Override
    public AudioFile getNext(AudioFile current)
    {
        if (this.playlist == null)
            return null;

        List<AudioFile> audioFiles = this.playlist.getAudioFiles();
        if (audioFiles.size() == 0)
            return null;

        int playlistSize = audioFiles.size();
        int nextIndex = audioFiles.indexOf(current);

        //look for the next suitable file to play
        int count = 0;
        AudioFile newFile;
        do
        {
            count++;
            nextIndex++;

            if (nextIndex == playlistSize)
            {
                if (this.repeatMode == RepeatMode.Playlist)
                    nextIndex = 0;
                else
                    return null;
            }

            newFile = audioFiles.get(nextIndex);

        } while ((!newFile.exists() || !newFile.isValid()) && count < playlistSize);

        if (newFile.exists() && newFile.isValid() && !newFile.equals(current))
            return newFile;
        else
            return null;
    }

    @Override
    public AudioFile getPrevious(AudioFile current)
    {
        if (this.playlist == null)
            return null;

        List<AudioFile> audioFiles = this.playlist.getAudioFiles();
        if (audioFiles.size() == 0)
            return null;

        int playlistSize = audioFiles.size();
        int prevIndex = audioFiles.indexOf(current);

        //look for the suitable previous file to play
        int count = 0;
        AudioFile newFile;
        do
        {
            count++;
            prevIndex--;

            if (prevIndex < 0)
            {
                if (this.repeatMode == RepeatMode.Playlist)
                    prevIndex = playlistSize - 1;
                else
                    return null;
            }

            newFile = audioFiles.get(prevIndex);

        } while ((!newFile.exists() || !newFile.isValid()) && count < playlistSize);

        if (newFile.exists() && newFile.isValid() && !newFile.equals(current))
            return newFile;
        else
            return null;
    }

    @Override
    public void setPlaylist(AudioPlaylist playlist)
    {
        if (playlist != null && playlist.getAudioFiles() != null)
            this.playlist = playlist;
        else
            this.playlist = null;
    }

    @Override
    public void setRepeatMode(RepeatMode mode)
    {
        this.repeatMode = mode;
    }
}
