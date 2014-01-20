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
