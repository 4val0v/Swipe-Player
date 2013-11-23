package net.illusor.swipeplayer.fragments;

import android.app.Service;
import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
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
import net.illusor.swipeplayer.services.SoundService;
import net.illusor.swipeplayer.widgets.AudioControlView;
import net.illusor.swipeplayer.widgets.PlaylistItemView;

import java.io.File;
import java.util.List;

public class PlaylistFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private static final String SHARED_PREF_PLAYLIST_KEY = "net.illusor.swipeplayer.playlist";

    private ListView listView;
    private File currentAudioFolder;
    private final AudioLoaderCallbacks audioLoaderCallbacks = new AudioLoaderCallbacks();
    private final SoundServiceConnection connection = new SoundServiceConnection();
    private final SoundServiceReceiver receiver = new SoundServiceReceiver();

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
        this.connection.bind();
        this.receiver.register();

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
            this.audioLoaderCallbacks.initLoader(this.currentAudioFolder);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.getAudioControl().onStop();
        this.audioLoaderCallbacks.quitLoader();
        this.receiver.unRegister();
        this.connection.unbind();
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
        this.connection.service.play(audioFile);
    }

    public void setTargetFolder(File folder)
    {
        this.currentAudioFolder = folder;
        this.audioLoaderCallbacks.restartLoader(folder);
    }

    private void setItemChecked(AudioFile audioFile)
    {
        final PlaylistAdapter adapter = (PlaylistAdapter)listView.getAdapter();
        final int index = adapter.getData().indexOf(audioFile);
        if (index > 0)
        {
            listView.setItemChecked(index, true);
            listView.smoothScrollToPosition(index);
        }
    }

    private AudioControlView getAudioControl()
    {
        return ((SwipeActivity) this.getActivity()).getAudioControl();
    }

    private class PlaylistAdapter extends ArrayAdapter<AudioFile>
    {
        private List<AudioFile> data;

        private PlaylistAdapter(Context context, List<AudioFile> data)
        {
            super(context, 0, data);
            this.data = data;
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

        private List<AudioFile> getData()
        {
            return data;
        }
    }

    private class AudioLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<AudioFile>>
    {
        private static final String ARGS_DIRECTORY = "folder";

        @Override
        public Loader<List<AudioFile>> onCreateLoader(int i, Bundle bundle)
        {
            File directory = (File) bundle.getSerializable(ARGS_DIRECTORY);
            return new AudioLoader(getActivity(), directory);
        }

        @Override
        public void onLoadFinished(Loader<List<AudioFile>> listLoader, List<AudioFile> audioFiles)
        {
            listView.setAdapter(new PlaylistAdapter(getActivity(), audioFiles));

            connection.service.setPlaylist(audioFiles);
            final AudioFile audioFile = connection.service.getCurrentFile();
            if (audioFile != null)
                setItemChecked(audioFile);
        }

        @Override
        public void onLoaderReset(Loader<List<AudioFile>> listLoader)
        {
            listView.setAdapter(null);
        }

        public void initLoader(File directory)
        {
            Bundle args = this.getArgs(directory);
            getLoaderManager().initLoader(0, args, this);
        }

        public void restartLoader(File directory)
        {
            Bundle args = this.getArgs(directory);
            getLoaderManager().restartLoader(0, args, this);
        }

        public void quitLoader()
        {
            getLoaderManager().destroyLoader(0);
        }

        private Bundle getArgs(File directory)
        {
            Bundle args = new Bundle();
            args.putSerializable(AudioLoaderCallbacks.ARGS_DIRECTORY, directory);
            return args;
        }
    }

    private class SoundServiceConnection implements ServiceConnection
    {
        private SoundService.SoundServiceBinder service;

        public void bind()
        {
            Intent intent = new Intent(getActivity(), SoundService.class);
            getActivity().bindService(intent, this, Service.BIND_AUTO_CREATE);
        }

        public void unbind()
        {
            getActivity().unbindService(this);
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder)
        {
            this.service = (SoundService.SoundServiceBinder)binder;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            this.service = null;
        }
    }

    private class SoundServiceReceiver extends BroadcastReceiver
    {
        public void register()
        {
            IntentFilter filter = new IntentFilter(SoundService.ACTION_NEW_AUDIO);
            getActivity().registerReceiver(this, filter);
        }

        public void unRegister()
        {
            getActivity().unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction() == SoundService.ACTION_NEW_AUDIO)
            {
                final AudioFile audioFile = (AudioFile)intent.getSerializableExtra(SoundService.ACTION_NEW_AUDIO);
                setItemChecked(audioFile);
            }
        }
    }
}
