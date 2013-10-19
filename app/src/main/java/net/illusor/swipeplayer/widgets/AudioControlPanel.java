package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.helpers.FontHelper;

public class AudioControlPanel extends LinearLayout
{
    private TextView title1, title2, artist;
    private SeekBar progress;

    public AudioControlPanel(Context context)
    {
        this(context, null);
    }

    public AudioControlPanel(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.setOrientation(LinearLayout.VERTICAL);
        this.setBackgroundColor(Color.parseColor("#8D004C"));

        LayoutInflater.from(context).inflate(R.layout.audio_control_panel, this);

        this.title1 = (TextView)this.findViewById(R.id.id_audio_control_title1);
        this.title2 = (TextView)this.findViewById(R.id.id_audio_control_title2);
        this.artist = (TextView)this.findViewById(R.id.id_audio_control_artist);

        this.title1.setTypeface(FontHelper.RobotoMedium);
        this.title2.setTypeface(FontHelper.RobotoMedium);
        this.artist.setTypeface(FontHelper.RobotoLight);
    }

    public CharSequence getTitle()
    {
        return this.title1.getText();
    }

    public void setTitle(CharSequence title)
    {
        this.title1.setText(title);
    }
}
