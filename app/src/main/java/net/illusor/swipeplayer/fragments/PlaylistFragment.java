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
import android.widget.ListView;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.activities.SwipeActivity;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.helpers.PreferencesHelper;
import net.illusor.swipeplayer.services.AudioBroadcastHandler;
import net.illusor.swipeplayer.services.SoundService;
import net.illusor.swipeplayer.widgets.PlaylistItemView;

import java.io.File;
import java.util.List;

public class PlaylistFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private ListView listView;
    private File currentMediaDirectory;
    private AudioControlFragment audioControlFragment;
    private final AudioLoaderCallbacks audioLoaderCallbacks = new AudioLoaderCallbacks();
    private final SoundServiceConnection connection = new SoundServiceConnection();
    private final SoundServiceReceiver receiver = new SoundServiceReceiver();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_playlist, container, false);
        this.listView = (ListView)view.findViewById(R.id.id_playlist);
        this.audioControlFragment = (AudioControlFragment)this.getActivity().getSupportFragmentManager().findFragmentById(R.id.id_audio_control);
        return view;
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
        this.connection.bind();
        this.receiver.register();

        this.currentMediaDirectory = PreferencesHelper.getStoredPlaylist(this.getActivity());

        if (this.currentMediaDirectory != null)
            this.audioLoaderCallbacks.initLoader(this.currentMediaDirectory);
        else
            this.showFolderButton(true);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.connection.unbind();
        this.receiver.unregister();
        this.audioLoaderCallbacks.quitLoader();

        PreferencesHelper.setStoredPlaylist(this.getActivity(), this.currentMediaDirectory);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        PlaylistItemView selectedItem = (PlaylistItemView) view;
        AudioFile audioFile = selectedItem.getAudioFile();
        this.connection.service.play(audioFile);
    }

    public void setMediaDirectory(File folder)
    {
        this.currentMediaDirectory = folder;
        this.audioLoaderCallbacks.restartLoader(folder);
    }

    private void setItemChecked(AudioFile audioFile)
    {
        final PlaylistAdapter adapter = (PlaylistAdapter)this.listView.getAdapter();
        final int index = adapter.getData().indexOf(audioFile);
        if (index >= 0)
        {
            this.listView.setItemChecked(index, true);

            int pos = this.listView.getFirstVisiblePosition();
            if ((Math.abs(pos - index)) < 20)
                this.listView.smoothScrollToPosition(index);
            else
                this.listView.setSelection(index);
        }
    }

    private void showLoadingIndicator(boolean show)
    {
        this.getView().findViewById(R.id.id_list_preloader).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showFolderButton(boolean show)
    {
        this.getView().findViewById(R.id.id_playlist_folder).setVisibility(show ? View.VISIBLE : View.GONE);
        if (show)
        {
            this.getView().findViewById(R.id.id_playlist_folder_btn).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    getSwipeActivity().openMediaBrowser();
                }
            });
        }
    }

    private SwipeActivity getSwipeActivity()
    {
        return (SwipeActivity)this.getActivity();
    }

    private class AudioLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<AudioFile>>
    {
        private static final String ARGS_DIRECTORY = "folder";

        @Override
        public Loader<List<AudioFile>> onCreateLoader(int i, Bundle bundle)
        {
            listView.setAdapter(null);

            showLoadingIndicator(true);
            showFolderButton(false);

            File directory = (File) bundle.getSerializable(ARGS_DIRECTORY);
            return new AudioFilesLoader(getActivity(), directory);
        }

        @Override
        public void onLoadFinished(Loader<List<AudioFile>> listLoader, List<AudioFile> audioFiles)
        {
            if (audioFiles.size() == 0)
                showFolderButton(true);

            showLoadingIndicator(false);

            listView.setAdapter(new PlaylistAdapter(getActivity(), audioFiles));
            audioControlFragment.setPlaylist(audioFiles);

            //we do not know, what fires faster: music loader or service connection
            //so we duplicate service playlist inflation code here and inside the service connection
            if (connection.service != null)
            {
                connection.service.setPlaylist(audioFiles);

                AudioFile audioFile = connection.service.getAudioFile();
                if (audioFile != null) setItemChecked(audioFile);
            }
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

            //we do not know, what fires faster: music loader or service connection
            //so we duplicate service playlist inflation code here and inside the music loader
            if (listView.getAdapter() != null)
            {
                PlaylistAdapter adapter = (PlaylistAdapter)listView.getAdapter();
                service.setPlaylist(adapter.getData());

                AudioFile audioFile = connection.service.getAudioFile();
                if (audioFile != null) setItemChecked(audioFile);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            this.service = null;
        }
    }

    private class SoundServiceReceiver extends AudioBroadcastHandler
    {
        @Override
        protected void onPlayAudioFile(AudioFile audioFile)
        {
            super.onPlayAudioFile(audioFile);
            setItemChecked(audioFile);
        }

        @Override
        protected Context getClassContext()
        {
            return getActivity();
        }
    }
}
