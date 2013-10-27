package net.illusor.swipeplayer.helpers;

import java.util.concurrent.TimeUnit;

public class TimeFormatter
{
    public static String hhmmss(long milliseconds)
    {
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);

        String result;
        if (hours > 0)
            result = String.format("%d:%d:%d", hours, minutes, seconds);
        else
            result = String.format("%d:%d", minutes, seconds);

        return result;
    }
}
