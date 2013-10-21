package net.illusor.swipeplayer.helpers;

import android.graphics.Typeface;
import net.illusor.swipeplayer.SwipeApplication;

public class FontHelper
{
    public static Typeface RobotoLight = Typeface.createFromAsset(SwipeApplication.getAppContext().getAssets(), "fonts/Roboto-Light.ttf");
    public static Typeface RobotoMedium = Typeface.createFromAsset(SwipeApplication.getAppContext().getAssets(), "fonts/Roboto-Medium.ttf");
    public static Typeface PTSans = Typeface.createFromAsset(SwipeApplication.getAppContext().getAssets(), "fonts/PTS55F.ttf");
    public static Typeface PTSansNarrow = Typeface.createFromAsset(SwipeApplication.getAppContext().getAssets(), "fonts/PTN57F.ttf");
}
