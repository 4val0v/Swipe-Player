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

package net.illusor.swipeplayer.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.domain.AudioPlaylist;

import java.io.File;
import java.util.*;

/**
 * Gets the list of music folders, contained into the current directory
 */
class AudioFoldersLoader extends AsyncTaskLoader<AudioPlaylist>
{
    private final File directory;
    private AudioPlaylist result;

    /**
     * Creates a new instance of {@link AudioFoldersLoader}
     * @param context Current activity context
     * @param directory Directory to load audio folders from
     */
    public AudioFoldersLoader(Context context, File directory)
    {
        super(context);
        this.directory = directory;
    }

    @Override
    protected void onStartLoading()
    {
        //fix a bug of SupportLibrary AsyncTaskLoader implementation
        if (this.takeContentChanged() || this.result == null)
            this.forceLoad();
        else
            this.deliverResult(this.result);
    }

    @Override
    public AudioPlaylist loadInBackground()
    {
        List<AudioFile> audioFiles = new ArrayList<>();

        //scan internal storage for music files
        Cursor internal = this.executeQuery(MediaStore.Audio.Media.INTERNAL_CONTENT_URI);
        if (internal != null)
        {
            List<AudioFile> internalContent = this.getMediaObjects(internal);
            audioFiles.addAll(internalContent);
            internal.close();
        }

        //scan external storage
        Cursor external = this.executeQuery(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI);
        if (external != null)
        {
            List<AudioFile> externalContent = this.getMediaObjects(external);
            audioFiles.addAll(externalContent);
            external.close();
        }

        Comparator<AudioFile> comparator = this.getResultsSortComparator();
        Collections.sort(audioFiles, comparator);

        this.result = new AudioPlaylist(this.directory, audioFiles);
        return this.result;
    }

    /**
     * Gets a list of music folders, contained into the current directory
     * @param cursor Cursor, containing info about audio files into the directory
     * @return List of audio folders
     */
    protected List<AudioFile> getMediaObjects(Cursor cursor)
    {
        List<AudioFile> result = new LinkedList<>();
        if (cursor == null) return result;

        while (cursor.moveToNext())
        {
            AudioFile mediaObject = this.getNextLevelObject(cursor);
            if (mediaObject != null && !result.contains(mediaObject))
                result.add(mediaObject);
        }

        return result;
    }

    protected Comparator<AudioFile> getResultsSortComparator()
    {
        return new AudioFileComparator();
    }

    /**
     * Queries the content provider
     * @param uri Content uri
     * @return Content provider data
     */
    private Cursor executeQuery(Uri uri)
    {
        Cursor cursor = this.getContext().getContentResolver().query(uri,
                new String[]{ MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA },
                MediaStore.Audio.Media.DATA + " like ? and " + MediaStore.Audio.Media.IS_MUSIC + "!=0",
                new String[]{ this.directory.getAbsolutePath() + "%" }, null);

        return cursor;
    }

    /**
     * Checks, if current cursor entry is a 1st-level-child music folder of the current directory
     * @param cursor Cursor, containing info about audio files into the directory
     * @return {@link AudioFile} representing a music folder, or null
     */
    private AudioFile getNextLevelObject(Cursor cursor)
    {
        String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

        int length = this.directory.getAbsolutePath().length();
        int separatorPos = fileName.indexOf(File.separator, length + 1);

        AudioFile result = null;
        //separatorPos >= 0 means we've found a directory; we don't need to find files here
        if (separatorPos >= 0)
        {
            String directory = fileName.substring(0, separatorPos);
            boolean hasSubDirectories = fileName.indexOf(File.separator, separatorPos + 1) >= 0;
            result = new AudioFile(directory, hasSubDirectories);
        }

        return result;
    }

    private class AudioFileComparator implements Comparator<AudioFile>
    {
        @Override
        public int compare(AudioFile audioFile1, AudioFile audioFile2)
        {
            if (audioFile1.isFile() == audioFile2.isFile())
                return audioFile1.getAbsolutePath().compareTo(audioFile2.getAbsolutePath());
            else
            {
                if (audioFile1.isFile())
                    return -1;
                else
                    return 1;
            }
        }
    }
}