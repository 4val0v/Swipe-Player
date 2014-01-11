package net.illusor.swipeplayer.fragments;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import net.illusor.swipeplayer.domain.AudioFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Gets all the music files contained into the directory
 */
class AudioFilesLoader extends AudioFoldersLoader
{
    /**
     * Creates a new instance of {@link AudioFilesLoader}
     * @param context Current activity context
     * @param directory Directory to load audio files from
     */
    public AudioFilesLoader(Context context, File directory)
    {
        super(context, directory);
    }

    @Override
    protected List<AudioFile> getMediaObjects(Cursor cursor)
    {
        List<AudioFile> result = new ArrayList<>();
        if (cursor == null) return result;

        while (cursor.moveToNext())
        {
            AudioFile mediaObject = this.getNextLevelObject(cursor);
            if (!result.contains(mediaObject))
                result.add(mediaObject);
        }

        return result;
    }

    /**
     * Reads the {@link Cursor} entry and creates an {@link AudioFile} from it
     * @param cursor Cursor to read
     * @return Resulting audio file
     */
    private AudioFile getNextLevelObject(Cursor cursor)
    {
        String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
        String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
        String author = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
        long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
        AudioFile result = new AudioFile(fileName, title, author, duration);
        return result;
    }
}
