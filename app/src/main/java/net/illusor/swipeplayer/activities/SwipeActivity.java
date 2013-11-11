package net.illusor.swipeplayer.activities;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
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

        this.pagerAdapter = new LocalPagerAdapter(this.getSupportFragmentManager());
        this.pagerAdapter.addFolder(Environment.getExternalStorageDirectory());

        this.viewPager = (ViewPager) this.findViewById(R.id.id_swipe_view_pager);
        this.viewPager.setAdapter(this.pagerAdapter);
        this.viewPager.setCurrentItem(5);

        this.audioControlPanel = (AudioControlView)this.findViewById(R.id.id_swipe_control);
    }

    public void directoryOpen(File folder)
    {
        int index = this.pagerAdapter.findFolder(folder);
        if (index >= 0)
        {
            this.viewPager.setCurrentItem(index, true);
        }
        else
        {
            this.pagerAdapter.addFolder(folder);
            int item = this.viewPager.getCurrentItem();
            this.viewPager.setCurrentItem(item + 1, true);
        }
    }

    public PlaylistFragment getPlaylistFragment()
    {
        return playlistFragment;
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
