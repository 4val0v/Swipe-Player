package net.illusor.swipeplayer.helpers;

import android.R;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import net.illusor.swipeplayer.activities.SwipeActivity;
import net.illusor.swipeplayer.domain.AudioFile;

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

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this.context);
        Notification notification = builder.setContentTitle("Playing now")
                                           .setContentText(file.getTitle())
                                           .setSmallIcon(R.drawable.ic_media_play)
                                           .setContentIntent(pendingIntent)
                                           .build();

        return notification;
    }
}
