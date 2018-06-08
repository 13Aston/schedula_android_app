package com.aston.tanion.schedule.Handler;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.aston.tanion.schedule.model.TaskItem;
import com.aston.tanion.schedule.model.TimetableItem;
import com.aston.tanion.schedule.service.AlarmService;
import com.aston.tanion.schedule.utility.Constant;

/**
 * Created by Aston Tanion on 27/07/2016.
 */
public class AlarmHandler<T> extends HandlerThread {
    public static final String TAG = "AlarmHandler";

    private Context mContext;
    private Handler mRequestHandler;
    private String mDay;

    public AlarmHandler(Context context, String day) {
        super(TAG);
        mContext = context;
        mDay = day;
    }

    @Override
    public synchronized void start() {
        super.start();
        Looper looper = getLooper();

        mRequestHandler = new Handler(looper) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                T obj = (T) msg.obj;

                switch (msg.what) {
                    case Constant.ALARM_ADD:
                       if (msg.arg1 == Constant.IDENTIFIER_TIMETABLE) {

                           AlarmService.TimetableAlarm
                                   .addAlarm(mContext, (TimetableItem) obj, msg.arg2, mDay);

                       } else if (msg.arg1 == Constant.IDENTIFIER_TASK) {

                           AlarmService.TaskAlarm.addAlarm(mContext, (TaskItem) obj);

                       }

                        break;
                    case Constant.ALARM_UPDATE:
                        if (msg.arg1 == Constant.IDENTIFIER_TIMETABLE) {
                            AlarmService.TimetableAlarm
                                    .updateAlarm(mContext, (TimetableItem) obj, msg.arg2, mDay);

                        }
                        break;
                    case Constant.ALARM_REMOVE:
                        if (msg.arg1 == Constant.IDENTIFIER_TIMETABLE) {

                            AlarmService.TimetableAlarm
                                    .removeAlarm(mContext, (TimetableItem) obj);

                        } else if (msg.arg1 == Constant.IDENTIFIER_TASK) {

                            AlarmService.TaskAlarm.removeAlarm(mContext, (TaskItem) obj);

                        }

                        break;
                    case Constant.ALARM_CLEAR:
                        if (msg.arg1 == Constant.IDENTIFIER_TIMETABLE) {
                            AlarmService.TimetableAlarm
                                    .clearAlarm(mContext, (TimetableItem) obj, msg.arg2, mDay);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }

    /**
     * @param obj this could be either a timetableItem or a TaskItem.
     * @param what one of the alarm operation constant.
     * @param identifier a constant which represent a Task or a Timetable.
     * @param which a constant which represent a start or end of an alarm
     */
    public void queueMessage(T obj, int what, int identifier, int which) {
        if (!isAlive() || obj == null) return;
        mRequestHandler.obtainMessage(what, identifier, which, obj).sendToTarget();
    }
}