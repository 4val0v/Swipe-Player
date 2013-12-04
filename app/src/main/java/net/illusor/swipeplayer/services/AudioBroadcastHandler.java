package net.illusor.swipeplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import net.illusor.swipeplayer.domain.AudioFile;

public class AudioBroadcastHandler extends BroadcastReceiver
{
    private static final String ACTION_PLAY_AUDIO = "net.illusor.swipeplayer.services.SoundService.Play";
    private static final String ACTION_PLAY_STOP = "net.illusor.swipeplayer.services.SoundService.Stop";
    private static final String ACTION_PLAY_PAUSE = "net.illusor.swipeplayer.services.SoundService.Pause";
    private static final String ACTION_PLAY_RESUME = "net.illusor.swipeplayer.services.SoundService.Resume";

    private Context context;

    public AudioBroadcastHandler()
    {
    }

    public AudioBroadcastHandler(Context context)
    {
        this.context = context;
    }

    public void register()
    {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PLAY_AUDIO);
        filter.addAction(ACTION_PLAY_STOP);
        filter.addAction(ACTION_PLAY_PAUSE);
        filter.addAction(ACTION_PLAY_RESUME);
        LocalBroadcastManager.getInstance(this.getClassContext()).registerReceiver(this, filter);
    }

    public void unregister()
    {
        LocalBroadcastManager.getInstance(this.getClassContext()).unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        switch (intent.getAction())
        {
            case ACTION_PLAY_AUDIO:
            {
                AudioFile audioFile = (AudioFile)intent.getSerializableExtra(ACTION_PLAY_AUDIO);
                this.onPlayAudioFile(audioFile);
                break;
            }
            case ACTION_PLAY_STOP:
            {
                this.onPlaybackStop();
                break;
            }
            case ACTION_PLAY_PAUSE:
            {
                this.onPlaybackPause();
                break;
            }
            case ACTION_PLAY_RESUME:
            {
                this.onPlaybackResume();
                break;
            }
        }
    }

    void sendPlayAudioFile(AudioFile audioFile)
    {
        Intent intent = new Intent(ACTION_PLAY_AUDIO);
        intent.putExtra(ACTION_PLAY_AUDIO, audioFile);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    void sendPlaybackStop()
    {
        Intent intent = new Intent(ACTION_PLAY_STOP);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    void sendPlaybackPause()
    {
        Intent intent = new Intent(ACTION_PLAY_PAUSE);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    void sendPlaybackResume()
    {
        Intent intent = new Intent(ACTION_PLAY_RESUME);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(intent);
    }

    protected void onPlayAudioFile(AudioFile audioFile)
    {

    }

    protected void onPlaybackStop()
    {

    }

    protected void onPlaybackPause()
    {

    }

    protected void onPlaybackResume()
    {

    }

    protected Context getClassContext()
    {
        return this.context;
    }
}
