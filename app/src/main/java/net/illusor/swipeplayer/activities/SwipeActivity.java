package net.illusor.swipeplayer.activities;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.fragments.FolderBrowserFragment;
import net.illusor.swipeplayer.fragments.PlaylistFragment;

import java.io.File;
import java.util.ArrayList;
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

        this.pagerAdapter = new SwipePagerAdapter(this.getSupportFragmentManager(), Environment.getRootDirectory());

        this.viewPager = (ViewPager) this.findViewById(R.id.id_swipe_view_pager);
        this.viewPager.setAdapter(this.pagerAdapter);
        this.viewPager.setCurrentItem(this.pagerAdapter.getCount() - 1);
        this.viewPager.setOnPageChangeListener(new PageChangeListener());
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

    private static class SwipePagerAdapter extends ListPagerAdapter
    {
        private FolderBrowserController controller;

        private SwipePagerAdapter(FragmentManager fm, File rootDirectory)
        {
            super(fm);
            this.controller = new FolderBrowserController(rootDirectory);
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
            FolderBrowserController.OpenResult open = this.controller.open(folder);

            switch (open.result)
            {
                case FolderBrowserController.FOLDERS_UNCHANGED:
                    break;
                case FolderBrowserController.FOLDERS_REMOVED:
                {
                    this.popFragmentStack(open.argument);
                    this.notifyDataSetChanged();
                    break;
                }
                case FolderBrowserController.FOLDERS_RESTRUCTURED:
                {
                    this.popFragmentStack(open.argument);
                    this.pushFragmentStack();
                    this.notifyDataSetChanged();
                    break;
                }
            }

            return open.argument;
        }

        public void pop()
        {
            this.controller.pop();
            this.popFragmentStack();
        }
    }

    private static class FolderBrowserController
    {
        public static final int FOLDERS_RESTRUCTURED = -1;
        public static final int FOLDERS_REMOVED = -2;
        public static final int FOLDERS_UNCHANGED = -3;

        private List<File> folders = new ArrayList<>();

        public FolderBrowserController(File rootFolder)
        {
            this.folders.add(rootFolder);
        }

        public File get(int index)
        {
            return this.folders.get(index);
        }

        public void pop()
        {
            this.folders.remove(this.folders.size() - 1);
        }

        public OpenResult open(File folder)
        {
            int foldersCount = this.folders.size();
            int index = this.folders.indexOf(folder);

            if (index == foldersCount - 1)
            {
                return new OpenResult(FOLDERS_UNCHANGED, foldersCount - 1);
            }
            if (index >= 0)
            {
                int count = this.folders.size();
                for (int i = index + 1; i < count; i++)
                    this.folders.remove(this.folders.size() - 1);

                return new OpenResult(FOLDERS_REMOVED, this.folders.size() - 1);
            }
            else
            {
                File parent = folder.getParentFile();
                int parentIndex = this.folders.indexOf(parent);
                if (parentIndex < 0)
                    throw new IllegalStateException("Can not navigate to directory which is not child of the rootFolder");

                int count = this.folders.size();
                for (int i = parentIndex + 1; i < count; i++)
                    this.folders.remove(this.folders.size() - 1);

                this.folders.add(folder);

                return new OpenResult(FOLDERS_RESTRUCTURED, this.folders.size() - 1);
            }
        }

        public List<File> getFolders()
        {
            return new ArrayList<>(this.folders);
        }

        public class OpenResult
        {
            public OpenResult(int result, int argument)
            {
                this.result = result;
                this.argument = argument;
            }

            public int result;
            public int argument;
        }
    }

    private class PageChangeListener implements ViewPager.OnPageChangeListener
    {
        private int currentPageIndex = -1;

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
        {
        }

        @Override
        public void onPageSelected(int position)
        {
            this.currentPageIndex = -1;

        }

        @Override
        public void onPageScrollStateChanged(int state)
        {
        }
    }
}
