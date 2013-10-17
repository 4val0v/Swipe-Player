package net.illusor.swipeplayer.fragments;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FolderBrowserFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener, View.OnClickListener
{
    private Spinner listFolders;
    private FoldersAdapter foldersAdapter;
    private FilesAdapter filesAdapter;
    private File currentDirectory = Environment.getRootDirectory();
    private AudioLoaderCallbacks audioLoaderCallbacks = new AudioLoaderCallbacks();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.filesAdapter = new FilesAdapter(this.getActivity());
        this.foldersAdapter = new FoldersAdapter(this.getActivity());

        View view = inflater.inflate(R.layout.folder_browser_fragment, container, false);
        ListView listFiles = (ListView)view.findViewById(R.id.id_fb_list);
        listFiles.setAdapter(this.filesAdapter);
        listFiles.setOnItemClickListener(this);

        this.listFolders = (Spinner)view.findViewById(R.id.id_fb_folders);
        this.listFolders.setAdapter(this.foldersAdapter);
        this.listFolders.setOnItemSelectedListener(this);

        ImageButton btnBack = (ImageButton)view.findViewById(R.id.id_fb_up);
        btnBack.setOnClickListener(this);

        foldersAdapter.setFolder(currentDirectory);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Bundle args = new Bundle();
        args.putSerializable(AudioLoaderCallbacks.ARGS_DIRECTORY, this.currentDirectory);
        this.getLoaderManager().initLoader(0, args, this.audioLoaderCallbacks);
    }

    //region OnItemClickListener

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        File selected = (File)adapterView.getItemAtPosition(i);
        if (selected.isDirectory())
            foldersAdapter.setFolder(selected);
    }

    //endregion

    //region OnClickListener

    @Override
    public void onClick(View view)
    {
        if (view.getId() == R.id.id_fb_up && this.currentDirectory.getParentFile() != null)
            this.directoryOpen(this.currentDirectory.getParentFile());
    }

    //endregion

    //region OnItemSelectedListener

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        File selected = (File)adapterView.getSelectedItem();
        this.directoryOpen(selected);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    //endregion

    private void directoryOpen(File folder)
    {
        this.currentDirectory = folder;

        Bundle args = new Bundle();
        args.putSerializable(AudioLoaderCallbacks.ARGS_DIRECTORY, this.currentDirectory);
        this.getLoaderManager().restartLoader(0, args, this.audioLoaderCallbacks);
    }

    private class FilesAdapter extends ArrayAdapter<AudioFile>
    {
        private LayoutInflater inflater;

        private FilesAdapter(Context context)
        {
            super(context, 0);
            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view;

            if (convertView != null)
                view = convertView;
            else
                view = this.inflater.inflate(R.layout.list_item_file, null);

            File item = this.getItem(position);

            TextView text = (TextView)view.findViewById(R.id.id_file_name);
            text.setText(item.getName());

            ImageView image = (ImageView)view.findViewById(R.id.id_file_icon);
            if (item.isDirectory())
                image.setVisibility(View.VISIBLE);
            else
                image.setVisibility(View.INVISIBLE);

            return view;
        }
    }

    private class FoldersAdapter extends ArrayAdapter<File>
    {
        private FoldersAdapter(Context context)
        {
            super(context, R.layout.list_item_folder, 0);
        }

        private void setFolder(File folder)
        {
            this.clear();

            while (folder != null)
            {
                this.insert(folder, 0);
                folder = folder.getParentFile();
            }

            listFolders.setSelection(foldersAdapter.getCount());
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            TextView view = (TextView)super.getView(position, convertView, parent);
            this.formatTextView(view, position);
            return view;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent)
        {
            TextView view = (TextView)super.getDropDownView(position, convertView, parent);
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
        private static final String ARGS_DIRECTORY = "directory";

        @Override
        public Loader<List<AudioFile>> onCreateLoader(int i, Bundle bundle)
        {
            File directory = (File)bundle.getSerializable(ARGS_DIRECTORY);
            return new AudioLoader(getActivity(), directory);
        }

        @Override
        public void onLoadFinished(Loader<List<AudioFile>> listLoader, List<AudioFile> audioFiles)
        {
            filesAdapter.clear();
            for (AudioFile file : audioFiles)
                filesAdapter.add(file);
        }

        @Override
        public void onLoaderReset(Loader<List<AudioFile>> listLoader)
        {
            filesAdapter.clear();
        }
    }


}
