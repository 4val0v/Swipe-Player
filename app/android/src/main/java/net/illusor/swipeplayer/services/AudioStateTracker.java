package net.illusor.swipeplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * Defines application behavior when user plugs/unplugs wired headphones or audio focus gets changed for some reason
 */
class AudioStateTracker extends BroadcastReceiver implements AudioManager.OnAudioFocusChangeListener
{
    private final TelephonyStateListener telephonyListener = new TelephonyStateListener(this);
    private final SoundService service;
    private TelephonyManager telephonyManager;
    private boolean isDucking;//if we have forced ducking mode
    private boolean waitUntilFocusGot;//if we had lost audio focus before
    private boolean waitForHeadphonesIn;//if user had unplugged headphones
    private boolean waitForCallEnds;//if we have a call in progress
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

            //Had audio focus been lost? Are there any running calls? Did we force playback pause? Is playback paused now?
            if (!this.waitUntilFocusGot && !this.waitForCallEnds && this.pausedByUs && state == AudioPlayerState.Paused)
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
                //User could unplug headphones to answer the call, had he plugged them back? Are there any calls running? Did we force playback pause? Is playback paused now?
                if (!this.waitForHeadphonesIn && !this.waitForCallEnds && this.pausedByUs && state == AudioPlayerState.Paused)
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
        //react on headphones plug/unplug
        IntentFilter filter = new IntentFilter();
        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(Intent.ACTION_HEADSET_PLUG);
        this.service.registerReceiver(this, filter);

        //react on calls
        this.telephonyManager = (TelephonyManager)service.getSystemService(Context.TELEPHONY_SERVICE);
        this.telephonyManager.listen(this.telephonyListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * Unregisters current instance of {@link net.illusor.swipeplayer.services.AudioStateTracker} as a {@link android.content.BroadcastReceiver}
     */
    public void unregisterReceiver()
    {
        this.service.unregisterReceiver(this);
        this.telephonyManager.listen(this.telephonyListener, PhoneStateListener.LISTEN_NONE);
    }

    /**
     * Resets the internal state of {@link net.illusor.swipeplayer.services.AudioStateTracker}
     */
    public void reset()
    {
        this.waitForHeadphonesIn = false;
        this.waitUntilFocusGot = false;
        this.waitForCallEnds = false;
        this.pausedByUs = false;
    }

    /**
     * Detects and handles phone calls
     */
    private class TelephonyStateListener extends PhoneStateListener
    {
        private final AudioStateTracker tracker;

        private TelephonyStateListener(AudioStateTracker tracker)
        {
            this.tracker = tracker;
        }

        @Override
        public void onCallStateChanged(int state, String incomingNumber)
        {
            super.onCallStateChanged(state, incomingNumber);

            if (state == TelephonyManager.CALL_STATE_RINGING)
            {
                //a call is started
                this.tracker.waitForCallEnds = true;
                if (this.tracker.service.getState() == AudioPlayerState.Playing)
                {
                    this.tracker.service.pause();
                    this.tracker.pausedByUs = true;
                }
            }
            else if (state == TelephonyManager.CALL_STATE_IDLE)
            {
                //a call has ended
                this.tracker.waitForCallEnds = false;
                AudioPlayerState audioState = this.tracker.service.getState();

                //Had audio focus been lost? User could unplug headphones to answer the call, had he plugged them back? Did we force playback pause? Is playback paused now?
                if (!this.tracker.waitUntilFocusGot && !this.tracker.waitForHeadphonesIn && this.tracker.pausedByUs && audioState == AudioPlayerState.Paused)
                {
                    this.tracker.pausedByUs = false;
                    this.tracker.service.resume();
                }
            }
        }
    }
}
