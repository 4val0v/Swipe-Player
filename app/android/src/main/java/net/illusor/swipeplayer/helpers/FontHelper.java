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
