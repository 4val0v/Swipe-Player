package net.illusor.swipeplayer.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

abstract class ListPagerAdapter extends PagerAdapter
{
    private final FragmentManager fragmentManager;
    private FragmentTransaction curTransaction = null;
    private List<Fragment> fragments = new ArrayList<>();
    private List<Fragment.SavedState> savedStates = new ArrayList<>();
    private Fragment primaryItem;

    protected ListPagerAdapter(FragmentManager mFragmentManager)
    {
        this.fragmentManager = mFragmentManager;
        this.pushFragmentStack();
        this.pushFragmentStack();
    }

    @Override
    public void startUpdate(ViewGroup container)
    {
    }

    @Override
    public void finishUpdate(ViewGroup container)
    {
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
        Fragment fragment = this.fragments.get(position);
        if (fragment != null)
            return fragment;

        if (this.curTransaction == null)
            this.curTransaction = this.fragmentManager.beginTransaction();

        fragment = this.getFragment(position);
        Fragment.SavedState savedState = this.savedStates.get(position);
        if (savedState != null)
            fragment.setInitialSavedState(savedState);

        this.fragments.set(position, fragment);

        fragment.setMenuVisibility(false);
        fragment.setUserVisibleHint(false);

        this.curTransaction.add(container.getId(), fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        Fragment fragment = (Fragment) object;

        if (this.curTransaction == null)
            this.curTransaction = this.fragmentManager.beginTransaction();

        boolean alive = this.fragments.contains(fragment);
        if (alive)
        {
            this.fragments.set(position, null);
            this.savedStates.set(position, this.fragmentManager.saveFragmentInstanceState(fragment));
        }

        curTransaction.remove(fragment);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object)
    {
        Fragment fragment = (Fragment) object;
        if (fragment != primaryItem)
        {
            if (primaryItem != null)
            {
                primaryItem.setMenuVisibility(false);
                primaryItem.setUserVisibleHint(false);
            }
            if (fragment != null)
            {
                fragment.setMenuVisibility(true);
                fragment.setUserVisibleHint(true);
            }
            primaryItem = fragment;
        }
    }

    @Override
    public int getItemPosition(Object object)
    {
        int index = this.fragments.indexOf(object);
        return index >= 0 ? index : PagerAdapter.POSITION_NONE;
    }

    @Override
    public int getCount()
    {
        return this.fragments.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return ((Fragment) object).getView() == view;
    }

    protected abstract Fragment getFragment(int position);

    public void pushFragmentStack()
    {
        this.fragments.add(0, null);
        this.savedStates.add(0, null);
    }

    public void popFragmentStack(int count)
    {
        for (int i = 0; i < count; i++)
        {
            this.fragments.remove(0);
            this.savedStates.remove(0);
        }
    }
}