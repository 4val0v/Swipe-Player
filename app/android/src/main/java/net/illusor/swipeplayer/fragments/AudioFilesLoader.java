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
import android.provider.MediaStore;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;

import java.io.File;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Gets all the music files contained into the directory
 */
class AudioFilesLoader extends AudioFoldersLoader
{
    private final Comparator<AudioFile> comparator;

    /**
     * Creates a new instance of {@link AudioFilesLoader}
     * @param context Current activity context
     * @param directory Directory to load audio files from
     * @param comparator Comparator used to sort resulting files list
     */
    public AudioFilesLoader(Context context, File directory, Comparator<AudioFile> comparator)
    {
        super(context, directory);
        this.comparator = comparator;
    }

    @Override
    protected List<AudioFile> getMediaObjects(Cursor cursor)
    {
        List<AudioFile> result = new LinkedList<>();
        if (cursor == null) return result;

        while (cursor.moveToNext())
        {
            AudioFile mediaObject = this.getNextLevelObject(cursor);
            if (!result.contains(mediaObject))
                result.add(mediaObject);
        }

        return result;
    }

    @Override
    protected Comparator<AudioFile> getResultsSortComparator()
    {
        return this.comparator != null ? this.comparator : super.getResultsSortComparator();
    }

    /**
     * Reads the {@link Cursor} entry and creates an {@link AudioFile} from it
     * @param cursor Cursor to read
     * @return Resulting audio file
     */
    private AudioFile getNextLevelObject(Cursor cursor)
    {
        String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
        String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));

        if ("<unknown>".equals(artist))//when artist is unknown, it has text "<unknown>"; do not know, if android has some general resource for this string
            artist = this.getContext().getResources().getString(R.string.str_artist_unknown);

        AudioFile result = new AudioFile(fileName, title, artist, duration);

        return result;
    }

    /**
     * Comparator used for random sorting of {@link AudioFile} objects
     */
    public static class AudioRandomComparator implements Comparator<AudioFile>
    {
        private final int shuffle;

        /**
         * Creates an instance of {@link AudioRandomComparator}
         * @param shuffle random number used as a sort key; Each unique key provides a constant unique sort order
         */
        public AudioRandomComparator(int shuffle)
        {
            this.shuffle = shuffle;
        }

        @Override
        public int compare(AudioFile audioFile, AudioFile audioFile2)
        {
            int h1 = audioFile.getTitle().hashCode();
            int h2 = audioFile2.getTitle().hashCode();

            int x1 = h1 ^ shuffle;
            int x2 = h2 ^ shuffle;

            if (x1 == x2)
                return 0;
            if (x1 > x2)
                return -1;

            return 1;
        }
    }
}
