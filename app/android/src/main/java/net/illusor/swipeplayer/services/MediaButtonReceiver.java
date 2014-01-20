package net.illusor.swipeplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

/**
 * Receives and handles system media buttons broadcast messages
 */
public class MediaButtonReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        KeyEvent event = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
        if (event.getAction() == KeyEvent.ACTION_DOWN)
        {
            switch (event.getKeyCode())
            {
                case KeyEvent.KEYCODE_MEDIA_NEXT:
                {
                    this.onNextTrack(context);
                    break;
                }
                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                {
                    this.onPreviousTrack(context);
                    break;
                }
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                {
                    this.onPlayPause(context);
                    break;
                }
                case KeyEvent.KEYCODE_MEDIA_STOP:
                {
                    this.onStop(context);
                    break;
                }
            }
        }
    }

    /**
     * Fires when "Play Next" media button is pressed
     * @param context Call context
     */
    protected void onNextTrack(Context context)
    {
        Intent intent = new Intent(context, SoundService.class);
        intent.setAction(SoundService.INTENT_CODE_NEXT);
        context.startService(intent);
    }

    /**
     * Fires when "Play Previous" media button is pressed
     * @param context Call context
     */
    protected void onPreviousTrack(Context context)
    {
        Intent intent = new Intent(context, SoundService.class);
        intent.setAction(SoundService.INTENT_CODE_PREVIOUS);
        context.startService(intent);
    }

    /**
     * Fires when "Play/Pause" media button is pressed
     * @param context Call context
     */
    protected void onPlayPause(Context context)
    {
        Intent intent = new Intent(context, SoundService.class);
        intent.setAction(SoundService.INTENT_CODE_PAUSE);
        context.startService(intent);
    }

    /**
     * Fires when "Stop" media button is pressed
     * @param context Call context
     */
    protected void onStop(Context context)
    {
        Intent intent = new Intent(context, SoundService.class);
        intent.setAction(SoundService.INTENT_CODE_STOP);
        context.startService(intent);
    }
}
