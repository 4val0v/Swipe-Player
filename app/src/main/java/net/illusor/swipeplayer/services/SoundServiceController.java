package net.illusor.swipeplayer.services;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import net.illusor.swipeplayer.domain.AudioFile;

public class SoundServiceController implements ServiceConnection
{
    private SoundService.SoundServiceBinder binder;

    public void onServiceConnected(ComponentName componentName, IBinder iBinder)
    {
        this.binder = (SoundService.SoundServiceBinder)iBinder;
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

    public void seek(int milliseconds)
    {
        this.binder.seek(milliseconds);
    }
}
