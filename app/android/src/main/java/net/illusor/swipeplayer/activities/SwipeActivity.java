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

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.fragments.AboutDialog;
import net.illusor.swipeplayer.fragments.FolderBrowserFragment;
import net.illusor.swipeplayer.fragments.PlaylistFragment;
import net.illusor.swipeplayer.helpers.PreferencesHelper;
import net.illusor.swipeplayer.services.AudioPlayerState;
import net.illusor.swipeplayer.services.SoundService;

import java.io.File;
import java.util.List;

/**
 * Main application activity
 */
public class SwipeActivity extends FragmentActivity
{
    //options menu codes
    private static final int MENU_CODE_QUIT = 0;//quit application
    private static final int MENU_CODE_ABOUT = 1;//show "About" dialog

    private final SoundServiceConnection connection = new SoundServiceConnection();//connection to the sound service
    private LocalPagerAdapter pagerAdapter;//provides activity fragments sliding logic
    private ViewPager viewPager;//fragments container
    private PlaylistFragment playlistFragment;//fragment which shows list of music files playing
    private File currentMediaDirectory;//which directory we look music files in

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.setContentView(R.layout.activity_swipe);

        this.viewPager = (ViewPager) this.findViewById(R.id.id_swipe_view_pager);
        this.pagerAdapter = new LocalPagerAdapter(this.getSupportFragmentManager());

        this.currentMediaDirectory = PreferencesHelper.getStoredPlaylist(this);

        if (savedInstanceState == null)
        {
            Pair<File, File> lastBrowsedFolder = PreferencesHelper.getBrowserFolders(this);
            if (lastBrowsedFolder != null)
                this.pagerAdapter.setCurrentFolder(lastBrowsedFolder);
            else
                this.pagerAdapter.addFolder(new File(File.separator));

            this.viewPager.setAdapter(this.pagerAdapter);
            this.viewPager.setCurrentItem(this.pagerAdapter.getCount() - 1);
        }
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
    protected void onStart()
    {
        super.onStart();
        this.connection.bind();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        this.connection.unbind();
        Pair<File, File> browsedFolder = this.pagerAdapter.getCurrentFolder();
        PreferencesHelper.setBrowserFolders(this, browsedFolder);
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

    private class SoundServiceConnection implements ServiceConnection
    {
        private SoundService.SoundServiceBinder service;

        private void bind()
        {
            Intent intent = new Intent(getBaseContext(), SoundService.class);
            bindService(intent, this, Service.BIND_AUTO_CREATE);
        }

        private void unbind()
        {
            unbindService(this);
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder)
        {
            this.service = (SoundService.SoundServiceBinder)binder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            this.service = null;
        }
    }
}
