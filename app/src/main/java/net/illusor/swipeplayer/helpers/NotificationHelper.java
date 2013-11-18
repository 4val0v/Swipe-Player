package net.illusor.swipeplayer.helpers;

import android.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;
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

    public Notification getAudioNotification(AudioFile file)
    {
        Intent intent = new Intent(this.context, SwipeActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this.context, 0, intent, 0);

        Intent stopIntent = new Intent(this.context, SoundService.class);
        stopIntent.setAction(SoundService.INTENT_CODE_STOP);
        PendingIntent stopPendingIntent = PendingIntent.getService(this.context, 0, stopIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
        Notification notification = builder.setContentTitle("Playing now")
                                           .setContentText(file.getTitle())
                                           .setSmallIcon(R.drawable.ic_media_play)
                                           .setContentIntent(pendingIntent)
                                           .addAction(R.drawable.ic_menu_close_clear_cancel, "Stop", stopPendingIntent)
                                           .build();

        return notification;
    }
}
