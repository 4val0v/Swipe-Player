/*Copyright 2013 Nikita Kobzev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

package net.illusor.swipeplayer.fragments;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
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
import net.illusor.swipeplayer.domain.AudioPlaylist;
import net.illusor.swipeplayer.helpers.OverScrollHelper;
import net.illusor.swipeplayer.helpers.PreferencesHelper;
import net.illusor.swipeplayer.services.AudioBroadcastHandler;
import net.illusor.swipeplayer.services.SoundService;
import net.illusor.swipeplayer.widgets.PlaylistItemView;

import java.io.File;
import java.util.List;

/**
 * Fragment displays the actual application playlist
 */
public class PlaylistFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private ListView listView;//playlist
    private File currentMediaDirectory;//directory to look audio files for in
    private AudioControlFragment audioControlFragment;//fragment used to display progress and info about playing track
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
        OverScrollHelper.overScrollDisable(this.listView);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.connection.bind();
        this.receiver.register();

        this.currentMediaDirectory = PreferencesHelper.getStoredPlaylist(this.getActivity());
        if (this.currentMediaDirectory == null)
            this.showEmptyPlaylistMessage(true);
        else
            this.audioLoaderCallbacks.initLoader(this.currentMediaDirectory);
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

    /**
     * Sets provided directory as the application playlist, and load music files from there
     * @param folder Directory to treat as the playlist root
     */
    public void setMediaDirectory(File folder)
    {
        this.currentMediaDirectory = folder;
        this.audioLoaderCallbacks.restartLoader(folder);
    }

    /**
     * Mark playlist item as "Currently playing" and focus it
     * @param audioFile The item of the playlist that should be marked as checked
     */
    private void setItemChecked(AudioFile audioFile)
    {
        PlaylistAdapter adapter = (PlaylistAdapter)this.listView.getAdapter();
        int index = adapter.getData().getAudioFiles().indexOf(audioFile);
        if (index >= 0)
        {
            this.listView.setItemChecked(index, true);

            //if we have to scroll to much - just jump to the necessary item
            int pos = this.listView.getFirstVisiblePosition();
            if ((Math.abs(pos - index)) < 20)
                this.listView.smoothScrollToPosition(index);
            else
                this.listView.setSelection(index);
        }
    }

    /**
     * Show/Hide "loading" indicator
     * @param show <b>true</b> to show, <b>false</b> - to hide
     */
    private void showLoadingIndicator(boolean show)
    {
        this.getView().findViewById(R.id.id_list_preloader).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Displays message "Playlist is empty" and opens the folder browser
     * @param show <b>true</b> to show, <b>false</b> to hide
     */
    private void showEmptyPlaylistMessage(boolean show)
    {
        this.getView().findViewById(R.id.id_playlist_folder).setVisibility(show ? View.VISIBLE : View.GONE);
        if (show)
            getSwipeActivity().openMediaBrowser();
    }

    /**
     * Gets parent activity
     * @return Parent activity
     */
    private SwipeActivity getSwipeActivity()
    {
        return (SwipeActivity)this.getActivity();
    }

    /**
     * Inflates the Sound Service playlist with the actual data and scrolls the visible playlist to the currently playing audio file
     * @param playlist Actual set of audio files
     */
    private void synchronizePlaylist(AudioPlaylist playlist)
    {
        this.connection.service.setPlaylist(playlist);

        List<AudioFile> audioFiles = playlist.getAudioFiles();
        int index = audioFiles.indexOf(this.getSwipeActivity().PLAYBACK_ON_LOAD);
        if (index >= 0)
        {
            AudioFile audioFile = audioFiles.get(index);
            this.connection.service.play(audioFile);
        }
        else
        {
            AudioFile audioFile = this.connection.service.getAudioFile();
            if (audioFile != null) setItemChecked(audioFile);
        }

        this.getSwipeActivity().PLAYBACK_ON_LOAD = null;
    }

    /**
     * Handles loading of music files into the playlist
     */
    private class AudioLoaderCallbacks implements LoaderManager.LoaderCallbacks<AudioPlaylist>
    {
        private static final String ARGS_DIRECTORY = "folder";

        @Override
        public Loader<AudioPlaylist> onCreateLoader(int i, Bundle bundle)
        {
            listView.setAdapter(null);

            showLoadingIndicator(true);
            showEmptyPlaylistMessage(false);

            File directory = (File) bundle.getSerializable(ARGS_DIRECTORY);
            return new AudioFilesLoader(getActivity(), directory);
        }

        @Override
        public void onLoadFinished(Loader<AudioPlaylist> listLoader, AudioPlaylist playlist)
        {
            if (playlist.getAudioFiles().size() == 0)
                showEmptyPlaylistMessage(true);

            showLoadingIndicator(false);

            listView.setAdapter(new PlaylistAdapter(getActivity(), playlist));
            audioControlFragment.setPlaylist(playlist);

            //we do not know, what fires faster: music loader or service connection
            //so we duplicate service playlist inflation code here and inside the service connection
            if (connection.service != null)
                synchronizePlaylist(playlist);
        }

        @Override
        public void onLoaderReset(Loader<AudioPlaylist> listLoader)
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

    /**
     * Handles interrogation with the sound service
     */
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
                synchronizePlaylist(adapter.getData());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            this.service = null;
        }
    }

    /**
     * Receives messages from the sound service
     */
    private class SoundServiceReceiver extends AudioBroadcastHandler
    {
        @Override
        protected void onPlayAudioFile(AudioFile audioFile)
        {
            //in some cases (when this method gets called by synchronizePlaylist()) calls chain,
            //setItemChecked() doesn't scroll the listView to selected item
            //to fix it we use a delayed call, though we do not need it when this method gets
            //called from somewhere outside synchronizePlaylist()
            final AudioFile file = audioFile;
            listView.postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    setItemChecked(file);
                }
            }, 50);
        }
    }
}
