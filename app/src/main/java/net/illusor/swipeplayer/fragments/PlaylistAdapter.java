package net.illusor.swipeplayer.fragments;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.widgets.PlaylistItemView;

import java.util.List;

class PlaylistAdapter extends ArrayAdapter<AudioFile>
{
    private List<AudioFile> data;

    PlaylistAdapter(Context context, List<AudioFile> data)
    {
        super(context, 0, data);
        this.data = data;
    }

    @Override
    public long getItemId(int position)
    {
        return this.getItem(position).hashCode();
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

    List<AudioFile> getData()
    {
        return data;
    }
}
