package org.timematters.misc;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import org.timematters.R;
import org.timematters.activities.MainActivity;

/**
 * This class manages the notification bar when a tracking is running
 * and the application is not on the top of the stack
 */
public class NotificationWrapper {

    /**
     * Notification identifier
     */
    private int mid = 999;

    private NotificationManager mNotificationManager = null;

    /**
     * Create and show a notification
     *
     * @param c application context
     * @param active_or_pause TRUE means that an activity is running, FALSE when it is paused
     */
    public void create(Context c, boolean active_or_pause ) {
        //Intent intent = new Intent(c, SaveActivity.class);
        //PendingIntent pIntent = PendingIntent.getActivity(c, 0, intent, 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(c)
                .setAutoCancel(true)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(c.getResources().getString(R.string.notification_title));

        if (active_or_pause)
            mBuilder.setContentText(c.getResources().getString(R.string.notification_subtitle_run));
        else
            mBuilder.setContentText(c.getResources().getString(R.string.notification_subtitle_pause));

        Intent resultIntent = new Intent(c, MainActivity.class);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(c, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        mNotificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(mid, mBuilder.build());
    }

    /**
     * Destroy the notification object, removing it from the notification area
     * 
     * @param c context
     */
    public void destroy (Context c) {
        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(mid);
        mNotificationManager = null;
    }

}
