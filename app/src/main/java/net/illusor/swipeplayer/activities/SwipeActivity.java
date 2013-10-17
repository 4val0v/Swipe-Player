package net.illusor.swipeplayer.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.fragments.FolderBrowserFragment;
import net.illusor.swipeplayer.fragments.PlaylistFragment;
import org.apache.commons.lang.NotImplementedException;

public class SwipeActivity extends FragmentActivity
{
    private Fragment fragmentPlaylist, fragmentFolderBrowser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.swipe_activity);

        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(this.getSupportFragmentManager());
        ViewPager viewPager = (ViewPager)this.findViewById(R.id.id_swipe_view_pager);
        viewPager.setAdapter(fragmentPagerAdapter);
    }

    private class FragmentPagerAdapter extends FragmentStatePagerAdapter
    {
        private FragmentPagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        public Fragment getItem(int i)
        {
            switch (i)
            {
                case 0:
                {
                    if (fragmentPlaylist == null)
                        fragmentPlaylist = new PlaylistFragment();

                    return fragmentPlaylist;
                }
                case 1:
                {
                    if (fragmentFolderBrowser == null)
                        fragmentFolderBrowser = new FolderBrowserFragment();

                    return fragmentFolderBrowser;
                }
                default:
                    throw new NotImplementedException();
            }
        }

        @Override
        public int getCount()
        {
            return 2;
        }
    }
}
