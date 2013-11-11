package net.illusor.swipeplayer.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

abstract class SwipePagerAdapter extends PagerAdapter
{
    private Fragment playlistFragment;
    private final List<Fragment> browserFragments = new ArrayList<>();
    private final List<Fragment.SavedState> browserStates = new ArrayList<>();
    private final List<File> browserFolders = new ArrayList<>();

    private final FragmentManager fragmentManager;
    private FragmentTransaction curTransaction = null;
    private Fragment primaryFragment;

    public SwipePagerAdapter(FragmentManager fragmentManager)
    {
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
        Fragment fragment;

        if (this.isPlaylistIndex(position))
            fragment = this.playlistFragment;
        else
            fragment = this.browserFragments.get(position);

        if (fragment != null)
            return fragment;

        if (this.curTransaction == null)
            this.curTransaction = this.fragmentManager.beginTransaction();

        if (this.isPlaylistIndex(position))
        {
            fragment = this.getPlaylistFragment();
            this.playlistFragment = fragment;
        }
        else
        {
            File folder = this.browserFolders.get(position);
            fragment = this.getBrowserFragment(folder);

            Fragment.SavedState savedState = this.browserStates.get(position);
            if (savedState != null)
                fragment.setInitialSavedState(savedState);

            this.browserFragments.set(position, fragment);
            this.browserStates.set(position, null);
        }

        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);

        this.curTransaction.add(container.getId(), fragment);

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        Fragment fragment = (Fragment)object;
        if (fragment == this.playlistFragment)
            return;

        if (this.curTransaction == null)
            this.curTransaction = this.fragmentManager.beginTransaction();

        if (this.browserFragments.contains(fragment))
        {
            Fragment.SavedState savedState = this.fragmentManager.saveFragmentInstanceState(fragment);
            this.browserStates.set(position, savedState);
            this.browserFragments.set(position, null);
        }

        curTransaction.remove(fragment);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object)
    {
        Fragment fragment = (Fragment)object;
        if (!fragment.equals(this.primaryFragment))
        {
            if (this.primaryFragment != null)
            {
                this.primaryFragment.setMenuVisibility(false);
                this.primaryFragment.setUserVisibleHint(false);
            }

            fragment.setMenuVisibility(true);
            fragment.setUserVisibleHint(true);

            if (primaryFragment != this.playlistFragment)
            {
                int index = this.browserFragments.indexOf(primaryFragment);
                if (index > position)
                    this.removeFolder(index);
            }

            this.primaryFragment = fragment;
        }
    }

    @Override
    public int getItemPosition(Object object)
    {
        if (object.equals(this.playlistFragment))
            return this.browserFragments.size();

        int index = this.browserFragments.indexOf(object);
        return index >= 0 ? PagerAdapter.POSITION_UNCHANGED : PagerAdapter.POSITION_NONE;
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

    public List<File> getBrowserFolders()
    {
        return new ArrayList<>(this.browserFolders);
    }

    protected abstract Fragment getPlaylistFragment();

    protected abstract Fragment getBrowserFragment(File folder);

    public int findFolder(File folder)
    {
        int value = this.browserFolders.indexOf(folder);
        return value;
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
