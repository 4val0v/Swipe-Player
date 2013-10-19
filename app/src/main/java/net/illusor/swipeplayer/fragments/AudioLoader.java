package net.illusor.swipeplayer.fragments;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.support.v4.content.AsyncTaskLoader;
import net.illusor.swipeplayer.domain.AudioFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

class AudioLoader extends AsyncTaskLoader<List<AudioFile>>
{
    private File directory;
    private List<AudioFile> result;

    public AudioLoader(Context context, File directory)
    {
        super(context);
        this.directory = directory;
    }

    @Override
    protected void onStartLoading()
    {
        if (this.takeContentChanged() || this.result == null)
            this.forceLoad();
        else
            this.deliverResult(this.result);
    }

    @Override
    public List<AudioFile> loadInBackground()
    {
        Cursor cursor = this.getContext().getContentResolver().query(MediaStore.Audio.Media.INTERNAL_CONTENT_URI,
                new String[]{ MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATA },
                MediaStore.Audio.Media.DATA + " like ? and " + MediaStore.Audio.Media.IS_MUSIC + "!=0",
                new String[]{ this.directory.getAbsolutePath() + "%" }, null);

        List<AudioFile> mediaObjects = this.getMediaObjects(cursor);

        cursor.close();

        Collections.sort(mediaObjects, new AudioFileComparator());

        return mediaObjects;
    }

    private List<AudioFile> getMediaObjects(Cursor cursor)
    {
        this.result = new ArrayList<>();
        if (cursor == null) return result;

        while (cursor.moveToNext())
        {
            AudioFile mediaObject = this.getNextLevelObject(cursor);
            if (!this.result.contains(mediaObject))
                this.result.add(mediaObject);
        }

        return this.result;
    }

    private AudioFile getNextLevelObject(Cursor cursor)
    {
        String fileName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

        int length = this.directory.getAbsolutePath().length();
        int separatorPos = fileName.indexOf(File.separator, length + 1);

        AudioFile result;
        if (separatorPos < 0)
        {
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
            String author = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
            long duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION));
            result = new AudioFile(fileName, title, author, duration);
        }
        else
        {
            String directory = fileName.substring(0, separatorPos);
            result = new AudioFile(directory);
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
