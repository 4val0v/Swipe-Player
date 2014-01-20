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

package net.illusor.swipeplayer.helpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import java.io.File;

/**
 * Provides methods for convinient interrogation with android shared preferences API
 * */
public class PreferencesHelper
{
    private static final String SHARED_PREF_PLAYLIST_KEY = "net.illusor.swipeplayer.playlist";
    private static final String SHARED_PREF_FOLDER_KEY_START = "net.illusor.swipeplayer.folder-start";
    private static final String SHARED_PREF_FOLDER_KEY_END = "net.illusor.swipeplayer.folder-end";

    /**
     * Gets information about the directory where audio files should be looked for in
     * @param context Current activity context
     * @return Directory used as a playlist root
     */
    public static File getStoredPlaylist(Context context)
    {
        File file = getFileByKey(context, SHARED_PREF_PLAYLIST_KEY);
        return file;
    }

    /**
     * Saves information about the directory where audio files should be looked for in
     * @param context Current activity context
     * @param file Directory used as a playlist root
     */
    public static void setStoredPlaylist(Context context, File file)
    {
        setFileByKey(context, SHARED_PREF_PLAYLIST_KEY, file);
    }

    /**
     * Gets information about which folders were last open into the folder browser
     * @param context Current activity context
     * @return Pair of files: (root of the opened hierarchy, last child of the opened hierarchy)
     */
    public static Pair<File, File> getBrowserFolders(Context context)
    {
        File start = getFileByKey(context, SHARED_PREF_FOLDER_KEY_START);
        if (start == null) return null;
        File end = getFileByKey(context, SHARED_PREF_FOLDER_KEY_END);
        return new Pair<>(start, end);
    }

    /**
     * Saves information about which folders are opened into the folder browser
     * @param context Current activity context
     * @param files Pair of files: (root of the opened hierarchy, last child of the opened hierarchy)
     */
    public static void setBrowserFolders(Context context, Pair<File, File> files)
    {
        setFileByKey(context, SHARED_PREF_FOLDER_KEY_START, files.first);
        setFileByKey(context, SHARED_PREF_FOLDER_KEY_END, files.second);
    }

    /**
     * Reads a preference by key and returns its value
     * @param context Current activity context
     * @param key Preference key
     * @return File, contained in a preference value
     */
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

    /**
     * Sets a preference value
     * @param context Current activity context
     * @param key Preference key
     * @param file File to save
     */
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
