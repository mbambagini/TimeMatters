package org.timematters.misc;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import org.timematters.*;
import org.timematters.misc.DateHandler;

/**
 * This class manages the notification bar when a tracking is running
 * and the application is not on the top of the stack
 */
public class NotificationWrapper {

    private int mid = 999;

    private NotificationManager mNotificationManager = null;
    private NotificationCompat.Builder mBuilder;

    public void create (Context c) {
        Intent intent = new Intent(c, SaveActivity.class);
        PendingIntent pIntent = PendingIntent.getActivity(c, 0, intent, 0);

        mBuilder = new NotificationCompat.Builder(c)
                .setAutoCancel(true)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(c.getResources().getString(R.string.notification_title))
                .setContentText(c.getResources().getString(R.string.text_elapsed_time));

        Intent resultIntent = new Intent(c, BaseActivity.class);
        resultIntent.putExtra(c.getString(R.string.notification_intent), Long.valueOf(0));
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(c, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);


        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager =
                (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mid, mBuilder.build());
    }

    public void destroy (Context c) {
        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mid);
        mNotificationManager = null;
    }

    public void update (Context c, long duration) {
        if (mNotificationManager==null || mBuilder==null)
            return;

        Intent resultIntent = new Intent(c, BaseActivity.class);
        resultIntent.putExtra(c.getString(R.string.notification_intent), Long.valueOf(duration));
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(c, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setContentText(c.getResources().getString(R.string.text_elapsed_time)+" "+ DateConverter.GetElapsedTime(duration));
        mNotificationManager.notify(mid, mBuilder.build());
    }
}
