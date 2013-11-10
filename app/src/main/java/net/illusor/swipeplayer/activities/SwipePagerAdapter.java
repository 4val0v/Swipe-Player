package net.illusor.swipeplayer.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import net.illusor.swipeplayer.fragments.FolderBrowserFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SwipePagerAdapter extends PagerAdapter
{
    private final Fragment playlistFragment;
    private final List<Fragment> browserFragments = new ArrayList<>();
    private final List<Fragment.SavedState> browserStates = new ArrayList<>();
    private final List<File> browserFolders = new ArrayList<>();

    private final FragmentManager fragmentManager;
    private FragmentTransaction curTransaction = null;
    private Fragment primaryFragment;
    private int primaryItemIndex;

    public SwipePagerAdapter(FragmentManager fragmentManager, Fragment playlistFragment)
    {
        this.playlistFragment = playlistFragment;
        this.primaryFragment = playlistFragment;
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void finishUpdate(ViewGroup container)
    {
        super.finishUpdate(container);
        if (curTransaction != null)
        {
            curTransaction.commitAllowingStateLoss();
            curTransaction = null;
            fragmentManager.executePendingTransactions();
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position)
    {
        if (this.isPlaylistIndex(position))
            return this.playlistFragment;

        Fragment fragment = this.browserFragments.get(position);
        if (fragment != null)
            return fragment;

        if (this.curTransaction == null)
            this.curTransaction = this.fragmentManager.beginTransaction();

        File folder = this.browserFolders.get(position);
        fragment = FolderBrowserFragment.newInstance(folder);
        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);

        Fragment.SavedState savedState = this.browserStates.get(position);
        if (savedState != null)
            fragment.setInitialSavedState(savedState);

        this.browserFragments.set(position, fragment);
        this.browserStates.set(position, null);

        this.curTransaction.add(container.getId(), fragment);

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        if (this.isPlaylistIndex(position))
            return;

        Fragment fragment = (Fragment)object;

        if (this.curTransaction == null)
            this.curTransaction = this.fragmentManager.beginTransaction();

        Fragment.SavedState savedState = this.fragmentManager.saveFragmentInstanceState(fragment);
        this.browserStates.set(position, savedState);
        this.browserFragments.set(position, null);

        curTransaction.remove(fragment);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object)
    {
        Fragment fragment = (Fragment)object;
        if (!fragment.equals(this.primaryFragment))
        {
            fragment.setMenuVisibility(true);
            fragment.setUserVisibleHint(true);

            Fragment last;
            if (this.isPlaylistIndex(position))
                last = this.playlistFragment;
            else
                last = this.browserFragments.get(position);

            last.setMenuVisibility(false);
            last.setUserVisibleHint(false);

            /*if (position < this.primaryItemIndex && !isPlaylistIndex(this.primaryItemIndex))
                this.removeFolder(this.primaryItemIndex);*/

            this.primaryItemIndex = position;
            this.primaryFragment = fragment;
        }
    }

    @Override
    public int getItemPosition(Object object)
    {
        if (object.equals(this.playlistFragment))
            return this.browserFragments.size();

        int index = this.browserFragments.indexOf(object);
        return index > 0 ? index : PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount()
    {
        return this.browserFragments.size() + 1;
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return ((Fragment)object).getView() == view;
    }

    public void addFolder(File folder)
    {
        this.browserFragments.add(null);
        this.browserStates.add(null);
        this.browserFolders.add(folder);
        this.notifyDataSetChanged();
    }

    private void removeFolder(int position)
    {
        if (this.isPlaylistIndex(position))
            return;

        this.browserFragments.remove(position);
        this.browserStates.remove(position);
        this.browserFolders.remove(position);
        this.notifyDataSetChanged();
    }

    private boolean isPlaylistIndex(int index)
    {
        return index == this.browserFragments.size();
    }
}
