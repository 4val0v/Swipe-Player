package net.illusor.swipeplayer.services;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import net.illusor.swipeplayer.SwipeApplication;

public abstract class SoundServiceConnection implements ServiceConnection
{
    public SoundService.SoundServiceBinder service;

    public void bind()
    {
        Intent intent = new Intent(SwipeApplication.getAppContext(), SoundService.class);
        this.getContext().bindService(intent, this, Service.BIND_AUTO_CREATE);
    }

    public void unbind()
    {
        this.getContext().unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder binder)
    {
        this.service = (SoundService.SoundServiceBinder)binder;
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName)
    {
        this.service = null;
    }

    public abstract Context getContext();
}
