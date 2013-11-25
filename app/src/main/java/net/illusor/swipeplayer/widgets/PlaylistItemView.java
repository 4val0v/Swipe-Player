package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.helpers.TimeFormatter;

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
        this.setBackgroundDrawable(this.getResources().getDrawable(R.drawable.item_playlist_bg));

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
            super.mergeDrawableStates(state, new int[]{android.R.attr.state_checked});
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