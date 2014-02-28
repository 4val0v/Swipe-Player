package net.illusor.swipeplayer.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.activities.SwipeActivity;
import net.illusor.swipeplayer.domain.RepeatMode;
import net.illusor.swipeplayer.helpers.PreferencesHelper;
import net.illusor.swipeplayer.services.SoundServiceConnection;

import java.io.File;

/**
 * Displays playlist options: "Shuffle" and "Repeat"
 */
public class PlaylistOptionsFragment extends Fragment implements View.OnClickListener
{
    private final SoundServiceConnection connection = new LocalServiceConnection(this);
    private RepeatMode repeatMode;//playback repeat mode: repeat/not repeat
    private int shuffleKey;//a key used for playlist pseudo random sorting

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_options, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        View view = this.getView();

        ToggleButton btnShuffle = (ToggleButton) view.findViewById(R.id.id_playlist_shuffle);
        ToggleButton btnRepeat = (ToggleButton) view.findViewById(R.id.id_playlist_repeat);

        btnShuffle.setOnClickListener(this);
        btnRepeat.setOnClickListener(this);

        Context context = this.getActivity();
        this.shuffleKey = PreferencesHelper.getShuffleKey(context);
        this.repeatMode = PreferencesHelper.getRepeatMode(context);

        btnShuffle.setChecked(this.shuffleKey != SwipeActivity.SHUFFLE_KEY_NOSHUFFLE);
        btnRepeat.setChecked(this.repeatMode == RepeatMode.Playlist);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.connection.bind();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.connection.unbind();
    }

    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.id_playlist_repeat:
            {
                this.repeatMode = (this.repeatMode == RepeatMode.None) ? RepeatMode.Playlist : RepeatMode.None;
                PreferencesHelper.setRepeatMode(this.getActivity(), this.repeatMode);
                this.connection.service.setRepeatMode(this.repeatMode);
                break;
            }
            case R.id.id_playlist_shuffle:
            {
                //check if we should shuffle the playlist
                this.shuffleKey = this.shuffleKey != SwipeActivity.SHUFFLE_KEY_NOSHUFFLE ? SwipeActivity.SHUFFLE_KEY_NOSHUFFLE : (int)(Math.random() * Integer.MAX_VALUE);

                //save the value - the PlaylistFragment will read it on playlist reload and act accordingly
                PreferencesHelper.setShuffleKey(this.getActivity(), this.shuffleKey);

                //reload the playlist
                SwipeActivity swipeActivity = (SwipeActivity)this.getActivity();
                File directory = swipeActivity.getCurrentMediaDirectory();
                if (directory != null && directory.exists())
                    swipeActivity.playMediaDirectory(directory);

                break;
            }
        }
    }

    private class LocalServiceConnection extends SoundServiceConnection
    {
        private final Fragment fragment;

        private LocalServiceConnection(Fragment fragment)
        {
            this.fragment = fragment;
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder)
        {
            super.onServiceConnected(componentName, binder);
            this.service.setRepeatMode(repeatMode);
        }

        @Override
        public Context getContext()
        {
            return this.fragment.getActivity();
        }
    }
}
