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

package net.illusor.swipeplayer.helpers;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v4.app.NotificationCompat;
import net.illusor.swipeplayer.R;
import net.illusor.swipeplayer.activities.SwipeActivity;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.services.SoundService;

/**
 * Provides methods for displaying system notifications
 */
public class NotificationHelper
{
    private final Context context;

    public NotificationHelper(Context context)
    {
        this.context = context;
    }

    /**
     * Creates notification: "Playing audio.."
     * @param file Playing audio file
     * @return Notification instance
     */
    public Notification getPlayingNotification(AudioFile file)
    {
        PendingIntent activityIntent = this.getActivityIntent();
        PendingIntent pauseIntent = this.getActionIntent(SoundService.INTENT_CODE_PAUSE);
        PendingIntent stopIntent = this.getActionIntent(SoundService.INTENT_CODE_STOP);
        Resources r = this.context.getResources();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
        Notification notification = builder.setContentTitle(r.getString(R.string.str_state_playing))
                                           .setContentText(file.getTitle())
                                           .setSmallIcon(android.R.drawable.ic_media_play)
                                           .setContentIntent(activityIntent)
                                           .addAction(android.R.drawable.ic_media_pause, r.getString(R.string.str_action_pause), pauseIntent)
                                           .addAction(android.R.drawable.ic_menu_close_clear_cancel, r.getString(R.string.str_action_stop), stopIntent)
                                           .build();

        return notification;
    }

    /**
     * Creates notification: "Audio paused.."
     * @param file Paused audio file
     * @return Notification instance
     */
    public Notification getPausedNotification(AudioFile file)
    {
        PendingIntent activityIntent = this.getActivityIntent();
        PendingIntent resumeIntent = this.getActionIntent(SoundService.INTENT_CODE_RESUME);
        PendingIntent stopIntent = this.getActionIntent(SoundService.INTENT_CODE_STOP);
        Resources r = this.context.getResources();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
        Notification notification = builder.setContentTitle(r.getString(R.string.str_state_paused))
                .setContentText(file.getTitle())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(activityIntent)
                .addAction(android.R.drawable.ic_media_play, r.getString(R.string.str_action_resume), resumeIntent)
                .addAction(android.R.drawable.ic_menu_close_clear_cancel, r.getString(R.string.str_action_stop), stopIntent)
                .build();

        return notification;
    }

    /**
     * Creates notification: "Audio stopped.."
     * @return Notification instance
     */
    public Notification getStoppedNotification()
    {
        PendingIntent activityIntent = this.getActivityIntent();
        Resources r = this.context.getResources();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
        Notification notification = builder.setContentTitle(this.context.getApplicationInfo().nonLocalizedLabel)
                .setContentText(r.getString(R.string.str_state_stopped))
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(activityIntent)
                .build();

        return notification;
    }

    /**
     * Creates notification: "Error playing file.."
     * @param file Audio file which caused an error
     * @return Notification
     */
    public Notification getErrorNotification(AudioFile file)
    {
        Resources r = this.context.getResources();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
        Notification notification = builder.setContentTitle(r.getString(R.string.str_state_error))
                .setContentText(file.getTitle())
                .setSmallIcon(android.R.drawable.ic_media_play)
                .build();

        return notification;
    }

    /**
     * Creates an instance of {@link PendingIntent}, which opens the {@link SwipeActivity} on activation
     * @return Pending intent instance
     */
    private PendingIntent getActivityIntent()
    {
        Intent intent = new Intent(this.context, SwipeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, intent, 0);
        return pendingIntent;
    }

    /**
     * Creates an instance of {@link PendingIntent} which sends an action intent on activation
     * @param action Action to send on activation
     * @return Pending intent instance
     */
    private PendingIntent getActionIntent(String action)
    {
        Intent intent = new Intent(this.context, SoundService.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(this.context, 0, intent, 0);
        return pendingIntent;
    }
}
