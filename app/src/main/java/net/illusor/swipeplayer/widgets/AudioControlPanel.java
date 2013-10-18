package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import net.illusor.swipeplayer.R;

public class AudioControlPanel extends LinearLayout
{
    private TextView title;
    private TextView artist;

    public AudioControlPanel(Context context)
    {
        this(context, null);
    }

    public AudioControlPanel(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.audio_info_panel, this, true);
    }
}
