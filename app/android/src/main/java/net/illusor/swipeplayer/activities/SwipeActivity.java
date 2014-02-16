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

package net.illusor.swipeplayer.activities;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.fragments.AboutDialog;
import net.illusor.swipeplayer.fragments.FolderBrowserFragment;
import net.illusor.swipeplayer.fragments.PlaylistFragment;
import net.illusor.swipeplayer.helpers.DimensionHelper;
import net.illusor.swipeplayer.helpers.PreferencesHelper;
import net.illusor.swipeplayer.services.AudioPlayerState;
import net.illusor.swipeplayer.services.SoundServiceConnection;

import java.io.File;
import java.util.List;

/**
 * Main application activity
 */
public class SwipeActivity extends FragmentActivity
{
    //sometimes we should start playback of some track on playlistFragment loading (when we open a music file with the application)
    public File PLAYBACK_ON_LOAD;
    public static final int SHUFFLE_KEY_NOSHUFFLE = 0;
    //which directory we will use as the folder browser root
    private static final String rootMusicDirectory = File.separator;

    //options menu codes
    private static final int MENU_CODE_QUIT = 0;//quit application
    private static final int MENU_CODE_ABOUT = 1;//show "About" dialog

    private final SoundServiceConnection connection = new LocalServiceConnection(this);//connection to the sound service
    private LocalPagerAdapter pagerAdapter;//provides activity fragments sliding logic
    private ViewPager viewPager;//fragments container
    private PlaylistFragment playlistFragment;//fragment which shows list of music files playing
    private File currentMediaDirectory;//which directory we look music files in
    private SlidingMenu menuOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.setContentView(R.layout.activity_swipe);

        this.menuOptions = (SlidingMenu)this.findViewById(R.id.id_playlist_menu);
        this.viewPager = (ViewPager) this.findViewById(R.id.id_swipe_view_pager);
        this.viewPager.setOnPageChangeListener(new OnSwipeListener());
        this.pagerAdapter = new LocalPagerAdapter(this.getSupportFragmentManager());

        this.handleIncomingIntent(this.getIntent());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        Parcelable state = this.pagerAdapter.saveObjectState();
        outState.putParcelable("pagerAdapter", state);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        Parcelable state = savedInstanceState.getParcelable("pagerAdapter");
        this.pagerAdapter.restoreObjectState(state);

        this.viewPager.setAdapter(this.pagerAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        this.handleIncomingIntent(intent);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        this.connection.bind();

        //if pagerAdapter contains no data - inflate it
        Pair<File, File> structure = this.pagerAdapter.getFolderStructure();
        if (structure != null) return;
        this.initializeFolderBrowser();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        this.connection.unbind();

        Pair<File, File> browsedFolder = this.pagerAdapter.getFolderStructure();
        PreferencesHelper.setBrowserFolderStructure(this, browsedFolder);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add(0, MENU_CODE_ABOUT, 0, R.string.str_menu_about);
        menu.add(0, MENU_CODE_QUIT, 0, R.string.str_menu_exit);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean result = false;
        switch (item.getItemId())
        {
            case MENU_CODE_ABOUT:
            {
                this.appAbout();
                break;
            }
            case MENU_CODE_QUIT:
            {
                this.appShutdown();
                break;
            }
            default:
                result = super.onOptionsItemSelected(item);
        }

        return result;
    }

    @Override
    public void onBackPressed()
    {
        int index = this.viewPager.getCurrentItem();
        if (index == 0)
            super.onBackPressed();
        else
            this.viewPager.setCurrentItem(index - 1, true);
    }

    /**
     * Opens top folder of the folder browser
     */
    public void openMediaBrowser()
    {
        final int count = this.pagerAdapter.getCount();
        this.viewPager.setCurrentItem(count - 2, true);
    }

    /**
     * Sets provided directory as the application playlist root, and fills the playlist with music files, contained into the directory
     * @param directory Directory to set as the playlist root
     */
    public void playMediaDirectory(File directory)
    {
        this.currentMediaDirectory = directory;
        this.playlistFragment.setMediaDirectory(directory);
        this.viewPager.setCurrentItem(this.pagerAdapter.getCount() - 1, true);
    }

    /**
     * Opens the directory into the folder browser
     * @param directory directory to open
     */
    public void openMediaDirectory(File directory)
    {
        //check, if the directory is already a member of currently browsed directories hierarchy
        int index = this.pagerAdapter.findFolder(directory);
        if (index >= 0)
        {
            //if so, just swipe viewPager back
            int current = this.viewPager.getCurrentItem();
            for (int i  = current; i > index; i--)
                this.viewPager.setCurrentItem(i - 1, false);
        }
        else
        {
            //if not, add a new fragment to the viewPager
            this.pagerAdapter.addFolder(directory);
            int item = this.viewPager.getCurrentItem();
            this.viewPager.setCurrentItem(item + 1, true);
        }
    }

    /**
     * Gets list of files, representing currently browsed directories hierarchy
     * @return Currently browsed directories hierarchy
     */
    public List<File> getBrowserHistory()
    {
        return this.pagerAdapter.getData();
    }

    /**
     * Gets the directory, where audio files are searched
     * @return Playlist root directory
     */
    public File getCurrentMediaDirectory()
    {
        return currentMediaDirectory;
    }

    /**
     * Handles the intent which the activity was started with
     * @param intent incoming intent
     */
    private void handleIncomingIntent(Intent intent)
    {
        //if the activity was started as usual, the intent will contain no data
        //but if it was started via "open with dialog", the intent will contain data about the file user wanted to open with the application
        if (!Intent.ACTION_VIEW.equals(intent.getAction()) || intent.getData() == null)
            return;

        File audioFile = new File(intent.getData().getPath());
        File playlistFolder = audioFile.getParentFile();

        this.PLAYBACK_ON_LOAD = audioFile;

        //if the playlistFragment is online - just tell it to load the new playlist
        //if not,-override the application settings, and playlistFragment will read them when loading
        if (this.playlistFragment != null)
            this.playMediaDirectory(playlistFolder);
        else
            PreferencesHelper.setStoredPlaylist(this, playlistFolder);
    }

    private void initializeFolderBrowser()
    {
        this.currentMediaDirectory = PreferencesHelper.getStoredPlaylist(this);

        Pair<File, File> structure = PreferencesHelper.getBrowserFolderStructure(this);
        if (structure != null)
            this.pagerAdapter.setFolderStructure(structure);
        else
            this.pagerAdapter.addFolder(new File(rootMusicDirectory));

        this.viewPager.setAdapter(this.pagerAdapter);
        this.viewPager.setCurrentItem(this.pagerAdapter.getCount() - 1);
    }

    /**
     * <p><i>Options menu action</i></p>
     * Displays "About" dialog
     */
    private void appAbout()
    {
        DialogFragment about = new AboutDialog();
        about.show(this.getSupportFragmentManager(), "");
    }

    /**
     * <p><i>Options menu action</i></p>
     * Quits the application
     */
    private void appShutdown()
    {
        if (this.connection.service != null && this.connection.service.getState() != AudioPlayerState.Stopped)
            this.connection.service.stop();
        this.finish();
    }

    private class LocalPagerAdapter extends SwipePagerAdapter
    {
        private LocalPagerAdapter(FragmentManager fragmentManager)
        {
            super(fragmentManager);
        }

        @Override
        protected Fragment getPlaylistFragment()
        {
            if (playlistFragment == null)
                playlistFragment = new PlaylistFragment();

            return playlistFragment;
        }

        @Override
        protected Fragment getBrowserFragment(File folder)
        {
            return FolderBrowserFragment.newInstance(folder);
        }
    }

    private class LocalServiceConnection extends SoundServiceConnection
    {
        private final Context context;

        private LocalServiceConnection(Context context)
        {
            this.context = context;
        }

        @Override
        public Context getContext()
        {
            return this.context;
        }
    }

    private class OnSwipeListener extends ViewPager.SimpleOnPageChangeListener
    {
        @Override
        public void onPageSelected(int position)
        {
            int playlistFragmentPos = viewPager.getAdapter().getCount() - 1;
            menuOptions.setTouchModeAbove(position == playlistFragmentPos ? SlidingMenu.TOUCHMODE_FULLSCREEN : SlidingMenu.TOUCHMODE_NONE);
        }
    }
}
