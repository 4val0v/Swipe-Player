package net.illusor.swipeplayer.widgets;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;

public class AudioControlView extends LinearLayout
{
    private final TextView title1, title2, artist;
    private final SeekBar progress;
    private AudioFile audioFile;

    public AudioControlView(Context context)
    {
        this(context, null);
    }

    public AudioControlView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.setOrientation(LinearLayout.VERTICAL);
        this.setBackgroundColor(this.getResources().getColor(R.drawable.color_controlpanel_bg));

        LayoutInflater.from(context).inflate(R.layout.audio_control_panel, this);

        this.title1 = (TextView) this.findViewById(R.id.id_audio_control_title1);
        this.title2 = (TextView) this.findViewById(R.id.id_audio_control_title2);
        this.artist = (TextView) this.findViewById(R.id.id_audio_control_artist);
        this.progress = (SeekBar) this.findViewById(R.id.id_audio_control_progress);
        this.progress.setOnSeekBarChangeListener(new ProgressListener());
    }

    public AudioFile getAudioFile()
    {
        return audioFile;
    }

    public void setAudioFile(AudioFile audioFile)
    {
        this.audioFile = audioFile;
        this.title1.setText(audioFile.getTitle());
        this.artist.setText(audioFile.getArtist());
        this.progress.setProgress(0);
    }

    private class ProgressListener implements SeekBar.OnSeekBarChangeListener
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b)
        {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
        }
    }
}
