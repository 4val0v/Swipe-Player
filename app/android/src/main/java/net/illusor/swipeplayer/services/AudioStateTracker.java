package net.illusor.swipeplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

/**
 * Defines application behavior when user plugs/unplugs wired headphones or audio focus gets changed for some reason
 */
class AudioStateTracker extends BroadcastReceiver implements AudioManager.OnAudioFocusChangeListener
{
    private final SoundService service;
    private boolean isDucking;//if we have forced ducking mode
    private boolean waitUntilFocusGot;//if we had lost audio focus before
    private boolean waitForHeadphonesIn;//if user had unplugged headphones
    private boolean pausedByUs;//if we had stopped the playback

    public AudioStateTracker(SoundService service)
    {
        this.service = service;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction()))
        {
            //user unplugs headphones while playing - pause playback
            this.waitForHeadphonesIn = true;
            if (this.service.getState() == AudioPlayerState.Playing)
            {
                this.service.pause();
                this.pausedByUs = true;
            }
        }
        else if (Intent.ACTION_HEADSET_PLUG.equals(intent.getAction()) && intent.getIntExtra("state", -1) == 1)
        {
            //user plugs in headphones
            this.waitForHeadphonesIn = false;
            AudioPlayerState state = this.service.getState();

            //Had audio focus been lost? Did we force playback pause? Is playback paused now?
            if (!this.waitUntilFocusGot && this.pausedByUs && state == AudioPlayerState.Paused)
            {
                this.pausedByUs = false;
                this.service.resume();
            }
        }
    }

    @Override
    public void onAudioFocusChange(int i)
    {
        switch (i)
        {
            case AudioManager.AUDIOFOCUS_LOSS:
            {
                //we lost audio focus completely - stop, if not stopped already
                if (this.service.getState() != AudioPlayerState.Stopped)
                    this.service.stop();

                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            {
                //temporally lost audio focus (e.g. call incoming) - pause
                this.waitUntilFocusGot = true;
                if (this.service.getState() == AudioPlayerState.Playing)
                {
                    this.service.pause();
                    this.pausedByUs = true;
                }

                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
            {
                //we should get a bit more quiet
                this.service.setVolume(0.2f);
                this.isDucking = true;
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN:
            {
                //if we were quiet - get loud
                if (this.isDucking)
                {
                    this.service.setVolume(1.0f);
                    this.isDucking = false;
                    return;
                }

                //if incoming call is over
                this.waitUntilFocusGot = false;
                AudioPlayerState state = this.service.getState();
                //User could unplug headphones to answer the call, had he plugged them back? Did we force playback pause? Is playback paused now?
                if (!this.waitForHeadphonesIn && this.pausedByUs && state == AudioPlayerState.Paused)
                {
                    this.pausedByUs = false;
                    this.service.resume();
                }

                break;
            }
        }
    }

    /**
     * Registers current instance of {@link net.illusor.swipeplayer.services.AudioStateTracker} as a {@link android.content.BroadcastReceiver}
     */
    public void registerReceiver()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        this.service.registerReceiver(this, filter);
    }

    /**
     * Unregisters current instance of {@link net.illusor.swipeplayer.services.AudioStateTracker} as a {@link android.content.BroadcastReceiver}
     */
    public void unregisterReceiver()
    {
        this.service.unregisterReceiver(this);
    }

    /**
     * Resets the internal state of {@link net.illusor.swipeplayer.services.AudioStateTracker}
     */
    public void reset()
    {
        this.waitForHeadphonesIn = false;
        this.waitUntilFocusGot = false;
        this.pausedByUs = false;
    }
}
