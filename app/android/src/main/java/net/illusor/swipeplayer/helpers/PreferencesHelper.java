package net.illusor.swipeplayer.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import java.io.File;

public class PreferencesHelper
{
    private static final String SHARED_PREF_PLAYLIST_KEY = "net.illusor.swipeplayer.playlist";
    private static final String SHARED_PREF_FOLDER_KEY_START = "net.illusor.swipeplayer.folder-start";
    private static final String SHARED_PREF_FOLDER_KEY_END = "net.illusor.swipeplayer.folder-end";

    public static File getStoredPlaylist(Context context)
    {
        File file = getFileByKey(context, SHARED_PREF_PLAYLIST_KEY);
        return file;
    }

    public static void setStoredPlaylist(Context context, File file)
    {
        setFileByKey(context, SHARED_PREF_PLAYLIST_KEY, file);
    }

    public static Pair<File, File> getBrowserFolders(Context context)
    {
        File start = getFileByKey(context, SHARED_PREF_FOLDER_KEY_START);
        if (start == null) return null;
        File end = getFileByKey(context, SHARED_PREF_FOLDER_KEY_END);
        return new Pair<>(start, end);
    }

    public static void setBrowserFolders(Context context, Pair<File, File> files)
    {
        setFileByKey(context, SHARED_PREF_FOLDER_KEY_START, files.first);
        setFileByKey(context, SHARED_PREF_FOLDER_KEY_END, files.second);
    }

    private static File getFileByKey(Context context, String key)
    {
        SharedPreferences preferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        if (preferences.contains(key))
        {
            String path = preferences.getString(key, "");
            return new File(path);
        }
        return null;
    }

    private static void setFileByKey(Context context, String key, File file)
    {
        if (file != null)
        {
            SharedPreferences preferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, file.getAbsolutePath());
            editor.commit();
        }
    }
}
