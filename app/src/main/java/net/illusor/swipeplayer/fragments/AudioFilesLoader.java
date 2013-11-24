package net.illusor.swipeplayer.fragments;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import net.illusor.swipeplayer.domain.AudioFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioFilesLoader extends AudioFoldersLoader
{
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
