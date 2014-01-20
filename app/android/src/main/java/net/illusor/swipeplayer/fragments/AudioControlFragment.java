package net.illusor.swipeplayer.fragments;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import com.nineoldandroids.view.ViewPropertyAnimator;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.services.AudioBroadcastHandler;
import net.illusor.swipeplayer.services.AudioPlayerState;
import net.illusor.swipeplayer.services.SoundService;
import net.illusor.swipeplayer.widgets.DurationDisplayView;
import net.illusor.swipeplayer.widgets.TrackPager;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Fragment used to display info about currently played audio file, display progress and play/pause the playback
 */
public class AudioControlFragment extends Fragment implements View.OnClickListener
{
    private final SoundServiceConnection connection = new SoundServiceConnection();//provides interrogation with the sound service
    private final SoundServiceReceiver receiver = new SoundServiceReceiver();//receives messages from the sound service
    private TrackPager trackList;//provides current playing track change by "swipe" gesture
    private SeekBar progress;//progressbar to track and change playback progress
    private Timer progressTimer;//timer to update the progressbar

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_audio_control, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        this.trackList = (TrackPager) this.getView().findViewById(R.id.id_audio_control_track);
        this.trackList.setOnClickListener(this);
        this.trackList.setOnPageChangeListener(new TrackSwipeListener());

        this.progress = (SeekBar) this.getView().findViewById(R.id.id_audio_control_progress);
        this.progress.setOnSeekBarChangeListener(new ProgressListener());
        this.progress.setMax(Integer.MAX_VALUE);
        this.progress.setThumbOffset(0);
    }

    @Override
    public void onClick(View view)
    {
        //play/pause feature by simple click
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

    @Override
    public void onStart()
    {
        super.onStart();
        this.getView().setVisibility(View.GONE);
        this.connection.bind();
        this.receiver.register();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        this.receiver.unregister();
        this.connection.unbind();
        this.stopTrackingProgress();
    }

    /**
     * Sets the set of music files, which are treated as a playlist
     *
     * @param playlist List of music files
     */
    public void setPlaylist(List<AudioFile> playlist)
    {
        trackList.setAdapter(new TrackPagerAdapter(playlist, getFragmentManager()));
        if (this.connection.service != null)
            setAudioFile(connection.service.getAudioFile());
    }

    /**
     * Starts updating the progressbar, according to the playback progress
     */
    private void startTrackingProgress()
    {
        if (this.progressTimer == null)
        {
            Log.d("SWIPE", "Start tracking progress");
            this.progressTimer = new Timer();
            this.progressTimer.schedule(new ProgressTrackingTask(), 0, 500);
        }
    }

    /**
     * Stops updating the progressbar
     */
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

    /**
     * Sets the currently playing audio file
     *
     * @param audioFile Currently playing music file
     */
    private void setAudioFile(AudioFile audioFile)
    {
        if (audioFile != null)
        {
            boolean fileInPlaylist = this.trackList.swipeToItem(audioFile);
            this.getView().setVisibility(fileInPlaylist ? View.VISIBLE : View.GONE);
        }
        else
        {
            this.getView().setVisibility(View.GONE);
        }
    }

    /**
     * Play animation of "Paused" playback state
     */
    private void animatePause()
    {
        ViewPropertyAnimator.animate(this.progress).alpha(0.5f).setDuration(100);
        ViewPropertyAnimator.animate(this.trackList).alpha(0.5f).setDuration(100);
    }

    //Play animation of "Playing" playback state
    private void animatePlay()
    {
        ViewPropertyAnimator.animate(this.progress).alpha(1.0f).setDuration(100);
        ViewPropertyAnimator.animate(this.trackList).alpha(1.0f).setDuration(100);
    }

    /**
     * Provides interrogation with the progressbar, tracking and changing the playback progress
     */
    private class ProgressListener implements SeekBar.OnSeekBarChangeListener
    {
        private final DurationDisplayView display = (DurationDisplayView) getActivity().findViewById(R.id.id_audio_durations);//view used to display playback progress, when user is rewinding manually
        private boolean isRewinding;//is user rewinding the audio file
        private AudioPlayerState stateWhenRewindStarted;//was the playback running, when user started to rewind?

        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b)
        {
            Log.d("SWIPE", "Manual rewind " + i + " of " + seekBar.getMax() + " b=" + b);

            //if user is rewinding manually
            if (this.isRewinding)
            {
                AudioFile file = connection.service.getAudioFile();
                int duration = (int) file.getDuration();
                int played = (int) (duration * (1.0f * i / seekBar.getMax()));
                this.display.setDuration(played, duration);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar)
        {
            Log.d("SWIPE", "Started manual rewind");

            this.stateWhenRewindStarted = connection.service.getState();

            if (this.stateWhenRewindStarted == AudioPlayerState.Playing)
                stopTrackingProgress();

            connection.service.startRewind();

            this.isRewinding = true;
            this.display.setVisibility(View.VISIBLE);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar)
        {
            Log.d("SWIPE", "Finished manual rewind");
            final float percent = (float) (1.0 * seekBar.getProgress() / seekBar.getMax());
            final int milliseconds = (int) (connection.service.getDuration() * percent);

            connection.service.finishRewind(milliseconds);

            if (this.stateWhenRewindStarted == AudioPlayerState.Playing)
                startTrackingProgress();

            this.isRewinding = false;
            this.display.setVisibility(View.GONE);
        }
    }

    /**
     * Used to detect changes of playing track, which user made using "Swipe" gesture
     */
    private class TrackSwipeListener extends ViewPager.SimpleOnPageChangeListener
    {
        @Override
        public void onPageSelected(int position)
        {
            super.onPageSelected(position);
            final AudioFile audioFile = trackList.getTrackAdapter().getData().get(position);
            getView().postDelayed(new Runnable()
            {
                @Override
                public void run()
                {
                    connection.service.play(audioFile);
                }
            }, 300);
        }
    }

    /**
     * Provides interrogation with the sound service
     */
    private class SoundServiceConnection implements ServiceConnection
    {
        private SoundService.SoundServiceBinder service;

        public void bind()
        {
            Intent intent = new Intent(getActivity(), SoundService.class);
            getActivity().bindService(intent, this, Service.BIND_AUTO_CREATE);
        }

        public void unbind()
        {
            getActivity().unbindService(this);
        }

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder)
        {
            this.service = (SoundService.SoundServiceBinder) binder;

            AudioFile audioFile = this.service.getAudioFile();
            setAudioFile(audioFile);

            AudioPlayerState state = service.getState();
            if (state == AudioPlayerState.Playing)
                startTrackingProgress();//if service is playing audio - start tracking
            else
                if (state == AudioPlayerState.Paused)
                    new ProgressTrackingTask().run();//if not - just update the progressbar once
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            this.service = null;
        }
    }

    /**
     * Receives messages sent by the sound service
     */
    private class SoundServiceReceiver extends AudioBroadcastHandler
    {
        @Override
        protected void onPlayAudioFile(AudioFile audioFile)
        {
            super.onPlayAudioFile(audioFile);
            setAudioFile(audioFile);
            startTrackingProgress();
            animatePlay();
        }

        @Override
        protected void onPlaybackStop()
        {
            super.onPlaybackStop();
            stopTrackingProgress();
            setAudioFile(null);
        }

        @Override
        protected void onPlaybackPause()
        {
            super.onPlaybackPause();
            stopTrackingProgress();
            animatePause();
        }

        @Override
        protected void onPlaybackResume()
        {
            super.onPlaybackResume();
            startTrackingProgress();
            animatePlay();
        }
    }

    /**
     * Tracks the playback progress
     */
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

            final int percent = (int) (1.0 * maxProgress * played / duration);
            getView().post(new Runnable()
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
