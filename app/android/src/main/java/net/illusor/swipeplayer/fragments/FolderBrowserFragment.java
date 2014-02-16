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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.*;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.activities.SwipeActivity;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.domain.AudioPlaylist;
import net.illusor.swipeplayer.helpers.OverScrollHelper;
import net.illusor.swipeplayer.widgets.FolderItemView;

import java.io.File;
import java.util.List;

/**
 * Displays contents of music folder
 */
public class FolderBrowserFragment extends Fragment implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener
{
    //region Factory

    private static final String PARAM_FOLDER = "folder";

    /**
     * Creates a new instance of {@link FolderBrowserFragment}
     * @param folder Folder to browse
     * @return Created fragment
     */
    public static FolderBrowserFragment newInstance(File folder)
    {
        Bundle args = new Bundle();
        args.putSerializable(PARAM_FOLDER, folder);
        FolderBrowserFragment fragment = new FolderBrowserFragment();
        fragment.setArguments(args);
        return fragment;
    }

    //endregion

    private Spinner navigationHistory;//dropdown with the history of navigation
    private ListView listAudioFiles;//list of audio folders into the current directory

    private File currentDirectory;//directory being browsed
    private final AudioLoaderCallbacks audioLoaderCallbacks = new AudioLoaderCallbacks();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_browser, container, false);
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
        this.currentDirectory = (File)this.getArguments().getSerializable(PARAM_FOLDER);

        OverScrollHelper.overScrollDisable(this.listAudioFiles);
        this.registerForContextMenu(this.listAudioFiles);
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

        //called when current fragment is brought into screen by owning ViewPager
        //here we update the navigationHistory dropdown box
        if (isVisibleToUser && this.navigationHistory != null)
        {
            List<File> navigationItems = this.getSwipeActivity().getBrowserHistory();
            NavigationHistoryAdapter adapter = new NavigationHistoryAdapter(this.getActivity(), navigationItems);

            this.navigationHistory.setAdapter(adapter);
            this.navigationHistory.setSelection(navigationItems.indexOf(this.currentDirectory));

            this.listAudioFiles.invalidateViews();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        menu.add(0, 0, 0, R.string.str_directory_play);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item)
    {
        if (!this.getUserVisibleHint())
            return false;

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        AudioFile audioFile = (AudioFile)this.listAudioFiles.getItemAtPosition(info.position);
        this.getSwipeActivity().playMediaDirectory(audioFile);
        return true;
    }

    //region OnItemClickListener

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        //if selected directory has subfolders with music - navigate there
        //if not - inflate the playlist with the contents of the directory
        AudioFile selected = (AudioFile) adapterView.getItemAtPosition(i);
        if (selected.hasSubDirectories())
            this.getSwipeActivity().openMediaDirectory(selected);
        else
            this.getSwipeActivity().playMediaDirectory(selected);
    }

    //endregion

    //region OnItemSelectedListener

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l)
    {
        NavigationHistoryAdapter adapter = (NavigationHistoryAdapter)adapterView.getAdapter();
        if (this.currentDirectory == adapter.getData().get(i))
            return;

        Log.d("SWIPE", this.currentDirectory.toString() + " " + i);
        File selected = (File) adapterView.getItemAtPosition(i);
        this.getSwipeActivity().openMediaDirectory(selected);

        this.navigationHistory.setSelection(adapter.getData().indexOf(this.currentDirectory));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    //endregion

    /**
     * Show/Hide "Loading" indicator while loading the contents of the fragment
     * @param show <b>true</b> to show, <b>false</b> to hide
     */
    private void showLoadingIndicator(boolean show)
    {
        this.getView().findViewById(R.id.id_list_preloader).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    /**
     * Gets the parent activity
     * @return Parent activity
     */
    private SwipeActivity getSwipeActivity()
    {
        return (SwipeActivity)this.getActivity();
    }

    /**
     * Manages the content of the fragment
     */
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

            File playlist = getSwipeActivity().getCurrentMediaDirectory();
            AudioFile item = this.getItem(position);

            //check if this folder is one of the current playlist`s ancestors
            boolean isSelected = playlist != null && playlist.getAbsolutePath().startsWith(item.getAbsolutePath());
            view.setSelected(isSelected);
            view.setText(item.getTitle());

            return view;
        }
    }

    /**
     * Manages loading of the fragment contents
     */
    private class AudioLoaderCallbacks implements LoaderManager.LoaderCallbacks<AudioPlaylist>
    {
        private static final String ARGS_DIRECTORY = "folder";

        @Override
        public Loader<AudioPlaylist> onCreateLoader(int i, Bundle bundle)
        {
            showLoadingIndicator(true);
            File directory = (File) bundle.getSerializable(ARGS_DIRECTORY);
            return new AudioFoldersLoader(getActivity(), directory);
        }

        @Override
        public void onLoadFinished(Loader<AudioPlaylist> listLoader, AudioPlaylist playlist)
        {
            showLoadingIndicator(false);
            listAudioFiles.setAdapter(new AudioFilesAdapter(getActivity(), playlist.getAudioFiles()));
        }

        @Override
        public void onLoaderReset(Loader<AudioPlaylist> listLoader)
        {
            listAudioFiles.setAdapter(null);
        }

        public void initLoader()
        {
            Bundle args = new Bundle();
            args.putSerializable(AudioLoaderCallbacks.ARGS_DIRECTORY, currentDirectory);
            getLoaderManager().initLoader(0, args, this);
        }

        public void quitLoader()
        {
            getLoaderManager().destroyLoader(0);
        }
    }
}