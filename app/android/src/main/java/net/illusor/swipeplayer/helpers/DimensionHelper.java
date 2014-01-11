package net.illusor.swipeplayer.helpers;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import net.illusor.swipeplayer.SwipeApplication;

/**
 * Contains methods for convinient dimension conversion
 */
public class DimensionHelper
{
    private static final DisplayMetrics metrics = SwipeApplication.getAppContext().getResources().getDisplayMetrics();

    /**
     * Converts Dip to Px
     * @param px Px value
     * @return Dip value
     */
    public static float dipToPx(float px)
    {
        float dip = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, metrics);
        return dip;
    }
}
