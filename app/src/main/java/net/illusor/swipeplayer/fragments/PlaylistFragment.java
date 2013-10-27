package net.illusor.swipeplayer.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.activities.SwipeActivity;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.widgets.AudioControlView;
import net.illusor.swipeplayer.widgets.PlaylistItemView;

import java.util.ArrayList;
import java.util.List;

public class PlaylistFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        this.listView = (ListView) inflater.inflate(R.layout.playlist_fragment, container, false);
        return this.listView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        String[] playlist = new String[]{"Speak to Me", "Breathe", "On the Run", "Time", "The Great Gig in the Sky", "Money", "Us and Them", "Any Colour You Like", "Us and Them"};
        List<AudioFile> files = new ArrayList<>(playlist.length);
        for (String item : playlist)
        {
            AudioFile file = new AudioFile("file://root/qwerty", item, "Artist Name", 356000);
            files.add(file);
        }

        ArrayAdapter adapter = new PlaylistAdapter(this.getActivity(), files);
        this.listView.setAdapter(adapter);
        this.listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        PlaylistItemView selectedItem = (PlaylistItemView) view;
        this.getAudioControl().setAudioFile(selectedItem.getAudioFile());
        this.getAudioControl().setVisibility(View.VISIBLE);
    }

    private AudioControlView getAudioControl()
    {
        return ((SwipeActivity) this.getActivity()).getAudioControl();
    }

    private class PlaylistAdapter extends ArrayAdapter<AudioFile>
    {
        private PlaylistAdapter(Context context, List<AudioFile> data)
        {
            super(context, 0, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            PlaylistItemView view;

            if (convertView != null)
                view = (PlaylistItemView) convertView;
            else
                view = new PlaylistItemView(this.getContext());

            AudioFile file = this.getItem(position);
            view.setAudioFile(file);
            return view;
        }
    }
}
