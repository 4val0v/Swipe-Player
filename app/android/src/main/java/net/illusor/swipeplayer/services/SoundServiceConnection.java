/*Copyright 2014 Nikita Kobzev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

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
