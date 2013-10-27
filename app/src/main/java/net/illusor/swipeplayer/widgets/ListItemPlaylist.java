package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.helpers.TimeFormatter;

public class ListItemPlaylist extends LinearLayout
{
    private TextView title, duration;

    public ListItemPlaylist(Context context)
    {
        this(context, null);
    }

    public ListItemPlaylist(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.list_item_playlist, this);
        this.title = (TextView)this.findViewById(R.id.id_play_title);
        this.duration = (TextView)this.findViewById(R.id.id_play_duration);
        this.setPlaying(false);
    }

    public void setTitle(CharSequence title)
    {
        this.title.setText(title);
    }

    public void setDuration(long milliseconds)
    {
        String text = TimeFormatter.hhmmss(milliseconds);
        this.duration.setText(text);
    }

    public void setPlaying(boolean isPlaying)
    {
        int background;
        int text;

        Resources resources = this.getResources();
        if (isPlaying)
        {
            background = resources.getColor(R.drawable.color_playlist_bg_playing);
            text = resources.getColor(R.drawable.color_playlist_text_playing);
        }
        else
        {
            background = resources.getColor(R.drawable.color_playlist_bg_normal);
            text = resources.getColor(R.drawable.color_playlist_text_normal);
        }

        this.setBackgroundColor(background);
        this.title.setTextColor(text);
        this.duration.setTextColor(text);
    }

}
