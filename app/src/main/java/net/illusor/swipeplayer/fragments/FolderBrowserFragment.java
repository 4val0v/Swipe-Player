package net.illusor.swipeplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.activities.SwipeActivity;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.widgets.ListItemFolder;

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
    private AudioLoaderCallbacks audioLoaderCallbacks = new AudioLoaderCallbacks();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.folder_browser_fragment, container, false);
        this.listAudioFiles = (ListView) view.findViewById(R.id.id_fb_audio_files);
        this.listAudioFiles.setOnItemClickListener(this);

        this.navigationHistory = (Spinner) view.findViewById(R.id.id_fb_nav_history);
        this.currentFolder = (File)this.getArguments().getSerializable(PARAM_FOLDER);

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        List<File> navigationItems = ((SwipeActivity)getActivity()).getNavigationHistory();
        NavigationAdapter adapter = new NavigationAdapter(this.getActivity(), navigationItems);

        this.navigationHistory.setAdapter(adapter);
        this.navigationHistory.setSelection(navigationItems.indexOf(this.currentFolder));
        this.navigationHistory.setOnItemSelectedListener(this);

        this.audioLoaderCallbacks.initLoader();
    }

    @Override
    public void onStop()
    {
        super.onStop();

        this.navigationHistory.setOnItemSelectedListener(null);
        this.navigationHistory.setAdapter(null);

        this.audioLoaderCallbacks.quitLoader();
    }

    //region OnItemClickListener

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        File selected = (File) adapterView.getItemAtPosition(i);
        ((SwipeActivity)this.getActivity()).directoryOpen(selected);
    }

    //endregion

    //region OnItemSelectedListener

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        /*File selected = (File) adapterView.getItemAtPosition(i);
        ((SwipeActivity) this.getActivity()).directoryOpen(selected);*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    //endregion

    private class AudioFilesAdapter extends ArrayAdapter<AudioFile>
    {
        private AudioFilesAdapter(Context context)
        {
            super(context, 0);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ListItemFolder view;

            if (convertView != null)
                view = (ListItemFolder)convertView;
            else
                view = new ListItemFolder(this.getContext());

            AudioFile item = this.getItem(position);

            view.setTitle(item.getTitle());
            view.setIsFolder(item.isDirectory());
            view.setHasPlaylistFiles(false);

            return view;
        }
    }

    private class NavigationAdapter extends ArrayAdapter<File>
    {
        private NavigationAdapter(Context context, List<File> navigationHistory)
        {
            super(context, R.layout.list_item_nav_history, 0, navigationHistory);
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
            view.setText(item.getName());
        }
    }

    private class AudioLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<AudioFile>>
    {
        private static final String ARGS_DIRECTORY = "folder";

        private AudioFilesAdapter audioFilesAdapter;

        public Loader<List<AudioFile>> onCreateLoader(int i, Bundle bundle)
        {
            this.audioFilesAdapter = new AudioFilesAdapter(getActivity());
            listAudioFiles.setAdapter(this.audioFilesAdapter);

            File directory = (File) bundle.getSerializable(ARGS_DIRECTORY);
            return new AudioLoader(getActivity(), directory);
        }

        @Override
        public void onLoadFinished(Loader<List<AudioFile>> listLoader, List<AudioFile> audioFiles)
        {
            audioFilesAdapter.clear();
            for (AudioFile file : audioFiles)
                audioFilesAdapter.add(file);
        }

        @Override
        public void onLoaderReset(Loader<List<AudioFile>> listLoader)
        {
            listAudioFiles.setAdapter(null);
            this.audioFilesAdapter = null;
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
