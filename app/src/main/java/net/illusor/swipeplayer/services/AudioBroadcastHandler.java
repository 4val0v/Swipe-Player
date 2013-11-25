package net.illusor.swipeplayer.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import net.illusor.swipeplayer.domain.AudioFile;

public class AudioBroadcastHandler extends BroadcastReceiver
{
    private static final String ACTION_PLAY_AUDIO = "net.illusor.swipeplayer.services.SoundService.Play";
    private static final String ACTION_PLAY_STOP = "net.illusor.swipeplayer.services.SoundService.Stop";

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
        this.getClassContext().registerReceiver(this, filter);
    }

    public void unregister()
    {
        this.getClassContext().unregisterReceiver(this);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (ACTION_PLAY_AUDIO.equals(intent.getAction()))
        {
            AudioFile audioFile = (AudioFile)intent.getParcelableExtra(ACTION_PLAY_AUDIO);
            this.onPlayAudioFile(audioFile);
        }
        else if (ACTION_PLAY_STOP.equals(intent.getAction()))
        {
            this.onPlaybackStop();
        }
    }

    void sendPlayAudioFile(AudioFile audioFile)
    {
        Intent intent = new Intent(ACTION_PLAY_AUDIO);
        intent.putExtra(ACTION_PLAY_AUDIO, audioFile);
        this.context.sendBroadcast(intent);
    }

    void sendPlaybackStop()
    {
        Intent intent = new Intent(ACTION_PLAY_STOP);
        this.context.sendBroadcast(intent);
    }

    protected void onPlayAudioFile(AudioFile audioFile)
    {

    }

    protected void onPlaybackStop()
    {

    }

    protected Context getClassContext()
    {
        return this.context;
    }
}
