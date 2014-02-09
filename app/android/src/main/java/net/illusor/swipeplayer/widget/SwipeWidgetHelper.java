package net.illusor.swipeplayer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.activities.SwipeActivity;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.services.SoundService;

/**
 * Provides helper methods for the screen widget management
 */
public class SwipeWidgetHelper
{
    private final Context context;

    public SwipeWidgetHelper(Context context)
    {
        this.context = context;
    }

    /**
     * Puts the screen widget to state: "Currently playing.."
     * @param audioFile Current audio file
     */
    public void setPlaying(AudioFile audioFile)
    {
        PendingIntent intent = this.getButtonIntent(SoundService.INTENT_CODE_PAUSE);
        this.setWidgetState(audioFile.getName(), audioFile.getArtist(), true, android.R.drawable.ic_media_pause, intent);
    }

    /**
     * Puts the screen widget to state: "Paused"
     * @param audioFile Current audio file
     */
    public void setPaused(AudioFile audioFile)
    {
        PendingIntent intent = this.getButtonIntent(SoundService.INTENT_CODE_RESUME);
        this.setWidgetState(audioFile.getName(), audioFile.getArtist(), true, android.R.drawable.ic_media_play, intent);
    }

    /**
     * Puts the screen widget to state: "Stopped"
     */
    public void setStopped()
    {
        this.setWidgetState(context.getApplicationInfo().nonLocalizedLabel, context.getResources().getString(R.string.str_state_stopped), false, 0, null);
    }

    /**
     * Sets the screen widget state
     * @param title Title of the currently played music
     * @param artist Music artist who's music is being played now
     * @param buttonVisible whether or not show "play/pause" button
     * @param buttonIcon which icon should the "play/pause" button have, if visible
     * @param buttonIntent which action should the "play/pause" button do, if visible
     */
    private void setWidgetState(CharSequence title, CharSequence artist, boolean buttonVisible, int buttonIcon, PendingIntent buttonIntent)
    {
        AppWidgetManager manager = AppWidgetManager.getInstance(this.context);
        ComponentName widget = new ComponentName(this.context, SwipeWidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(widget);

        PendingIntent contentIntent = this.getContentIntent();

        for (int id : ids)
        {
            RemoteViews views = new RemoteViews(this.context.getPackageName(), R.layout.screen_widget);
            views.setTextViewText(R.id.id_appwidget_title, title);
            views.setTextViewText(R.id.id_appwidget_artist, artist);
            views.setOnClickPendingIntent(R.id.id_appwidget_contet, contentIntent);

            if (buttonVisible)
            {
                views.setImageViewResource(R.id.id_appwidget_button, buttonIcon);
                views.setViewVisibility(R.id.id_appwidget_button, View.VISIBLE);
                views.setOnClickPendingIntent(R.id.id_appwidget_button, buttonIntent);
            }
            else
            {
                views.setViewVisibility(R.id.id_appwidget_button, View.GONE);
            }

            manager.updateAppWidget(id, views);
        }
    }

    /**
     * Gets the intent for "play/pause" button
     * @param action action wrapped by the resulting intent
     * @return intent instance
     */
    private PendingIntent getButtonIntent(String action)
    {
        Intent intent = new Intent(this.context, SoundService.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(this.context, 0, intent, 0);
        return pendingIntent;
    }

    /**
     * Gets the intent for the overall widget click
     * @return intent instance
     */
    private PendingIntent getContentIntent()
    {
        Intent intent = new Intent(this.context, SwipeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, intent, 0);
        return pendingIntent;
    }
}
