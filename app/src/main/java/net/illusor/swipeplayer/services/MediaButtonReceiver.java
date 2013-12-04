package net.illusor.swipeplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

class MediaButtonReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.ACTION_MEDIA_BUTTON.equals(intent.getAction()))
        {
            KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            switch (event.getKeyCode())
            {
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                {
                    this.onNextTrack();
                    break;
                }
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                {
                    this.onPreviousTrack();
                    break;
                }
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                {
                    this.onPlayPause();
                    break;
                }
                case KeyEvent.KEYCODE_MEDIA_STOP:
                {
                    this.onStop();
                    break;
                }

            }
        }
    }

    protected void onNextTrack()
    {

    }

    protected void onPreviousTrack()
    {

    }

    protected void onPlayPause()
    {

    }

    protected void onStop()
    {

    }
}
