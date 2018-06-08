package com.aston.tanion.schedule.Handler;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.aston.tanion.schedule.database.TasksLab;
import com.aston.tanion.schedule.database.TimetableLab;
import com.aston.tanion.schedule.database.WeekLab;
import com.aston.tanion.schedule.model.TaskItem;
import com.aston.tanion.schedule.model.TimetableItem;
import com.aston.tanion.schedule.model.WeekItem;
import com.aston.tanion.schedule.utility.Constant;

import java.util.List;

/**
 * Created by Aston Tanion on 24/03/2016.
 */
public class DatabaseHandler<T> extends HandlerThread {
    public static final String TAG = "DatabaseHandler";

    private TimetableLab mTimetableLab;
    private TasksLab mTasksLab;
    private WeekLab mWeekLab;
    private String mDay;
    private String mWeek;
    private String mState;

    private Handler mRequestHandler;
    private Handler mResultHandler;
    private RequestItems mRequestItems;


    public interface RequestItems {
        void onItemsRequest(List<?> items);
    }

    public void setOnItemRequest(RequestItems requestItems) {
        mRequestItems = requestItems;
    }

    public DatabaseHandler(
            Context context, Handler resultHandler, String day, String week, String state) {
        super(TAG);
        mTimetableLab = TimetableLab.get(context);
        mTasksLab = TasksLab.get(context);
        mWeekLab = WeekLab.get(context);
        mResultHandler = resultHandler;
        mDay = day;
        mWeek = week;
        mState = state;
    }

    @Override
    public synchronized void start() {
        super.start();
        Looper looper = getLooper();

        mRequestHandler = new Handler(looper) {
            @Override
            public void handleMessage(final Message msg) {

                final T object = (T) msg.obj;

                switch (msg.what) {
                    case Constant.DATABASE_ADD_ITEM:
                        if (object == null) return;

                        // Adding to the Timetable database.
                        if (msg.arg1 == Constant.IDENTIFIER_TIMETABLE) {
                            mTimetableLab.addItem((TimetableItem) object, mDay, mWeek);
                        }
                        // Adding to the Task database.
                        else if (msg.arg1 == Constant.IDENTIFIER_TASK) {
                            mTasksLab.addItem((TaskItem) object, mState);
                        }
                        // Adding to the Week database.
                        else if (msg.arg1 == Constant.IDENTIFIER_WEEK) {
                            mWeekLab.addItem((WeekItem) object);
                        }
                        break;
                    case Constant.DATABASE_REMOVE_ITEM:
                        if (object == null) return;

                        // Removing this object from the Timetable database.
                        if (msg.arg1 == Constant.IDENTIFIER_TIMETABLE) {
                            mTimetableLab.removeItem((TimetableItem) object, mDay);
                        }
                        // Removing this object from the Task database.
                        else if (msg.arg1 == Constant.IDENTIFIER_TASK) {
                            mTasksLab.removeItem((TaskItem) object, mState);
                        }
                        // Removing this object from the Week database.
                        else if (msg.arg1 == Constant.IDENTIFIER_WEEK) {
                            mWeekLab.removeItem(((WeekItem) object).getUUID().toString());
                        }
                        break;
                    case Constant.DATABASE_UPDATE_ITEM:
                        if (object == null) return;

                        // Updating this object in the Timetable database.
                        if (msg.arg1 == Constant.IDENTIFIER_TIMETABLE) {
                            mTimetableLab.updateItem((TimetableItem) object, mDay, mWeek);
                        }
                        // Updating this object in the Task database.
                        else if (msg.arg1 == Constant.IDENTIFIER_TASK) {
                            mTasksLab.updateItem((TaskItem) object, mState);
                        }
                        // Updating this object in the Week database.
                        else if (msg.arg1 == Constant.IDENTIFIER_WEEK) {
                            mWeekLab.updateItem((WeekItem) object);
                        }
                        break;
                    case Constant.DATABASE_GET_ITEMS:
                        // Requesting TimeTable list from the database.
                        if (msg.arg1 == Constant.IDENTIFIER_TIMETABLE) {
                            mResultHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mRequestItems.onItemsRequest(
                                            mTimetableLab.getItems(mDay, mWeek));
                                }
                            });
                        }
                        // Requesting TaskItem list from the database.
                        else if (msg.arg1 == Constant.IDENTIFIER_TASK) {
                            mResultHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mRequestItems.onItemsRequest(mTasksLab.getItems(mState));
                                }
                            });
                        }
                        // Requesting WeekItem list from the database.
                        else if (msg.arg1 == Constant.IDENTIFIER_WEEK) {
                            mResultHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    mRequestItems.onItemsRequest(mWeekLab.getItems());
                                }
                            });
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
     * @param what one of the database operation constant.
     * @param identifier a constant which represent a Task or a Timetable.
     */
    public void queueRequest(T obj, int identifier, int what) {

        if (obj == null) {
            mRequestHandler.obtainMessage(what, identifier, 0).sendToTarget();
        } else {
            mRequestHandler.obtainMessage(what, identifier, 0, obj).sendToTarget();
        }
    }
}