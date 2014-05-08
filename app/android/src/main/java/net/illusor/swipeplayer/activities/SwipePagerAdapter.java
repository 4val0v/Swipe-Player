/*Copyright 2014 Nikita Kobzev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package net.illusor.swipeplayer.activities;

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

/**
 * Controls the set of fragments, used to browse media directories and play audio files
 */
abstract class SwipePagerAdapter extends PagerAdapter
{
    private Fragment playlistFragment;//fragment, used to play audio files
    private final ArrayList<Fragment> browserFragments = new ArrayList<>();//fragments, used to browse audio directories
    private final ArrayList<Fragment.SavedState> browserStates = new ArrayList<>();//fragments states
    private final ArrayList<File> browserFolders = new ArrayList<>();//audio directories being browsed

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

        //the last fragment in the list - is always playlistFragment
        //check, if we should return it
        if (this.isPlaylistIndex(position))
            fragment = this.playlistFragment;
        else
            fragment = this.browserFragments.get(position);

        //if fragment is null means fragment was disposed to reduce app
        //memory footprint, and we should restore it from its saved state
        //(or this is the first run of the adapter, and there are no fragments initialized)
        if (fragment != null)
            return fragment;

        if (this.curTransaction == null)
            this.curTransaction = this.fragmentManager.beginTransaction();

        //creating a new fragment
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

        this.activateFragment(fragment, false);

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
        final Fragment fragment = (Fragment)object;
        if (!fragment.equals(this.primaryFragment))
        {
            if (this.primaryFragment != null)
                activateFragment(this.primaryFragment, false);

            //here we implement the feature of removing folder-browser fragments, if we swipe the ViewPager Left-To-Right
            //(but we never remove the playlistFragment)
            if (primaryFragment != this.playlistFragment)
            {
                //detect Left-To-Right swipe
                //if detected, remove folder-browser fragment, which got moved to the right of the screen
                int index = this.browserFragments.indexOf(primaryFragment);
                if (index > position)
                    removeFolder(index);
            }

            this.activateFragment(fragment, true);
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

    /**
     * Creates a fragment, used to display audio files to play
     * @return Playlist fragment
     */
    protected abstract Fragment getPlaylistFragment();

    /**
     * Creates a fragment, used to browse audio files stored in the device
     * @param folder Directory to browse
     * @return Fragment used to browse audio directory
     */
    protected abstract Fragment getBrowserFragment(File folder);

    /**
     * Searches this for the specified folder and returns the index of the first occurrence
     * @param folder Folder to search
     * @return Index of the first occurrence, or -1
     */
    public int findFolder(File folder)
    {
        int value = this.browserFolders.indexOf(folder);
        return value;
    }

    /**
     * Opens a new folder browser fragment and browses the specified folder
     * @param folder Folder to browse
     */
    public void addFolder(File folder)
    {
        this.browserFragments.add(null);
        this.browserStates.add(null);
        this.browserFolders.add(folder);

        this.notifyDataSetChanged();
    }

    /**
     * Gets information about currently browsed folders
     * @return Pair of files: (root of the browsed hierarchy, last element of the browsed hierarchy) or null, if the adapter is empty
     */
    public Pair<File, File> getFolderStructure()
    {
        if (this.browserFolders.size() == 0)
            return null;

        File first = this.browserFolders.get(0);
        File second = this.browserFolders.get(this.browserFolders.size() - 1);
        return new Pair<>(first, second);
    }

    /**
     * Restores class state using information about browsed folders
     * @param files Pair of files: (root of the browsed hierarchy, last element of the browsed hierarchy)
     */
    public void setFolderStructure(Pair<File, File> files)
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

    /**
     * Gets list of currently browsed folders
     * @return
     */
    public List<File> getData()
    {
        //wrap browser folders into the new List, to prevent modifications outside of this class
        return new ArrayList<>(this.browserFolders);
    }

    /**
     * Removes folder from folder browser
     * @param position index of the folder to remove
     */
    private void removeFolder(int position)
    {
        this.browserFragments.remove(position);
        this.browserStates.remove(position);
        this.browserFolders.remove(position);
        this.notifyDataSetChanged();
    }

    /**
     * Activates/deactivates fragment
     * @param fragment Fragment to activate
     * @param activate activate or deactivate
     */
    private void activateFragment(Fragment fragment, boolean activate)
    {
        fragment.setMenuVisibility(activate);
        fragment.setUserVisibleHint(activate);
    }

    /**
     * Checks if provided index should be treated as index of the playlistFragment
     * @param index index to check
     * @return <b>true</b> if should;<br><b>false</b> if not
     */
    private boolean isPlaylistIndex(int index)
    {
        return index == this.browserFolders.size();
    }
}
