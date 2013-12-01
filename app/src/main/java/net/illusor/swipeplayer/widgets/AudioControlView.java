package net.illusor.swipeplayer.widgets;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.fragments.TrackListAdapter;
import net.illusor.swipeplayer.services.AudioBroadcastHandler;
import net.illusor.swipeplayer.services.AudioPlayerState;
import net.illusor.swipeplayer.services.SoundService;

import java.util.Timer;
import java.util.TimerTask;

public class AudioControlView extends LinearLayout implements View.OnClickListener
{
    private final TrackPager trackList;
    private final SeekBar progress;
    private final SoundServiceConnection connection = new SoundServiceConnection();
    private final SoundServiceReceiver receiver = new SoundServiceReceiver();
    private Timer progressTimer;

    public AudioControlView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        this.setOrientation(LinearLayout.VERTICAL);
        this.setBackgroundColor(this.getResources().getColor(R.color.color_controlpanel_bg));

        LayoutInflater.from(context).inflate(R.layout.audio_control_view, this);

        this.trackList = (TrackPager)this.findViewById(R.id.id_audio_control_track);
        this.trackList.setOnClickListener(this);
        this.trackList.setOnPageChangeListener(new TrackSwipeListener());

        this.progress = (SeekBar) this.findViewById(R.id.id_audio_control_progress);
        this.progress.setOnSeekBarChangeListener(new ProgressListener());
        this.progress.setMax(Integer.MAX_VALUE);
        this.progress.setThumbOffset(0);
    }

    @Override
    public void onClick(View view)
    {
        AudioPlayerState state = this.connection.service.getState();
        switch (state)
        {
            case Playing:
            {
                Log.d("SWIPE", "Click: pause (service is playing now)");
                this.connection.service.pause();
                break;
            }
            case Paused:
            {
                Log.d("SWIPE", "Click: resume (service is paused now)");
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
        this.receiver.unregister();
        this.connection.unbind();
        this.stopTrackingProgress();
    }

    public void setPlaylistAdapter(TrackListAdapter adapter)
    {
        if (adapter != null)
        {
            this.trackList.setAdapter(adapter);
            if (this.connection.service != null)
            {
                AudioFile audioFile = this.connection.service.getAudioFile();
                if (audioFile != null)
                    this.setAudioFile(audioFile);
            }
        }
        else
        {
            this.setVisibility(View.GONE);
            stopTrackingProgress();
        }
    }

    private void setAudioFile(AudioFile audioFile)
    {
        if (this.trackList.getAdapter() != null)
        {
            int index = this.trackList.getTrackAdapter().getData().indexOf(audioFile);
            if (index >= 0)
            {
                this.setVisibility(VISIBLE);
                this.trackList.swipeToItem(index);
                AudioPlayerState state = this.connection.service.getState();
                if (state == AudioPlayerState.Playing)
                    startTrackingProgress();

            }
            else
            {
                this.setVisibility(GONE);
                stopTrackingProgress();
            }
        }
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
        private AudioPlayerState stateWhenRewindStarted;

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b)
        {
            Log.d("SWIPE", "Manual rewind " + i + " of " + seekBar.getMax() + " b=" + b);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
            Log.d("SWIPE", "Started manual rewind");

            this.stateWhenRewindStarted = connection.service.getState();

            if (this.stateWhenRewindStarted == AudioPlayerState.Playing)
                stopTrackingProgress();

            connection.service.startRewind();
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            Log.d("SWIPE", "Finished manual rewind");
            final float percent = (float)(1.0 * seekBar.getProgress() / seekBar.getMax());
            final int milliseconds = (int)(connection.service.getDuration() * percent);

            connection.service.finishRewind(milliseconds);

            if (this.stateWhenRewindStarted == AudioPlayerState.Playing)
                startTrackingProgress();
        }
    }

    private class TrackSwipeListener extends ViewPager.SimpleOnPageChangeListener
    {
        @Override
        public void onPageSelected(int position)
        {
            super.onPageSelected(position);
            final AudioFile audioFile = trackList.getTrackAdapter().getData().get(position);
            postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    connection.service.play(audioFile);
                }
            }, 300);

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

            AudioFile audioFile = this.service.getAudioFile();
            if (audioFile != null)
                setAudioFile(audioFile);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            this.service = null;
        }
    }

    private class SoundServiceReceiver extends AudioBroadcastHandler
    {
        @Override
        protected void onPlayAudioFile(AudioFile audioFile)
        {
            super.onPlayAudioFile(audioFile);
            setAudioFile(audioFile);
            startTrackingProgress();
        }

        @Override
        protected void onPlaybackStop()
        {
            super.onPlaybackStop();
            stopTrackingProgress();
            setVisibility(View.GONE);
        }

        @Override
        protected void onPlaybackPause()
        {
            super.onPlaybackPause();
            stopTrackingProgress();
        }

        @Override
        protected void onPlaybackResume()
        {
            super.onPlaybackResume();
            startTrackingProgress();
        }

        @Override
        protected Context getClassContext()
        {
            return getContext();
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
            AudioFile file = connection.service.getAudioFile();
            int played = connection.service.getPosition();
            long duration = file.getDuration();

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
}
