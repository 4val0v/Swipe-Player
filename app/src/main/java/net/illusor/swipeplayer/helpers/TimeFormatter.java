package net.illusor.swipeplayer.helpers;

import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.SwipeApplication;

public class TimeFormatter
{
    private static final long hourSize = 3600000;
    private static final long minuteSize = 60000;
    private static final long secondSize = 1000;

    private static final String timeFormatHHMMSS = SwipeApplication.getAppContext().getString(R.string.time_format_hhmmss);
    private static final String timeFormatMMSS = SwipeApplication.getAppContext().getString(R.string.time_format_mmss);

    public static String hhmmss(long milliseconds)
    {
        long hours = milliseconds / hourSize;
        long h = hours * hourSize;
        long minutes = (milliseconds - h) / minuteSize;
        long m = minutes * minuteSize;
        long seconds =  (milliseconds - h - m) / secondSize;

        String result;
        if (hours > 0)
            result = String.format(timeFormatHHMMSS, hours, minutes, seconds);
        else
            result = String.format(timeFormatMMSS, minutes, seconds);

        return result;
    }
}
