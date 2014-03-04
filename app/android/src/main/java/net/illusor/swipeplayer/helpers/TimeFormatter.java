/*Copyright 2013 Nikita Kobzev

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

import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.SwipeApplication;

/**
 * Provides time formatting features
 */
public class TimeFormatter
{
    private static final long hourSize = 3600000;
    private static final long minuteSize = 60000;
    private static final long secondSize = 1000;

    private static final String timeFormatHHMMSS = SwipeApplication.getAppContext().getString(R.string.time_format_hhmmss);
    private static final String timeFormatMMSS = SwipeApplication.getAppContext().getString(R.string.time_format_mmss);

    /**
     * Outputs input time in form of hh:mm:ss
     * @param milliseconds time milliseconds value
     * @return Formatted output
     */
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
