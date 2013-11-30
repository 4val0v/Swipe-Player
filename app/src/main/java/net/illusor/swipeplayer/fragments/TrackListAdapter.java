package net.illusor.swipeplayer.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import net.illusor.swipeplayer.domain.AudioFile;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

public class TrackListAdapter extends PagerAdapter
{
    private final List<AudioFile> audioFiles;
    private final Dictionary<AudioFile, TrackFragment> fragments = new Hashtable<>();
    private final FragmentManager fragmentManager;
    private FragmentTransaction currentTransaction;

    public TrackListAdapter(List<AudioFile> audioFiles, FragmentManager fragmentManager)
    {
        this.audioFiles = audioFiles;
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
    public Object instantiateItem(ViewGroup container, int position)
    {
        AudioFile audioFile = this.audioFiles.get(position);
        TrackFragment fragment = this.fragments.get(audioFile);
        if (fragment != null)
            return fragment;

        if (this.currentTransaction == null)
            this.currentTransaction = this.fragmentManager.beginTransaction();

        fragment = TrackFragment.newInstance(audioFile);
        this.fragments.put(audioFile, fragment);

        this.currentTransaction.add(container.getId(), fragment);

        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object)
    {
        AudioFile audioFile = this.audioFiles.get(position);
        this.fragments.remove(audioFile);

        TrackFragment fragment = (TrackFragment)object;

        if (this.currentTransaction == null)
            this.currentTransaction = this.fragmentManager.beginTransaction();

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
}
