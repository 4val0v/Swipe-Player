/*Copyright 2014 Nikita Kobzev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.*/

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
        this.setWidgetState(audioFile.getTitle(), audioFile.getArtist(), true, R.drawable.ic_widget_pause, intent);
    }

    /**
     * Puts the screen widget to state: "Paused"
     * @param audioFile Current audio file
     */
    public void setPaused(AudioFile audioFile)
    {
        PendingIntent intent = this.getButtonIntent(SoundService.INTENT_CODE_RESUME);
        this.setWidgetState(audioFile.getTitle(), audioFile.getArtist(), true, R.drawable.ic_widget_play, intent);
    }

    /**
     * Puts the screen widget to state: "Stopped"
     */
    public void setStopped()
    {
        CharSequence title = context.getApplicationInfo().nonLocalizedLabel;
        CharSequence artist = context.getResources().getString(R.string.str_state_stopped);
        this.setWidgetState(title, artist, false, 0, null);
    }

    /**
     * Sets the small screen widget state
     * @param title Title of the currently played music
     * @param artist Music artist who's music is being played now
     * @param buttonVisible whether or not show "play/pause" button
     * @param playButtonIcon which icon should the "play/pause" button have, if visible
     * @param playButtonIntent which action should the "play/pause" button do, if visible
     */
    private void setWidgetState(CharSequence title, CharSequence artist, boolean buttonVisible, int playButtonIcon, PendingIntent playButtonIntent)
    {
        AppWidgetManager manager = AppWidgetManager.getInstance(this.context);
        ComponentName widget = new ComponentName(this.context, WidgetProvider.class);
        int[] ids = manager.getAppWidgetIds(widget);

        PendingIntent contentIntent = this.getContentIntent();
        PendingIntent nextIntent = this.getButtonIntent(SoundService.INTENT_CODE_NEXT);

        for (int id : ids)
        {
            RemoteViews views = new RemoteViews(this.context.getPackageName(), R.layout.screen_widget);
            views.setTextViewText(R.id.id_appwidget_title, title);
            views.setTextViewText(R.id.id_appwidget_artist, artist);
            views.setOnClickPendingIntent(R.id.id_appwidget_content, contentIntent);

            if (buttonVisible)
            {
                views.setImageViewResource(R.id.id_appwidget_playpause, playButtonIcon);
                views.setViewVisibility(R.id.id_appwidget_playpause, View.VISIBLE);
                views.setOnClickPendingIntent(R.id.id_appwidget_playpause, playButtonIntent);

                views.setViewVisibility(R.id.id_appwidget_ffwd, View.VISIBLE);
                views.setOnClickPendingIntent(R.id.id_appwidget_ffwd, nextIntent);
            }
            else
            {
                views.setViewVisibility(R.id.id_appwidget_playpause, View.GONE);
                views.setViewVisibility(R.id.id_appwidget_ffwd, View.GONE);
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
