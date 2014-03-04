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
