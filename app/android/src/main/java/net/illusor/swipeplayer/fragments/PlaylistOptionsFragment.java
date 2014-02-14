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
import net.illusor.swipeplayer.services.SoundService;

public class PlaylistOptionsFragment extends Fragment implements View.OnClickListener
{
    private final SoundServiceConnection connection = new SoundServiceConnection();
    private ToggleButton btnShuffle, btnRepeat;
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

        this.btnShuffle = (ToggleButton)view.findViewById(R.id.id_playlist_shuffle);
        this.btnRepeat = (ToggleButton)view.findViewById(R.id.id_playlist_repeat);


        this.btnShuffle.setOnClickListener(this);
        this.btnRepeat.setOnClickListener(this);
    }

    @Override
    public void onStart()
    {
        super.onStart();
        this.connection.bind();

        Context context = this.getActivity();
        this.playbackMode = PreferencesHelper.getPlaybackMode(context);
        this.repeatMode = PreferencesHelper.getRepeatMode(context);
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
                break;
            }
            case R.id.id_playlist_shuffle:
            {
                break;
            }
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
}
