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
