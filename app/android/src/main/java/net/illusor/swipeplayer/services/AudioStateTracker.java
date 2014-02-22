package net.illusor.swipeplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import java.util.EnumSet;

class AudioStateTracker extends BroadcastReceiver implements AudioManager.OnAudioFocusChangeListener
{
    private final SoundService service;
    private EnumSet<AudioStates> audioState = EnumSet.noneOf(AudioStates.class);
    private EnumSet<AudioStates> storedAudioState;

    public AudioStateTracker(SoundService service)
    {
        this.service = service;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
        {
            EnumSet<AudioStates> newState = EnumSet.copyOf(this.audioState);
            newState.remove(AudioStates.HeadphonesPlugged);
            this.behaveAccordingToState(newState);
        }
        else if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction()) && intent.getIntExtra("state", -1) == 1)
        {
            EnumSet<AudioStates> newState = EnumSet.copyOf(this.audioState);

            if (!newState.contains(AudioStates.HeadphonesPlugged))
            {
                newState.add(AudioStates.HeadphonesPlugged);
                this.behaveAccordingToState(newState);
            }
        }
    }

    @Override
    public void onAudioFocusChange(int i)
    {
        EnumSet<AudioStates> newState = EnumSet.copyOf(this.audioState);

        switch (i)
        {
            case AudioManager.AUDIOFOCUS_LOSS:
            {
                newState.clear();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            {
                newState.remove(AudioStates.FocusGained);
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            {
                if (!newState.contains(AudioStates.Duck))
                    newState.add(AudioStates.Duck);
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN:
            {
                if (!newState.contains(AudioStates.FocusGained))
                    newState.add(AudioStates.FocusGained);

                newState.remove(AudioStates.Duck);
                break;
            }
        }

        this.behaveAccordingToState(newState);
    }

    public void registerReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        this.service.registerReceiver(this, filter);
    }

    public void unregisterReceiver()
    {
        this.service.unregisterReceiver(this);
    }

    public void startTracking()
    {
        EnumSet<AudioStates> newState = EnumSet.copyOf(this.audioState);

        if (!newState.contains(AudioStates.FocusGained))
            newState.add(AudioStates.FocusGained);

        this.behaveAccordingToState(newState);
    }

    private void behaveAccordingToState(EnumSet<AudioStates> newState)
    {
        if (newState.isEmpty())
        {
            this.service.stop();
            this.audioState = newState;
            return;
        }

        if (!newState.contains(AudioStates.FocusGained) || (!newState.contains(AudioStates.HeadphonesPlugged) && this.audioState.contains(AudioStates.HeadphonesPlugged)))
        {
            if (this.service.getState() != AudioPlayerState.Paused)
                this.service.pause();

            this.storedAudioState = this.audioState;
            this.audioState = newState;
            return;
        }
        else if (this.storedAudioState != null)
        {
            if (newState.contains(AudioStates.FocusGained) && (newState.contains(AudioStates.HeadphonesPlugged) || newState.contains(AudioStates.HeadphonesPlugged) == this.storedAudioState.contains(AudioStates.HeadphonesPlugged)))
            {
                if (this.service.getState() == AudioPlayerState.Paused)
                    this.service.resume();

                this.service.resume();
                this.audioState = newState;
                this.storedAudioState = null;
            }
        }

        if (newState.contains(AudioStates.Duck) && !this.audioState.contains(this.service))
            this.service.setVolume(0.2f);
        else if (!newState.contains(AudioStates.Duck) && this.audioState.contains(this.service))
            this.service.setVolume(1.0f);

        this.audioState = newState;
    }

    private enum AudioStates
    {
        HeadphonesPlugged,
        FocusGained,
        Duck
    }
}
