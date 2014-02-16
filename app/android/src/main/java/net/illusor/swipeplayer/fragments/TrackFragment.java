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
public class TrackFragment extends Fragment
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
        title2.setText(audioFile.getArtist());
    }
}
