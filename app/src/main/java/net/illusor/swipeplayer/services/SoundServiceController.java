package net.illusor.swipeplayer.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import net.illusor.swipeplayer.domain.AudioFile;

import java.util.List;

public class SoundServiceController implements ServiceConnection
{
    private SoundService.SoundServiceBinder binder;
    private List<AudioFile> playlist;

    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        this.binder = (SoundService.SoundServiceBinder)iBinder;
        if (this.playlist != null)
            this.binder.setPlaylist(this.playlist);
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
        this.binder = null;
    }

    public void play(AudioFile file)
    {
        this.binder.play(file);
    }

    public void pause()
    {
        this.binder.pause();
    }

    public void resume()
    {
        this.binder.resume();
    }

    public void startSeek()
    {
        this.binder.startSeek();
    }

    public void endSeek(int milliseconds)
    {
        this.binder.endSeek(milliseconds);
    }

    public void setPlaylist(List<AudioFile> playlist)
    {
        if (this.binder == null)
        {
            this.playlist = playlist;
        }
        else
        {
            this.playlist = null;
            this.binder.setPlaylist(playlist);
        }
    }

    public SoundService.SoundServiceState getServiceState()
    {
        if (this.binder == null)
            return SoundService.SoundServiceState.Stopped;
        else
            return this.binder.getServiceState();
    }
}
