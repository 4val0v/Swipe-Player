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
        this.setSmallWidgetState(audioFile.getTitle(), audioFile.getArtist(), true, android.R.drawable.ic_media_pause, intent);
        this.setFullWidgetState(audioFile.getTitle(), audioFile.getArtist(), true, android.R.drawable.ic_media_pause, intent);
    }

    /**
     * Puts the screen widget to state: "Paused"
     * @param audioFile Current audio file
     */
    public void setPaused(AudioFile audioFile)
    {
        PendingIntent intent = this.getButtonIntent(SoundService.INTENT_CODE_RESUME);
        this.setSmallWidgetState(audioFile.getTitle(), audioFile.getArtist(), true, android.R.drawable.ic_media_play, intent);
        this.setFullWidgetState(audioFile.getTitle(), audioFile.getArtist(), true, android.R.drawable.ic_media_play, intent);
    }

    /**
     * Puts the screen widget to state: "Stopped"
     */
    public void setStopped()
    {
        CharSequence title = context.getApplicationInfo().nonLocalizedLabel;
        CharSequence artist = context.getResources().getString(R.string.str_state_stopped);
        this.setSmallWidgetState(title, artist, false, 0, null);
        this.setFullWidgetState(title, artist, false, 0, null);
    }

    /**
     * Sets the small screen widget state
     * @param title Title of the currently played music
     * @param artist Music artist who's music is being played now
     * @param buttonVisible whether or not show "play/pause" button
     * @param buttonIcon which icon should the "play/pause" button have, if visible
     * @param buttonIntent which action should the "play/pause" button do, if visible
     */
    private void setSmallWidgetState(CharSequence title, CharSequence artist, boolean buttonVisible, int buttonIcon, PendingIntent buttonIntent)
    {
        AppWidgetManager manager = AppWidgetManager.getInstance(this.context);
        ComponentName widget = new ComponentName(this.context, WidgetSmallProvider.class);
        int[] ids = manager.getAppWidgetIds(widget);

        PendingIntent contentIntent = this.getContentIntent();

        for (int id : ids)
        {
            RemoteViews views = new RemoteViews(this.context.getPackageName(), R.layout.screen_widget_small);
            views.setTextViewText(R.id.id_appwidget_title, title);
            views.setTextViewText(R.id.id_appwidget_artist, artist);
            views.setOnClickPendingIntent(R.id.id_appwidget_content, contentIntent);

            if (buttonVisible)
            {
                views.setImageViewResource(R.id.id_appwidget_playpause_small, buttonIcon);
                views.setViewVisibility(R.id.id_appwidget_playpause_small, View.VISIBLE);
                views.setOnClickPendingIntent(R.id.id_appwidget_playpause_small, buttonIntent);
            }
            else
            {
                views.setViewVisibility(R.id.id_appwidget_playpause_small, View.GONE);
            }

            manager.updateAppWidget(id, views);
        }
    }

    /**
     * Sets the full screen widget state
     * @param title Title of the currently played music
     * @param artist Music artist who's music is being played now
     * @param buttonsEnabled whether or not enable widget buttons
     * @param playPauseIcon which icon should the "play/pause" button have, if visible
     * @param playPauseIntent which action should the "play/pause" button do, if visible
     */
    private void setFullWidgetState(CharSequence title, CharSequence artist, boolean buttonsEnabled, int playPauseIcon, PendingIntent playPauseIntent)
    {
        AppWidgetManager manager = AppWidgetManager.getInstance(this.context);
        ComponentName widget = new ComponentName(this.context, WidgetFullProvider.class);
        int[] ids = manager.getAppWidgetIds(widget);

        PendingIntent contentIntent = this.getContentIntent();

        for (int id : ids)
        {
            RemoteViews views = new RemoteViews(this.context.getPackageName(), R.layout.screen_widget_full);
            views.setTextViewText(R.id.id_appwidget_title, title);
            views.setTextViewText(R.id.id_appwidget_artist, artist);
            views.setOnClickPendingIntent(R.id.id_appwidget_content, contentIntent);

            if (buttonsEnabled)
            {
                views.setViewVisibility(R.id.id_appwidget_enabled, View.VISIBLE);

                views.setImageViewResource(R.id.id_appwidget_playpause_full, playPauseIcon);
                views.setOnClickPendingIntent(R.id.id_appwidget_playpause_full, playPauseIntent);

                PendingIntent nextIntent = this.getButtonIntent(SoundService.INTENT_CODE_NEXT);
                PendingIntent prevIntent = this.getButtonIntent(SoundService.INTENT_CODE_PREVIOUS);

                views.setOnClickPendingIntent(R.id.id_appwidget_next, nextIntent);
                views.setOnClickPendingIntent(R.id.id_appwidget_prev, prevIntent);
            }
            else
            {
                views.setViewVisibility(R.id.id_appwidget_enabled, View.GONE);
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
