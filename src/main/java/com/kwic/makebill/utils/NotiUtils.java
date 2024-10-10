package com.kwic.makebill.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;

import androidx.core.app.NotificationCompat;

import com.kwic.makebill.R;

public class NotiUtils {

    public static NotificationCompat.Builder mkNotification(Context context, boolean isAutocancel, String content, PendingIntent pendingIntent) {
        String CHANNEL_ID = context.getResources().getString(R.string.app_notification_channal);
        Bitmap licon = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        return new NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.baro_app_icon_144_white) // 본인이 사용할 아이콘을 설정하자.
                .setColor(context.getResources().getColor(R.color.colorPoint))
                .setAutoCancel(isAutocancel)
                .setContentTitle("바로청구")
                .setContentText(content)
                .setLargeIcon(licon)
                .setContentIntent(pendingIntent);

    }

    public static void showNoti(Context context, NotificationCompat.Builder builder, int nofiId) {
        String CHANNEL_ID = context.getResources().getString(R.string.app_notification_channal);

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "바로청구 알림",
                    NotificationManager.IMPORTANCE_DEFAULT);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
        notificationManager.notify(nofiId, builder.build());
    }

    public static void cancelNoti(Context context, int nofiId) {
        new Handler().postDelayed(() -> {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (notificationManager != null) {
                notificationManager.cancel(nofiId);
            }
        }, 2000);
    }
}
