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

package net.illusor.swipeplayer.widgets;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.LinearLayout;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.helpers.TimeFormatter;

@TargetApi(16)
public class PlaylistItemView extends LinearLayout implements Checkable
{
    private FormattedTextView title, duration;
    private AudioFile audioFile;
    private boolean isChecked;

    public PlaylistItemView(Context context)
    {
        this(context, null);
    }

    public PlaylistItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        Drawable bg = context.getResources().getDrawable(R.drawable.item_playlist_bg);
        if (Build.VERSION.SDK_INT < 16)
            this.setBackgroundDrawable(bg);
        else
            this.setBackground(bg);

        LayoutInflater.from(context).inflate(R.layout.list_item_playlist, this);

        this.title = (FormattedTextView) this.findViewById(R.id.id_play_title);
        this.duration = (FormattedTextView) this.findViewById(R.id.id_play_duration);
    }

    public AudioFile getAudioFile()
    {
        return audioFile;
    }

    public void setAudioFile(AudioFile audioFile)
    {
        String format = TimeFormatter.hhmmss(audioFile.getDuration());
        this.audioFile = audioFile;
        this.title.setText(audioFile.getTitle());
        this.duration.setText(format);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace)
    {
        final int[] state = super.onCreateDrawableState(extraSpace + 1);
        if (this.isChecked)
            mergeDrawableStates(state, new int[]{android.R.attr.state_checked});
        return state;
    }

    //region Checkable

    @Override
    public void setChecked(boolean b)
    {
        this.isChecked = b;
        this.title.setChecked(b);
        this.duration.setChecked(b);
        this.refreshDrawableState();
    }

    @Override
    public boolean isChecked()
    {
        return this.isChecked;
    }

    @Override
    public void toggle()
    {
        this.setChecked(!this.isChecked);
    }

    //endregion
}