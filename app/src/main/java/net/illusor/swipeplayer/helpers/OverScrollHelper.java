package net.illusor.swipeplayer.helpers;

import android.R;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.widget.ListView;

import java.lang.reflect.Field;

public class OverScrollHelper
{
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
                edgeDrawable.set(edgeGlow, new ColorDrawable(R.color.transparent));
                Field glowDrawable = glowClass.getDeclaredField("mGlow");
                glowDrawable.setAccessible(true);
                glowDrawable.set(edgeGlow, new ColorDrawable(R.color.transparent));
            }
        }
        catch (Exception e)
        {
        }
    }
}
