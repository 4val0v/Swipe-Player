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

package net.illusor.swipeplayer.helpers;

import android.graphics.Typeface;
import net.illusor.swipeplayer.SwipeApplication;
import org.apache.commons.lang.NotImplementedException;

public class FontHelper
{
    public static Typeface RobotoLight = Typeface.createFromAsset(SwipeApplication.getAppContext().getAssets(), "fonts/Roboto-Light.ttf");
    public static Typeface PTSans = Typeface.createFromAsset(SwipeApplication.getAppContext().getAssets(), "fonts/PTS55F.ttf");
    public static Typeface PTSansNarrow = Typeface.createFromAsset(SwipeApplication.getAppContext().getAssets(), "fonts/PTN57F.ttf");
    public static Typeface PTSerifBold = Typeface.createFromAsset(SwipeApplication.getAppContext().getAssets(), "fonts/PTF75F.ttf");
    public static Typeface PTSerifItalic = Typeface.createFromAsset(SwipeApplication.getAppContext().getAssets(), "fonts/PTF56F.ttf");

    public static Typeface font(int value)
    {
        switch (value)
        {
            case 0:
                return RobotoLight;
            case 1:
                return PTSans;
            case 2:
                return PTSansNarrow;
            case 3:
                return PTSerifBold;
            case 4:
                return PTSerifItalic;
            default:
                throw new NotImplementedException();
        }
    }
}
