package net.illusor.swipeplayer.helpers;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.File;

public class PreferencesHelper
{
    private static final String SHARED_PREF_PLAYLIST_KEY = "net.illusor.swipeplayer.playlist";

    public static File getStoredPlaylist(Context context)
    {
        SharedPreferences preferences = context.getSharedPreferences(SHARED_PREF_PLAYLIST_KEY, Context.MODE_PRIVATE);
        if (preferences.contains(SHARED_PREF_PLAYLIST_KEY))
        {
            String path = preferences.getString(SHARED_PREF_PLAYLIST_KEY, "");
            return new File(path);
        }
        return null;
    }

    public static void setStoredPlaylist(Context context, File file)
    {
        if (file != null)
        {
            SharedPreferences preferences = context.getSharedPreferences(SHARED_PREF_PLAYLIST_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(SHARED_PREF_PLAYLIST_KEY, file.getAbsolutePath());
            editor.commit();
        }
    }

}
