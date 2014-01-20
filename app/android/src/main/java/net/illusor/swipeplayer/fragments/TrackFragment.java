package net.illusor.swipeplayer.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.widgets.FormattedTextView;

/**
 * Represents a music file info item of the {@link AudioControlFragment}
 */
class TrackFragment extends Fragment
{
    //region Factory

    private static final String KEY_ARGS = "args";

    /**
     * Creates a new instance of {@link TrackFragment}
     * @param audioFile Music file, represented by the fragment
     * @return Created fragment
     */
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

        if (!audioFile.getArtist().equals("<unknown>"))//when artist is unknown, it has text "<unknown>"; do not know, if android has some general resource for this string
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
