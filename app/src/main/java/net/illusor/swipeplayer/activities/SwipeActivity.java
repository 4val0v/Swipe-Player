package net.illusor.swipeplayer.activities;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.fragments.FolderBrowserFragment;
import net.illusor.swipeplayer.fragments.PlaylistFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SwipeActivity extends FragmentActivity
{
    private SwipePagerAdapter pagerAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.swipe_activity);

        this.pagerAdapter = new SwipePagerAdapter(this.getSupportFragmentManager());

        this.viewPager = (ViewPager) this.findViewById(R.id.id_swipe_view_pager);
        this.viewPager.setAdapter(this.pagerAdapter);
        this.viewPager.setCurrentItem(this.pagerAdapter.getCount() - 1);
    }

    public void directoryOpen(File folder)
    {
        int index = this.pagerAdapter.open(folder);
        int currentIndex = this.viewPager.getCurrentItem();
        this.viewPager.setCurrentItem(index, Math.abs(index - currentIndex) < 2);
    }

    public List<File> getNavigationHistory()
    {
        return this.pagerAdapter.getFolders();
    }

    private class SwipePagerAdapter extends ListPagerAdapter
    {
        private FolderBrowserController controller = new FolderBrowserController(Environment.getRootDirectory());

        private SwipePagerAdapter(FragmentManager fm)
        {
            super(fm);
        }

        @Override
        protected Fragment getFragment(int position)
        {
            if (position == this.getCount() - 1)
            {
                return new PlaylistFragment();
            }
            else
            {
                File folder = this.controller.get(position);
                return FolderBrowserFragment.newInstance(folder);
            }
        }

        @Override
        public int getItemPosition(Object object)
        {
            if (object instanceof PlaylistFragment)
                return this.getCount() - 1;

            return super.getItemPosition(object);
        }

        public List<File> getFolders()
        {
            return this.controller.getFolders();
        }

        public int open(File folder)
        {
            int index = this.controller.open(folder);

            if (index < 0)
            {
                this.popFragmentStack(-index);
                this.pushFragmentStack();
                this.notifyDataSetChanged();
                index = 0;
            }
            else if (index == 0)
            {
                this.pushFragmentStack();
                this.notifyDataSetChanged();
            }

            return index;
        }
    }

    private class FolderBrowserController
    {
        private List<File> folders = new ArrayList<>();

        public FolderBrowserController(File rootFolder)
        {
            this.folders.add(rootFolder);
        }

        public File get(int index)
        {
            return this.folders.get(index);
        }

        public int open(File folder)
        {
            int index = this.folders.indexOf(folder);
            if (index >= 0)
            {
                return index;
            }
            else
            {
                File parent = folder.getParentFile();
                int parentIndex = this.folders.indexOf(parent);
                if (parentIndex < 0)
                    throw new IllegalStateException("Can not navigate to directory which is not child of the rootFolder");

                for (int i = 0; i < parentIndex; i++)
                    this.folders.remove(0);

                this.folders.add(0, folder);

                return -parentIndex;
            }
        }

        public List<File> getFolders()
        {
            List<File> copy = new ArrayList<>(this.folders);
            Collections.reverse(copy);
            return copy;
        }
    }
}
