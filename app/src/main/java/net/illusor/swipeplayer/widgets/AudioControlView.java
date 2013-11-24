package net.illusor.swipeplayer.widgets;

import android.app.Service;
import android.content.*;
import android.os.IBinder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.services.SoundService;

import java.util.Timer;
import java.util.TimerTask;

public class AudioControlView extends LinearLayout implements View.OnClickListener
{
    private final FormattedTextView title1, artist;
    private final SeekBar progress;
    private final SoundServiceConnection connection = new SoundServiceConnection();
    private final SoundServiceReceiver receiver = new SoundServiceReceiver();
    private Timer progressTimer;

    public AudioControlView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.setOrientation(LinearLayout.VERTICAL);
        this.setBackgroundColor(this.getResources().getColor(R.drawable.drawable_controlpanel_bg));
        this.setOnClickListener(this);

        LayoutInflater.from(context).inflate(R.layout.audio_control_view, this);

        this.title1 = (FormattedTextView) this.findViewById(R.id.id_audio_control_title1);
        this.artist = (FormattedTextView) this.findViewById(R.id.id_audio_control_artist);
        this.progress = (SeekBar) this.findViewById(R.id.id_audio_control_progress);
        this.progress.setOnSeekBarChangeListener(new ProgressListener());
        this.progress.setMax(Integer.MAX_VALUE);
    }

    @Override
    public void onClick(View view)
    {
        SoundService.SoundServiceState state = this.connection.service.getServiceState();
        switch (state)
        {
            case Playing:
            {
                Log.d("SWIPE", "Click: pause (service is playing now)");
                this.stopTrackingProgress();
                this.connection.service.pause();
                break;
            }
            case Paused:
            {
                Log.d("SWIPE", "Click: resume (service is paused now)");
                this.startTrackingProgress();
                this.connection.service.resume();
                break;
            }
        }
    }

    public void onStart()
    {
        this.connection.bind();
        this.receiver.register();
    }

    public void onStop()
    {
        this.receiver.unRegister();
        this.connection.unbind();
        this.stopTrackingProgress();
    }

    private void setVisualState(AudioFile audioFile)
    {
        this.setVisibility(VISIBLE);
        this.title1.setText(audioFile.getTitle());
        this.artist.setText(audioFile.getArtist());
    }

    private void startTrackingProgress()
    {
        if (this.progressTimer == null)
        {
            Log.d("SWIPE", "Start tracking progress");
            this.progressTimer = new Timer();
            this.progressTimer.schedule(new ProgressTrackingTask(), 0, 500);
        }
    }

    private void stopTrackingProgress()
    {
        if (this.progressTimer != null)
        {
            Log.d("SWIPE", "Cancel tracking progress");
            this.progressTimer.cancel();
            this.progressTimer.purge();
            this.progressTimer = null;
        }
    }

    private class ProgressListener implements SeekBar.OnSeekBarChangeListener
    {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b)
        {
            Log.d("SWIPE", "Manual rewind " + i + " of " + seekBar.getMax() + " b=" + b);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
            Log.d("SWIPE", "Started manual rewind");
            stopTrackingProgress();
            connection.service.startSeek();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            Log.d("SWIPE", "Finished manual rewind");
            final float percent = (float)(1.0 * seekBar.getProgress() / seekBar.getMax());
            final int milliseconds = (int)(connection.service.getCurrentFile().getDuration() * percent);
            connection.service.endSeek(milliseconds);
            startTrackingProgress();
        }
    }

    private class SoundServiceConnection implements ServiceConnection
    {
        private SoundService.SoundServiceBinder service;

        public void bind()
        {
            Intent intent = new Intent(getContext(), SoundService.class);
            getContext().bindService(intent, this, Service.BIND_AUTO_CREATE);
        }

        public void unbind()
        {
            getContext().unbindService(this);
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder)
        {
            this.service = (SoundService.SoundServiceBinder)binder;

            AudioFile audioFile = this.service.getCurrentFile();
            if (audioFile != null)
            {
                setVisualState(audioFile);
                SoundService.SoundServiceState state = this.service.getServiceState();
                if (state == SoundService.SoundServiceState.Playing)
                    startTrackingProgress();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            this.service = null;
        }
    }

    private class ProgressTrackingTask extends TimerTask
    {
        private final int maxProgress;

        private ProgressTrackingTask()
        {
            this.maxProgress = progress.getMax();
        }

        @Override
        public void run()
        {
            final AudioFile file = connection.service.getCurrentFile();
            final int played = connection.service.getProgress();
            final long duration = file.getDuration();

            final int percent = (int)(1.0 * maxProgress * played / duration);
            post(new Runnable()
            {
                @Override
                public void run()
                {
                    progress.setProgress(percent);
                    Log.d("SWIPE", "Progress: " + percent);
                }
            });
        }
    }

    private class SoundServiceReceiver extends BroadcastReceiver
    {
        public void register()
        {
            IntentFilter filter = new IntentFilter(SoundService.ACTION_NEW_AUDIO);
            getContext().registerReceiver(this, filter);
        }

        public void unRegister()
        {
            getContext().unregisterReceiver(this);
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (intent.getAction().equals(SoundService.ACTION_NEW_AUDIO))
            {
                AudioFile audioFile = (AudioFile)intent.getSerializableExtra(SoundService.ACTION_NEW_AUDIO);
                setVisualState(audioFile);
                startTrackingProgress();
            }
        }
    }
}
