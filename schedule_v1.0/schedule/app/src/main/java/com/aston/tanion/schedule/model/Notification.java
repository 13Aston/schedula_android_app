package com.aston.tanion.schedule.model;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.database.SharedPrefs;
import com.aston.tanion.schedule.utility.CommonMethod;
import com.aston.tanion.schedule.utility.Constant;

/**
 * Created by Aston Tanion on 23/02/2016.
 */
public class Notification {
    public static final String TAG = "Notification";

    private static Notification sNotification;
    private NotificationCompat.Builder mBuilder;
    private Context mContext;

    public static Notification get(Context context) {
        if (sNotification == null) {
            sNotification = new Notification(context);
        }
        return sNotification;
    }

    private Notification(Context context) {
        mContext = context;
    }

    public Notification createNotification(PendingIntent intent){
        mBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(intent)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setAutoCancel(true);

        return sNotification;
    }

    public Notification setType(int type) {
        switch (type) {
            case Constant.NOTIFICATION_DEFAULT:
                mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
                break;
            case Constant.NOTIFICATION_SOUND:
                // Get the sound preference value.
                String soundUri = (String) SharedPrefs.get(mContext).read(Constant.SOUND_PREF, "");
                mBuilder.setSound(Uri.parse(soundUri));
                break;
            case Constant.NOTIFICATION_VIBRATOR:
                int choice = Integer.parseInt(
                        (String) SharedPrefs.get(mContext).read(Constant.VIBRATOR_PREF, "0"));

                if (choice == 0) {
                    mBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
                } else {
                   long[] pattern = CommonMethod.getVibrator(choice);

                    if (pattern != null) {
                        mBuilder.setVibrate(pattern);
                    } else {
                        mBuilder.setDefaults(NotificationCompat.DEFAULT_VIBRATE);
                    }
                }
                break;
            default:
                mBuilder.setDefaults(NotificationCompat.DEFAULT_ALL);
        }

        return sNotification;
    }

    public Notification addTicker(String ticker) {
        mBuilder.setTicker(ticker);
        return sNotification;
    }

    public Notification addTitle(String title) {
        mBuilder.setContentTitle(title);
        return sNotification;
    }

    public Notification addContentText(String message) {
        mBuilder.setContentText(message);
        return sNotification;
    }

    public Notification addStyle(String text) {
        mBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
        return sNotification;
    }

    public Notification addGroup(String key) {
        mBuilder.setGroup(key).setGroupSummary(true);
        return sNotification;
    }

    public void ShowNotification(int id) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        notificationManager.notify(id, mBuilder.build());
    }
}