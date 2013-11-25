package net.illusor.swipeplayer.helpers;

import android.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import net.illusor.swipeplayer.activities.SwipeActivity;
import net.illusor.swipeplayer.domain.AudioFile;
import net.illusor.swipeplayer.services.SoundService;

public class NotificationHelper
{
    private Context context;

    public NotificationHelper(Context context)
    {
        this.context = context;
    }

    public Notification getPlayingNotification(AudioFile file)
    {
        PendingIntent activityIntent = this.getActivityIntent();
        PendingIntent pauseIntent = this.getActionIntent(SoundService.INTENT_CODE_PAUSE);
        PendingIntent stopIntent = this.getActionIntent(SoundService.INTENT_CODE_STOP);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
        Notification notification = builder.setContentTitle("Playing now")
                                           .setContentText(file.getTitle())
                                           .setSmallIcon(R.drawable.ic_media_play)
                                           .setContentIntent(activityIntent)
                                           .addAction(R.drawable.ic_media_pause, "Pause", pauseIntent)
                                           .addAction(R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
                                           .build();

        return notification;
    }

    public Notification getPausedNotification(AudioFile file)
    {
        PendingIntent activityIntent = this.getActivityIntent();
        PendingIntent resumeIntent = this.getActionIntent(SoundService.INTENT_CODE_RESUME);
        PendingIntent stopIntent = this.getActionIntent(SoundService.INTENT_CODE_STOP);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
        Notification notification = builder.setContentTitle("Paused")
                .setContentText(file.getTitle())
                .setSmallIcon(R.drawable.ic_media_play)
                .setContentIntent(activityIntent)
                .addAction(R.drawable.ic_media_play, "Resume", resumeIntent)
                .addAction(R.drawable.ic_menu_close_clear_cancel, "Stop", stopIntent)
                .build();

        return notification;
    }

    public Notification getStoppedNotification()
    {
        PendingIntent activityIntent = this.getActivityIntent();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
        Notification notification = builder.setContentTitle(this.context.getApplicationInfo().nonLocalizedLabel)
                .setContentText("Playing stopped")
                .setSmallIcon(R.drawable.ic_media_play)
                .setContentIntent(activityIntent)
                .build();

        return notification;
    }

    private PendingIntent getActivityIntent()
    {
        Intent intent = new Intent(this.context, SwipeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, intent, 0);
        return pendingIntent;
    }

    private PendingIntent getActionIntent(String action)
    {
        Intent intent = new Intent(this.context, SoundService.class);
        intent.setAction(action);
        PendingIntent pendingIntent = PendingIntent.getService(this.context, 0, intent, 0);
        return pendingIntent;
    }
}
