package net.illusor.swipeplayer.helpers;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import net.illusor.swipeplayer.SwipeApplication;

public class DimensionHelper
{
    public static float dipToPx(float px)
    {
        DisplayMetrics metrics = SwipeApplication.getAppContext().getResources().getDisplayMetrics();
        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, metrics);
        return dip;
    }
}
