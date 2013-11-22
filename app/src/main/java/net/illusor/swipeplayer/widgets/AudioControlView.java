package net.illusor.swipeplayer.widgets;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.services.SoundService;
import net.illusor.swipeplayer.services.SoundServiceController;

import java.util.List;

public class AudioControlView extends LinearLayout implements View.OnClickListener
{
    private final FormattedTextView title1, artist;
    private final SeekBar progress;
    private SoundServiceController soundServiceController = new SoundServiceController();
    private int seekBarProgress;

    public AudioControlView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.setOrientation(LinearLayout.VERTICAL);
        this.setBackgroundColor(this.getResources().getColor(R.drawable.drawable_controlpanel_bg));
        this.setOnClickListener(this);

        LayoutInflater.from(context).inflate(R.layout.audio_control_panel, this);

        this.title1 = (FormattedTextView) this.findViewById(R.id.id_audio_control_title1);
        this.artist = (FormattedTextView) this.findViewById(R.id.id_audio_control_artist);
        this.progress = (SeekBar) this.findViewById(R.id.id_audio_control_progress);
        this.progress.setOnSeekBarChangeListener(new ProgressListener());
    }

    @Override
    public void onClick(View view)
    {
        SoundService.SoundServiceState state = this.soundServiceController.getServiceState();
        switch (state)
        {
            case Playing:
                this.soundServiceController.pause();
                break;
            case Paused:
                this.soundServiceController.resume();
                break;
        }
    }

    public void onStart()
    {
        Intent intent = new Intent(this.getContext(), SoundService.class);
        this.getContext().bindService(intent, this.soundServiceController, Service.BIND_AUTO_CREATE);
    }

    public void onStop()
    {
        this.getContext().unbindService(this.soundServiceController);
    }

    public void setAudioFile(AudioFile audioFile)
    {
        this.title1.setText(audioFile.getTitle());
        this.artist.setText(audioFile.getArtist());
        this.progress.setProgress(0);
        this.soundServiceController.play(audioFile);
    }

    public void setPlaylist(List<AudioFile> playlist)
    {
        this.soundServiceController.setPlaylist(playlist);
    }

    private class ProgressListener implements SeekBar.OnSeekBarChangeListener
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b)
        {
            seekBarProgress = i;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
            //soundServiceController.startSeek();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            //soundServiceController.endSeek();
        }
    }
}
