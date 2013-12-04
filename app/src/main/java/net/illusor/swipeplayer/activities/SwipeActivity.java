package net.illusor.swipeplayer.activities;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Pair;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.fragments.FolderBrowserFragment;
import net.illusor.swipeplayer.fragments.PlaylistFragment;
import net.illusor.swipeplayer.helpers.PreferencesHelper;

import java.io.File;
import java.util.List;

public class SwipeActivity extends FragmentActivity
{
    private LocalPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private PlaylistFragment playlistFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.setContentView(R.layout.activity_swipe);

        this.viewPager = (ViewPager) this.findViewById(R.id.id_swipe_view_pager);
        this.pagerAdapter = new LocalPagerAdapter(this.getSupportFragmentManager());

        if (savedInstanceState == null)
        {
            Pair<File, File> lastBrowsedFolder = PreferencesHelper.getBrowserFolders(this);
            if (lastBrowsedFolder != null)
                this.pagerAdapter.setCurrentFolder(lastBrowsedFolder);
            else
                this.pagerAdapter.addFolder(Environment.getExternalStorageDirectory());

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
    protected void onStop()
    {
        super.onStop();
        Pair<File, File> browsedFolder = this.pagerAdapter.getCurrentFolder();
        PreferencesHelper.setBrowserFolders(this, browsedFolder);
    }

    public void openMediaBrowser()
    {
        final int count = this.pagerAdapter.getCount();
        this.viewPager.setCurrentItem(count - 2, true);
    }

    public void playMediaDirectory(File directory)
    {
        this.playlistFragment.setMediaDirectory(directory);
        this.viewPager.setCurrentItem(this.pagerAdapter.getCount() - 1, true);
    }

    public void openMediaDirectory(File directory)
    {
        int index = this.pagerAdapter.findFolder(directory);
        if (index >= 0)
        {
            int current = this.viewPager.getCurrentItem();
            for (int i  = current; i > index; i--)
                this.viewPager.setCurrentItem(i - 1, false);
        }
        else
        {
            this.pagerAdapter.addFolder(directory);
            int item = this.viewPager.getCurrentItem();
            this.viewPager.setCurrentItem(item + 1, true);
        }
    }

    public List<File> getBrowserHistory()
    {
        return this.pagerAdapter.getData();
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
}
