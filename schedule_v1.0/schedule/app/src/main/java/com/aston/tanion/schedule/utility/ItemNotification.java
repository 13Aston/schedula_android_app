package com.aston.tanion.schedule.utility;

import android.content.Context;
import android.content.res.Resources;

import com.aston.tanion.schedule.R;

/**
 * Created by Aston Tanion on 09/04/2016.
 */
public class ItemNotification {
    private static final String TAG = "ItemNotification";


    public static String getTicker(Context context, int identifier) {

        if (identifier == Constant.IDENTIFIER_TIMETABLE) {
            return context.getResources().getString(R.string.timetable);
        } else if (identifier == Constant.IDENTIFIER_TASK) {
            return context.getResources().getString(R.string.task);
        } else {
            return null;
        }
    }

    public static String getTitle(Context context, int identifier, String itemName) {
        if (identifier == Constant.IDENTIFIER_TIMETABLE) {
            return context.getResources().getString(R.string.timetable);
        } else if (identifier == Constant.IDENTIFIER_TASK) {
            return context.getResources().getString(R.string.task) + " : " + itemName;
        } else {
            return null;
        }
    }

    public static String getContentText(
            Context context, int before, int whichTime, int identifier, String name, String location) {

        Resources resources = context.getResources();

        int hour = before / 60;
        int minute = before % 60;
        String plural = (hour > 1) ?  "s" : "";
        String notificationMessage = "";

        if (hour > 0 && minute > 0) {
            switch (whichTime) {
                case Constant.ALARM_START:

                    if (identifier == Constant.IDENTIFIER_TIMETABLE) {
                        notificationMessage = String.format(resources.getString(
                                R.string.notification_timetable_start_alarm_before),
                                name,
                                CommonMethod.timeComponent(hour),
                                CommonMethod.timeComponent(minute),
                                location);

                    } else if (identifier == Constant.IDENTIFIER_TASK) {
                        notificationMessage = String.format(resources.getString(
                                R.string.notification_task_start_alarm_before),
                                name,
                                CommonMethod.timeComponent(hour),
                                CommonMethod.timeComponent(minute),
                                location);
                    }

                    break;
                case Constant.ALARM_END:
                    notificationMessage = String.format(resources.getString(
                            R.string.notification_alarm_end_before),
                            name,
                            CommonMethod.timeComponent(hour),
                            CommonMethod.timeComponent(minute));
                    break;
                default:
                    return null;
            }

        } else if (hour > 0 && minute == 0) { // hours only

            switch (whichTime) {
                case Constant.ALARM_START:
                    if (identifier == Constant.IDENTIFIER_TIMETABLE) {
                        notificationMessage = String.format(resources.getString(
                                R.string.notification_timetable_start_alarm_before_h),
                                name,
                                hour,
                                plural,
                                location);

                    } else if (identifier == Constant.IDENTIFIER_TASK) {
                        notificationMessage = String.format(resources.getString(
                                R.string.notification_task_start_alarm_before_h),
                                name,
                                hour,
                                plural,
                                location);
                    }
                    break;
                case Constant.ALARM_END:
                    notificationMessage = String.format(resources.getString(
                            R.string.notification_alarm_end_before_h),
                            name,
                            hour,
                            plural);
                    break;
                default:
                    return null;
            }

        } else if (hour == 0 && minute > 0) {
            switch (whichTime) {
                case Constant.ALARM_START:
                    if (identifier == Constant.IDENTIFIER_TIMETABLE) {
                        notificationMessage = String.format(resources.getString(
                                R.string.notification_timetable_start_alarm_before_m),
                                name,
                                minute,
                                location);

                    } else if (identifier == Constant.IDENTIFIER_TASK) {
                        notificationMessage = String.format(resources.getString(
                                R.string.notification_task_start_alarm_before_m),
                                name,
                                minute,
                                location);
                    }
                    break;
                case Constant.ALARM_END:
                    notificationMessage = String.format(resources.getString(
                            R.string.notification_alarm_end_before_m),
                            name,
                            minute);
                    break;
                default:
                    return null;
            }
        } else {
            switch (whichTime) {
                case Constant.ALARM_START:
                    if (identifier == Constant.IDENTIFIER_TIMETABLE) {
                        notificationMessage = String.format(resources.getString(
                                R.string.notification_timetable_start_alarm_now),
                                name,
                                location);
                    } else if (identifier == Constant.IDENTIFIER_TASK) {
                        notificationMessage = String.format(resources.getString(
                                R.string.notification_task_start_alarm_now),
                                name,
                                location);
                    }
                    break;
                case Constant.ALARM_END:
                    notificationMessage = String.format(resources.getString(
                            R.string.notification_alarm_end),
                            name);
                    break;
                default:
                    return null;
            }
        }

        return notificationMessage;
    }

    public static String getDetailText(Context context, String name) {
        String message = CommonMethod.readFile(context, name);

        if (message.equals("")) return null;

        return message;
    }
}