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
import net.illusor.swipeplayer.widgets.ListItemPlaylist;

public class PlaylistFragment extends Fragment implements AdapterView.OnItemClickListener
{
    private ListView listView;
    private ListItemPlaylist selectedItem;

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
        ArrayAdapter adapter = new PlaylistAdapter(this.getActivity(), playlist);
        this.listView.setAdapter(adapter);
        this.listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
    {
        if (this.selectedItem != null)
            this.selectedItem.setPlaying(false);

        this.selectedItem = (ListItemPlaylist)view;
        this.selectedItem.setPlaying(true);
    }

    private class PlaylistAdapter extends ArrayAdapter<String>
    {
        private PlaylistAdapter(Context context, String[] data)
        {
            super(context, 0, data);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            ListItemPlaylist view;

            if (convertView != null)
                view = (ListItemPlaylist)convertView;
            else
                view = new ListItemPlaylist(this.getContext());

            view.setTitle(this.getItem(position));
            return view;
        }
    }
}
