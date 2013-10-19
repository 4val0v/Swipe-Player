package net.illusor.swipeplayer;

import android.app.Application;
import android.content.Context;

public class SwipeApplication extends Application
{
    private static Context context;

    @Override
    public void onCreate()
    {
        super.onCreate();
        SwipeApplication.context = this.getApplicationContext();
    }

    public static Context getAppContext()
    {
        return SwipeApplication.context;
    }
}
