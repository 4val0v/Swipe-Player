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

import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.widget.ListView;

import java.lang.reflect.Field;

public class OverScrollHelper
{
    /**
     * Disables the overscroll effect of the ListView
     * @param listView ListView to process
     */
    public static void overScrollDisable(ListView listView)
    {
        //http://stackoverflow.com/questions/7777803/listview-top-highlight-on-scrolling/17569996#17569996
        if (Build.VERSION.SDK_INT < 16)//just know it is enough to use View.OVER_SCROLL_NEVER on LG firmware API level 16
        {
            Class<?> clazz = listView.getClass().getSuperclass();
            disableGlow(listView, clazz, "mEdgeGlowTop");
            disableGlow(listView, clazz, "mEdgeGlowBottom");
        }
        else
        {
            listView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }

    /**
     * Find and process internal fields of the lestview
     * @param sender ListView to process
     * @param absListView Its class
     * @param glow Private field name to process
     */
    private static void disableGlow(ListView sender, Class<?> absListView, String glow)
    {
        try
        {
            Field field = absListView.getDeclaredField(glow);
            field.setAccessible(true);
            Object edgeGlow = field.get(sender);
            if (edgeGlow != null)
            {
                Class glowClass = edgeGlow.getClass();
                Field edgeDrawable = glowClass.getDeclaredField("mEdge");
                edgeDrawable.setAccessible(true);
                edgeDrawable.set(edgeGlow, new ColorDrawable(android.R.color.holo_green_light));
                Field glowDrawable = glowClass.getDeclaredField("mGlow");
                glowDrawable.setAccessible(true);
                glowDrawable.set(edgeGlow, new ColorDrawable(android.R.color.holo_green_light));
            }
        }
        catch (Exception ignored)
        {
        }
    }
}
