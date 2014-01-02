package net.illusor.swipeplayer.activities;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

abstract class SwipePagerAdapter extends PagerAdapter
{
    private Fragment playlistFragment;
    private final ArrayList<Fragment> browserFragments = new ArrayList<>();
    private final ArrayList<Fragment.SavedState> browserStates = new ArrayList<>();
    private final ArrayList<File> browserFolders = new ArrayList<>();

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
        if (this.curTransaction != null)
        {
            this.curTransaction.commitAllowingStateLoss();
            this.curTransaction = null;
            this.fragmentManager.executePendingTransactions();
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
                final int index = this.browserFragments.indexOf(primaryFragment);
                if (index > position)
                {
                    this.onDataChange(primaryFragment, fragment);
                    container.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            removeFolder(index);
                        }
                    }, 300);
                }
            }

            this.primaryFragment = fragment;
        }
    }

    @Override
    public int getItemPosition(Object object)
    {
        if (object.equals(this.playlistFragment))
            return this.browserFolders.size();

        int index = this.browserFragments.indexOf(object);
        return index >= 0 ? PagerAdapter.POSITION_UNCHANGED : PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount()
    {
        return this.browserFolders.size() + 1;
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return ((Fragment)object).getView() == view;
    }

    public Parcelable saveObjectState()
    {
        Bundle state = new Bundle();

        for (int i = 0; i < this.browserFolders.size(); i++)
        {
            Fragment fragment = this.browserFragments.get(i);
            if (fragment == null) continue;

            this.fragmentManager.putFragment(state, "fs"+i, fragment);
        }

        state.putSerializable("states", this.browserStates);
        state.putSerializable("folders", this.browserFolders);

        return state;
    }

    public void restoreObjectState(Parcelable state)
    {
        Bundle bundle = (Bundle)state;
        ArrayList<Fragment.SavedState> fragmentStates = (ArrayList)bundle.getSerializable("states");
        ArrayList<File> folderStates = (ArrayList)bundle.getSerializable("folders");

        this.browserStates.clear();
        for (Fragment.SavedState fs : fragmentStates)
            this.browserStates.add(fs);

        this.browserFragments.clear();
        this.browserFolders.clear();
        for (File folder : folderStates)
        {
            this.browserFolders.add(folder);
            this.browserFragments.add(null);

        }

        Set<String> keys = ((Bundle) state).keySet();
        for (String key : keys)
        {
            if (!key.startsWith("fs")) continue;

            Fragment fragment = this.fragmentManager.getFragment(bundle, key);
            int index = Integer.valueOf(key.substring(2));
            this.browserFragments.set(index, fragment);
        }
    }

    public List<File> getData()
    {
        return new ArrayList<>(this.browserFolders);
    }

    protected abstract Fragment getPlaylistFragment();

    protected abstract Fragment getBrowserFragment(File folder);

    protected void onDataChange(Fragment oldPrimary, Fragment newPrimary)
    {

    }

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

    public Pair<File, File> getCurrentFolder()
    {
        File first = this.browserFolders.get(0);
        File second = this.browserFolders.get(this.browserFolders.size() - 1);
        return new Pair<>(first, second);
    }

    public void setCurrentFolder(Pair<File, File> files)
    {
        String root = files.first.getParent();
        File folder = files.second;
        do
        {
            if (folder.exists())
            {
                this.browserFolders.add(0, folder);
                this.browserFragments.add(0, null);
                this.browserStates.add(0, null);
            }
            folder = folder.getParentFile();
        }
        while (folder != null && !folder.getAbsolutePath().equals(root));
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
        return index == this.browserFolders.size();
    }
}
