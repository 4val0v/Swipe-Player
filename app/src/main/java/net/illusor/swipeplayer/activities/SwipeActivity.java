package net.illusor.swipeplayer.activities;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.fragments.FolderBrowserFragment;
import net.illusor.swipeplayer.fragments.PlaylistFragment;
import net.illusor.swipeplayer.widgets.AudioControlView;

import java.io.File;
import java.util.List;

public class SwipeActivity extends FragmentActivity
{
    private AudioControlView audioControlPanel;
    private LocalPagerAdapter pagerAdapter;
    private ViewPager viewPager;
    private PlaylistFragment playlistFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        this.setContentView(R.layout.swipe_activity);

        this.viewPager = (ViewPager) this.findViewById(R.id.id_swipe_view_pager);
        this.audioControlPanel = (AudioControlView)this.findViewById(R.id.id_swipe_control);
        this.pagerAdapter = new LocalPagerAdapter(this.getSupportFragmentManager());

        if (savedInstanceState == null)
        {
            this.pagerAdapter.addFolder(Environment.getExternalStorageDirectory());
            this.viewPager.setAdapter(this.pagerAdapter);
            this.viewPager.setCurrentItem(1);
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

    public List<File> getNavigationHistory()
    {
        return this.pagerAdapter.getBrowserFolders();
    }

    public AudioControlView getAudioControl()
    {
        return audioControlPanel;
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
