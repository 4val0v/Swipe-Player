package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.helpers.TimeFormatter;

public class PlaylistItemView extends LinearLayout
{
    private TextView title, duration;
    private AudioFile audioFile;

    public PlaylistItemView(Context context)
    {
        this(context, null);
    }

    public PlaylistItemView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.list_item_playlist, this);
        this.title = (TextView) this.findViewById(R.id.id_play_title);
        this.duration = (TextView) this.findViewById(R.id.id_play_duration);
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
}