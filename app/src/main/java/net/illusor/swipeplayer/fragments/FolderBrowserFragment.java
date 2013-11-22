package net.illusor.swipeplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.activities.SwipeActivity;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.widgets.FolderItemView;

import java.io.File;
import java.util.List;

public class FolderBrowserFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener
{
    //region Factory

    private static final String PARAM_FOLDER = "folder";

    public static FolderBrowserFragment newInstance(File folder)
    {
        Bundle args = new Bundle();
        args.putSerializable(PARAM_FOLDER, folder);
        FolderBrowserFragment fragment = new FolderBrowserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //endregion

    private Spinner navigationHistory;
    private ListView listAudioFiles;

    private File currentFolder;
    private final AudioLoaderCallbacks audioLoaderCallbacks = new AudioLoaderCallbacks();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.folder_browser_fragment, container, false);
        this.listAudioFiles = (ListView) view.findViewById(R.id.id_fb_audio_files);
        this.navigationHistory = (Spinner) view.findViewById(R.id.id_fb_nav_history);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        this.navigationHistory.setOnItemSelectedListener(this);
        this.listAudioFiles.setOnItemClickListener(this);
        this.currentFolder = (File)this.getArguments().getSerializable(PARAM_FOLDER);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.audioLoaderCallbacks.initLoader();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.audioLoaderCallbacks.quitLoader();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser)
    {
        super.setUserVisibleHint(isVisibleToUser);

        if (isVisibleToUser && this.navigationHistory != null)
        {
            List<File> navigationItems = this.getSwipeActivity().getNavigationHistory();
            NavigationHistoryAdapter adapter = new NavigationHistoryAdapter(this.getActivity(), navigationItems);

            this.navigationHistory.setAdapter(adapter);
            this.navigationHistory.setSelection(navigationItems.indexOf(this.currentFolder));
        }
    }

    //region OnItemClickListener

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        File selected = (File) adapterView.getItemAtPosition(i);
        if (selected.isDirectory())
            this.getSwipeActivity().directoryOpen(selected);
        else if (selected.isFile())
            this.getSwipeActivity().getPlaylistFragment().setTargetFolder(selected.getParentFile());
    }

    //endregion

    //region OnItemSelectedListener

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        NavigationHistoryAdapter adapter = (NavigationHistoryAdapter)this.navigationHistory.getAdapter();
        if (this.currentFolder == adapter.navigationHistory.get(i))
            return;

        Log.d("SWIPE", this.currentFolder.toString() + " " + i);
        File selected = (File) adapterView.getItemAtPosition(i);
        this.getSwipeActivity().directoryOpen(selected);

        this.navigationHistory.setSelection(adapter.navigationHistory.indexOf(this.currentFolder));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    //endregion

    private SwipeActivity getSwipeActivity()
    {
        return (SwipeActivity)this.getActivity();
    }

    private class AudioFilesAdapter extends ArrayAdapter<AudioFile>
    {
        private AudioFilesAdapter(Context context, List<AudioFile> files)
        {
            super(context, 0, files);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            FolderItemView view;

            if (convertView != null)
                view = (FolderItemView)convertView;
            else
                view = new FolderItemView(this.getContext());

            AudioFile item = this.getItem(position);

            view.setTitle(item.getTitle());
            view.setIsFolder(item.isDirectory());
            view.setHasPlaylistFiles(false);

            return view;
        }
    }

    private class NavigationHistoryAdapter extends ArrayAdapter<File>
    {
        private List<File> navigationHistory;

        private NavigationHistoryAdapter(Context context, List<File> navigationHistory)
        {
            super(context, R.layout.list_item_nav_history, 0, navigationHistory);
            this.navigationHistory = navigationHistory;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            TextView view = (TextView) super.getView(position, convertView, parent);
            this.formatTextView(view, position);
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            TextView view = (TextView) super.getDropDownView(position, convertView, parent);
            this.formatTextView(view, position);
            return view;
        }

        private void formatTextView(TextView view, int position)
        {
            File item = this.getItem(position);
            view.setText(item.getPath());
        }
    }

    private class AudioLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<AudioFile>>
    {
        private static final String ARGS_DIRECTORY = "folder";

        public Loader<List<AudioFile>> onCreateLoader(int i, Bundle bundle)
        {
            File directory = (File) bundle.getSerializable(ARGS_DIRECTORY);
            return new AudioLoader(getActivity(), directory);
        }

        @Override
        public void onLoadFinished(Loader<List<AudioFile>> listLoader, List<AudioFile> audioFiles)
        {
            listAudioFiles.setAdapter(new AudioFilesAdapter(getActivity(), audioFiles));
        }

        @Override
        public void onLoaderReset(Loader<List<AudioFile>> listLoader)
        {
            listAudioFiles.setAdapter(null);
        }

        public void initLoader()
        {
            Bundle args = new Bundle();
            args.putSerializable(AudioLoaderCallbacks.ARGS_DIRECTORY, currentFolder);
            getLoaderManager().initLoader(0, args, this);
        }

        public void quitLoader()
        {
            getLoaderManager().destroyLoader(0);
        }
    }
}
