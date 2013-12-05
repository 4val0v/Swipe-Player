package net.illusor.swipeplayer.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import net.illusor.swipeplayer.domain.AudioFile;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class TrackPagerAdapter extends PagerAdapter
{
    private final List<AudioFile> audioFiles;
    private final Dictionary<Integer, TrackFragment> fragments = new Hashtable<>();
    private final FragmentManager fragmentManager;
    private FragmentTransaction currentTransaction;

    public TrackPagerAdapter(List<AudioFile> audioFiles, FragmentManager fragmentManager)
    {
        //adapter should support cyclic scrolling, so if we have enough items to cycle (at least 2)
        //we add 2 fake null items at the begining and at the end of the playlist
        if (audioFiles.size() > 1)
        {
            this.audioFiles = new ArrayList<>(audioFiles.size() + 2);
            this.audioFiles.add(null);
            this.audioFiles.addAll(1, audioFiles);
            this.audioFiles.add(null);
        }
        else
        {
            //if we have not enough items for cycling, we just use the provided playlist
            this.audioFiles = audioFiles;
        }
        this.fragmentManager = fragmentManager;
    }

    @Override
    public void finishUpdate(ViewGroup container)
    {
        super.finishUpdate(container);
        if (this.currentTransaction != null)
        {
            this.currentTransaction.commitAllowingStateLoss();
            this.currentTransaction = null;
            this.fragmentManager.executePendingTransactions();
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int fragmentPosition)
    {
        //if we are going to cycle adapter content, we should adjust audioFile index
        int filePosition = this.coerceCyclicPosition(fragmentPosition);

        TrackFragment fragment = this.fragments.get(fragmentPosition);
        if (fragment != null)
            return fragment;

        if (this.currentTransaction == null)
            this.currentTransaction = this.fragmentManager.beginTransaction();

        AudioFile audioFile = this.audioFiles.get(filePosition);
        fragment = TrackFragment.newInstance(audioFile);
        this.fragments.put(fragmentPosition, fragment);

        this.currentTransaction.add(container.getId(), fragment);

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        this.fragments.remove(position);

        if (this.currentTransaction == null)
            this.currentTransaction = this.fragmentManager.beginTransaction();

        TrackFragment fragment = (TrackFragment)object;
        this.currentTransaction.remove(fragment);
    }

    @Override
    public int getCount()
    {
        return this.audioFiles.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object)
    {
        return ((Fragment)object).getView() == view;
    }

    public List<AudioFile> getData()
    {
        return this.audioFiles;
    }

    private int coerceCyclicPosition(int position)
    {
        if (this.audioFiles.size() > 1)
        {
            //if we ViewPager requests first or last elements of the playlist, which are "virtual",
            // we return an "opposite" item from another size of the playlist; see code of
            if (position == 0)
                return this.getCount() - 2;
            else if (position == this.getCount() - 1)
                return 1;
        }

        return position;
    }
}
