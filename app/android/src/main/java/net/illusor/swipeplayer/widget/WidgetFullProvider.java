package net.illusor.swipeplayer.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import net.illusor.swipeplayer.services.SoundService;

public class WidgetFullProvider extends AppWidgetProvider
{
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
    {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        Intent intent = new Intent(context, SoundService.class);
        intent.setAction(SoundService.INTENT_CODE_WIDGET_UPDATE);
        context.startService(intent);
    }
}
