package net.illusor.swipeplayer.fragments;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.PlaybackMode;
import net.illusor.swipeplayer.domain.RepeatMode;
import net.illusor.swipeplayer.helpers.PreferencesHelper;
import net.illusor.swipeplayer.services.PlaybackStrategy;
import net.illusor.swipeplayer.services.RandomPlaybackStrategy;
import net.illusor.swipeplayer.services.SequentialPlaybackStrategy;
import net.illusor.swipeplayer.services.SoundService;

public class PlaylistOptionsFragment extends Fragment implements View.OnClickListener
{
    private final SoundServiceConnection connection = new SoundServiceConnection();
    private PlaybackMode playbackMode;
    private RepeatMode repeatMode;

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
        this.playbackMode = PreferencesHelper.getPlaybackMode(context);
        this.repeatMode = PreferencesHelper.getRepeatMode(context);

        btnShuffle.setChecked(this.playbackMode == PlaybackMode.Random);
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
                break;
            }
            case R.id.id_playlist_shuffle:
            {
                this.playbackMode = (this.playbackMode == PlaybackMode.Sequential) ? PlaybackMode.Random : PlaybackMode.Sequential;
                PreferencesHelper.setPlaybackMode(this.getActivity(), this.playbackMode);
                this.updatePlaybackStrategy();
                break;
            }
        }
    }

    void updatePlaybackStrategy()
    {
        PlaybackStrategy strategy = (this.playbackMode == PlaybackMode.Sequential) ? new SequentialPlaybackStrategy() : new RandomPlaybackStrategy();
        this.connection.service.setPlaybackStrategy(strategy);
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
            updatePlaybackStrategy();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            this.service = null;
        }
    }
}
