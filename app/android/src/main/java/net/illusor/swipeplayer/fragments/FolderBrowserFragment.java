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
import net.illusor.swipeplayer.helpers.OverScrollHelper;
import net.illusor.swipeplayer.widgets.FormattedTextView;

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
        this.currentFolder = (File)this.getArguments().getSerializable(PARAM_FOLDER);

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

        if (isVisibleToUser && this.navigationHistory != null)
        {
            List<File> navigationItems = this.getSwipeActivity().getBrowserHistory();
            NavigationHistoryAdapter adapter = new NavigationHistoryAdapter(this.getActivity(), navigationItems);

            this.navigationHistory.setAdapter(adapter);
            this.navigationHistory.setSelection(navigationItems.indexOf(this.currentFolder));

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
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        AudioFile audioFile = (AudioFile)this.listAudioFiles.getItemAtPosition(info.position);
        this.getSwipeActivity().playMediaDirectory(audioFile);
        return true;
    }

    //region OnItemClickListener

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
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
        if (this.currentFolder == adapter.getData().get(i))
            return;

        Log.d("SWIPE", this.currentFolder.toString() + " " + i);
        File selected = (File) adapterView.getItemAtPosition(i);
        this.getSwipeActivity().openMediaDirectory(selected);

        this.navigationHistory.setSelection(adapter.getData().indexOf(this.currentFolder));
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView)
    {

    }

    //endregion

    private void showLoadingIndicator(boolean show)
    {
        this.getView().findViewById(R.id.id_list_preloader).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private SwipeActivity getSwipeActivity()
    {
        return (SwipeActivity)this.getActivity();
    }

    private class AudioFilesAdapter extends ArrayAdapter<AudioFile>
    {
        private int defaultColor, highlightColor;

        private AudioFilesAdapter(Context context, List<AudioFile> files)
        {
            super(context, 0, files);
            this.defaultColor = context.getResources().getColor(R.color.color_folder_text_default);
            this.highlightColor = context.getResources().getColor(R.color.color_folder_text_highlight);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View view;

            if (convertView != null)
                view = convertView;
            else
                view = LayoutInflater.from(this.getContext()).inflate(R.layout.list_item_folder, null);

            File playlist = getSwipeActivity().getCurrentMediaDirectory();
            AudioFile item = this.getItem(position);
            FormattedTextView text = (FormattedTextView)view.findViewById(R.id.id_file_title);
            text.setColor(playlist != null && playlist.getAbsolutePath().startsWith(item.getAbsolutePath()) ? this.highlightColor : this.defaultColor);
            text.setText(item.getTitle());

            return view;
        }
    }

    private class AudioLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<AudioFile>>
    {
        private static final String ARGS_DIRECTORY = "folder";

        @Override
        public Loader<List<AudioFile>> onCreateLoader(int i, Bundle bundle)
        {
            showLoadingIndicator(true);
            File directory = (File) bundle.getSerializable(ARGS_DIRECTORY);
            return new AudioFoldersLoader(getActivity(), directory);
        }

        @Override
        public void onLoadFinished(Loader<List<AudioFile>> listLoader, List<AudioFile> audioFiles)
        {
            showLoadingIndicator(false);
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
