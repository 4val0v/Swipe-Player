package net.illusor.swipeplayer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.widgets.FormattedTextView;

public class TrackFragment extends Fragment
{
    //region Factory

    private static final String KEY_ARGS = "args";

    public static TrackFragment newInstance(AudioFile audioFile)
    {
        Bundle bundle = new Bundle();
        bundle.putSerializable(KEY_ARGS, audioFile);
        TrackFragment fragment = new TrackFragment();
        fragment.setArguments(bundle);
        return fragment;
    }

    //endregion

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_track, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        FormattedTextView title1 = (FormattedTextView)this.getView().findViewById(R.id.id_audio_control_title1);
        FormattedTextView title2 = (FormattedTextView)this.getView().findViewById(R.id.id_audio_control_artist);

        AudioFile audioFile = (AudioFile)this.getArguments().getSerializable(KEY_ARGS);

        title1.setText(audioFile.getTitle());

        if (!audioFile.getArtist().equals("<unknown>"))
        {
            title2.setText(audioFile.getArtist());
            title2.setVisibility(View.VISIBLE);
        }
        else
        {
            title2.setVisibility(View.GONE);
        }
    }
}
