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
import net.illusor.swipeplayer.widgets.SimpleSlidingDrawer;

import java.io.File;
import java.util.List;

public class SwipeActivity extends FragmentActivity
{
    private PlaylistFragment playlistFragment;
    private SimpleSlidingDrawer slidingDrawer;
    private ViewPager viewPager;
    private LocalPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.setContentView(R.layout.activity_swipe);

        this.viewPager = (ViewPager) this.findViewById(R.id.id_slider_browser_content);
        this.pagerAdapter = new LocalPagerAdapter(this.getSupportFragmentManager());
        this.playlistFragment = (PlaylistFragment)this.getSupportFragmentManager().findFragmentById(R.id.id_fragment_playlist);
        this.slidingDrawer = (SimpleSlidingDrawer)this.findViewById(R.id.id_slider_browser);

        if (savedInstanceState == null)
        {
            Pair<File, File> lastBrowsedFolder = PreferencesHelper.getBrowserFolders(this);
            if (lastBrowsedFolder != null)
                this.pagerAdapter.setCurrentFolder(lastBrowsedFolder);
            else
                this.pagerAdapter.setFolder(Environment.getExternalStorageDirectory());

            this.viewPager.setAdapter(this.pagerAdapter);
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
        this.slidingDrawer.animateOpen();
    }

    public void playMediaDirectory(File directory)
    {
        this.playlistFragment.setMediaDirectory(directory);
        this.slidingDrawer.animateClose();
    }

    public void openMediaDirectory(File directory)
    {
        int index = this.pagerAdapter.findFolder(directory);
        if (index < 0)
        {
            this.pagerAdapter.setFolder(directory);
            int item = this.viewPager.getCurrentItem();
            this.viewPager.setCurrentItem(item + 1, true);
        }
        else
        {
             this.viewPager.setCurrentItem(index, true);
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
        protected Fragment getBrowserFragment(File folder)
        {
            return FolderBrowserFragment.newInstance(folder);
        }
    }
}
