package com.potatomasterextreme.personnel.infrastructure;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.potatomasterextreme.personnel.MainActivity;
import com.potatomasterextreme.personnel.R;

public class NotificationMaster {

    final static String CHANNEL_ID = "0";
    static int notification_id_count = 0;
    public static final int PRIORITY_DEFAULT = 0;
    public static final int PRIORITY_LOW = -1;
    public static final int PRIORITY_MIN = -2;
    public static final int PRIORITY_HIGH = 1;
    public static final int PRIORITY_MAX = 2;

    public static void createNotification(final Context context, final String notificationTitle, final String notificationMessage, int priority, int notification_id, @Nullable Intent intent) {
        createNotificationChannel(context, priority + 3);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_icon)
                .setColor(context.getResources().getColor(R.color.textColor))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),
                        R.mipmap.ic_launcher_new))
                .setContentTitle(notificationTitle)
                .setContentText(notificationMessage)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(notificationMessage))
                .setPriority(priority)
                .setAutoCancel(true);

        if (intent != null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            mBuilder.setContentIntent(pendingIntent);
        }
        //.setDefaults(Notification.DEFAULT_ALL)

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(notification_id, mBuilder.build());
    }

    private static void createNotificationChannel(Context context, int importance) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "CHANNEL_NAME";
            String description = "CHANNEL_DESCRIPTION";
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
