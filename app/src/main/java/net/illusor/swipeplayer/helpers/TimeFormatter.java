package net.illusor.swipeplayer.helpers;

import java.util.concurrent.TimeUnit;

public class TimeFormatter
{
    private static final long hourSize = 3600000;
    private static final long minuteSize = 60000;
    private static final long secondSize = 1000;

    public static String hhmmss(long milliseconds)
    {
        long hours = milliseconds / hourSize;
        long h = hours * hourSize;
        long minutes = (milliseconds - h) / minuteSize;
        long m = minutes * minuteSize;
        long seconds =  (milliseconds - h - m) / secondSize;

        String result;
        if (hours > 0)
            result = String.format("%d:%d:%d", hours, minutes, seconds);
        else
            result = String.format("%d:%d", minutes, seconds);

        return result;
    }
}
