package net.illusor.swipeplayer.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.activities.SwipeActivity;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.widgets.AudioControlView;
import net.illusor.swipeplayer.widgets.PlaylistItemView;

import java.io.File;
import java.util.List;

public class PlaylistFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private static final String SHARED_PREF_PLAYLIST_KEY = "net.illusor.swipeplayer.playlist";

    private ListView listView;
    private File currentAudioFolder;
    private AudioLoaderCallbacks audioLoaderCallbacks = new AudioLoaderCallbacks();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.listView = (ListView) inflater.inflate(R.layout.playlist_fragment, container, false);
        return this.listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        this.listView.setOnItemClickListener(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.getAudioControl().onStart();

        if (this.currentAudioFolder == null)
        {
            SharedPreferences preferences = this.getActivity().getSharedPreferences(SHARED_PREF_PLAYLIST_KEY, Context.MODE_PRIVATE);
            if (preferences.contains(SHARED_PREF_PLAYLIST_KEY))
            {
                String path = preferences.getString(SHARED_PREF_PLAYLIST_KEY, "");
                this.currentAudioFolder = new File(path);
            }
        }

        if (this.currentAudioFolder != null)
            this.audioLoaderCallbacks.initLoader(this.currentAudioFolder, false);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.getAudioControl().onStop();
        this.audioLoaderCallbacks.quitLoader();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (this.currentAudioFolder != null)
        {
            SharedPreferences preferences = this.getActivity().getSharedPreferences(SHARED_PREF_PLAYLIST_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(SHARED_PREF_PLAYLIST_KEY, currentAudioFolder.getAbsolutePath());
            editor.commit();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        PlaylistItemView selectedItem = (PlaylistItemView) view;
        AudioFile audioFile = selectedItem.getAudioFile();
        this.getAudioControl().setAudioFile(audioFile);
        this.getAudioControl().setVisibility(View.VISIBLE);
        this.listView.setItemChecked(i, true);
    }

    public void setTargetFolder(File folder)
    {
        this.currentAudioFolder = folder;
        this.audioLoaderCallbacks.initLoader(folder, true);
    }

    private AudioControlView getAudioControl()
    {
        return ((SwipeActivity) this.getActivity()).getAudioControl();
    }

    private class PlaylistAdapter extends ArrayAdapter<AudioFile>
    {
        private PlaylistAdapter(Context context, List<AudioFile> data)
        {
            super(context, 0, data);
        }

        @Override
        public long getItemId(int position)
        {
            return this.getItem(position).hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            PlaylistItemView view;

            if (convertView != null)
                view = (PlaylistItemView) convertView;
            else
                view = new PlaylistItemView(this.getContext());

            AudioFile file = this.getItem(position);
            view.setAudioFile(file);
            return view;
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
            listView.setAdapter(new PlaylistAdapter(getActivity(), audioFiles));
            getAudioControl().setPlaylist(audioFiles);
        }

        @Override
        public void onLoaderReset(Loader<List<AudioFile>> listLoader)
        {
            listView.setAdapter(null);
            getAudioControl().setPlaylist(null);
        }

        public void initLoader(File directory, boolean restart)
        {
            Bundle args = new Bundle();
            args.putSerializable(AudioLoaderCallbacks.ARGS_DIRECTORY, directory);
            if (restart)
                getLoaderManager().restartLoader(0, args, this);
            else
                getLoaderManager().initLoader(0, args, this);
        }

        public void quitLoader()
        {
            getLoaderManager().destroyLoader(0);
        }
    }
}
