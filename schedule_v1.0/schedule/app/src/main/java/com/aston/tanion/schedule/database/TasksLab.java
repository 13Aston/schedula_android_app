package com.aston.tanion.schedule.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aston.tanion.schedule.model.TaskItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Aston Tanion on 05/02/2016.
 */
public class TasksLab {
    public static final String TAG = "TasksLab";
    private static TasksLab sTasksLab;
    private SQLiteDatabase mDatabase;

    protected static class ItemTable {
        public static final String NAME = "TASK_TABLE";

        public static final class Cols {
            // Task unique identification.
            public static final String UUID = "ID";
            // Task title
            public static final String TITLE = "SUBJECT";
            // Task location.
            public static final String LOCATION = "LOCATION";
            // Time which the task have been set to start.
            public static final String TIME = "TIME";
            // Date which the task have been set to start.
            public static final String SET_DATE = "SET_DATE";
            // Wake up call time
            public static final String CALL_TIME = "CALL_TIME";
            // Should we she the wake up call
            public static final String CALL_TIME_STATE = "CALL_TIME_STATE";
            // Date which the task have been completed.
            public static final String COMPLETED_DATE = "COMPLETED_DATE";
            // Alarm state of the task (on or off).
            public static final String STATE = "STATE";
            // Interval of time which the alarm go off before the actual alarm set time.
            public static final String DELTA_TIME = "DELTA_TIME";
            // State of the task (ongoing or completed)
            public static final String TASK_STATE = "TASK_STATE";
            // Interval in which the task changes color.
            public static final String INTERVAL = "INTERVAL";
            // Boolean which decide whether to notify priority change
            public static final String PRIORITY_STATE = "PRIORITY";
            // Alarm type of a task
            public static final String TYPE = "TYPE";
        }
    }

    private class TaskDbHelper extends SQLiteOpenHelper {
        private static final int VERSION = 1;
        private static final String DATABASE_NAME = "task.db";

        public TaskDbHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + ItemTable.NAME + "(" +
                    " _id integer primary key autoincrement, " +
                    ItemTable.Cols.UUID + ", " +
                    ItemTable.Cols.TITLE + ", " +
                    ItemTable.Cols.LOCATION + ", " +
                    ItemTable.Cols.TIME + ", " +
                    ItemTable.Cols.SET_DATE + ", " +
                    ItemTable.Cols.CALL_TIME + ", " +
                    ItemTable.Cols.CALL_TIME_STATE + ", " +
                    ItemTable.Cols.COMPLETED_DATE + ", " +
                    ItemTable.Cols.STATE + ", " +
                    ItemTable.Cols.DELTA_TIME + ", " +
                    ItemTable.Cols.TASK_STATE + ", " +
                    ItemTable.Cols.INTERVAL + ", " +
                    ItemTable.Cols.PRIORITY_STATE + ", " +
                    ItemTable.Cols.TYPE +
                    ")"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Do nothing
        }
    }

    private class TaskCursorWrapper extends CursorWrapper {

        public TaskCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public TaskItem getItem() {
            String uuidString = getString(getColumnIndexOrThrow(ItemTable.Cols.UUID));
            String title = getString(getColumnIndexOrThrow(ItemTable.Cols.TITLE));
            String location = getString(getColumnIndexOrThrow(ItemTable.Cols.LOCATION));
            String alarmStateString = getString(getColumnIndexOrThrow(ItemTable.Cols.STATE));
            String shouldRemindPriorityString = getString(getColumnIndexOrThrow(ItemTable.Cols.PRIORITY_STATE));
            String shouldWakeUpCallString = getString(getColumnIndexOrThrow(ItemTable.Cols.CALL_TIME_STATE));
            String state = getString(getColumnIndexOrThrow(ItemTable.Cols.TASK_STATE));

            int time = getInt(getColumnIndexOrThrow(ItemTable.Cols.TIME));
            int alarmBefore = getInt(getColumnIndexOrThrow(ItemTable.Cols.DELTA_TIME));
            int alarmTypeChoice = getInt(getColumnIndexOrThrow(ItemTable.Cols.TYPE));
            int wakeUpCallTime = getInt(getColumnIndexOrThrow(ItemTable.Cols.CALL_TIME));

            long interval = getInt(getColumnIndexOrThrow(ItemTable.Cols.INTERVAL));
            long setDate = getLong(getColumnIndexOrThrow(ItemTable.Cols.SET_DATE));
            long completedDate = getLong(getColumnIndexOrThrow(ItemTable.Cols.COMPLETED_DATE));

            boolean shouldWakeUpCall = false;
            if (shouldWakeUpCallString.equals("true")) {
                shouldWakeUpCall = true;
            } else if (alarmStateString.equals("false")) {
                shouldWakeUpCall = false;
            }

            boolean shouldRemindPriority = false;
            if (shouldRemindPriorityString.equals("true")) {
                shouldRemindPriority = true;
            } else if (alarmStateString.equals("false")) {
                shouldRemindPriority = false;
            }

            boolean isAlarmStartStateChecked = false;
            if (alarmStateString.equals("true")) {
                isAlarmStartStateChecked = true;
            } else if (alarmStateString.equals("false")) {
                isAlarmStartStateChecked = false;
            }

            TaskItem item = new TaskItem(UUID.fromString(uuidString));
            item.setTitle(title);
            item.setLocation(location);
            item.setTime(time);
            item.setWakeUpCallTime(wakeUpCallTime);
            item.setAlarmStateChecked(isAlarmStartStateChecked);
            item.setDueDate(new Date(setDate));
            item.setCompleteDate(new Date(completedDate));
            item.setDeltaTime(alarmBefore);
            item.setState(state);
            item.setIntervalTime(interval);
            item.setShouldRemindPriority(shouldRemindPriority);
            item.setAlarmTypeChoice(alarmTypeChoice);
            item.setShouldWakeUpCall(shouldWakeUpCall);

            return item;
        }
    }


    public static TasksLab get(Context context) {
        if (sTasksLab == null) {
            sTasksLab = new TasksLab(context.getApplicationContext());
        }
        return sTasksLab;
    }

    private TasksLab(Context context) {
        mDatabase = new TaskDbHelper(context).getWritableDatabase();
    }

    public List<TaskItem> getItems(String state) {
        List<TaskItem> items = new ArrayList<>();
        if (state == null) return items;

        TaskCursorWrapper cursor = queryItems(
                ItemTable.NAME, // table
                null, // columns[]
                ItemTable.Cols.TASK_STATE + " = ?", // selection
                new String[]{state}, // selectionArg[]
                null, // groupBy
                null, // having
                ItemTable.Cols.SET_DATE, // orderBy
                null
        ); // limit

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                items.add(cursor.getItem());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return items;
    }

    public TaskItem getItem(UUID id) {
        TaskCursorWrapper cursor = queryItems(
                ItemTable.NAME, // table
                null, // columns[]
                ItemTable.Cols.UUID + " = ?", // selection
                new String[]{id.toString()}, // selectionArg[]
                null, // groupBy
                null, // having
                ItemTable.Cols.SET_DATE, // orderBy
                null); // limit

       try {
           if (cursor.getCount() == 0) {
               return null;
           }
           cursor.moveToFirst();
           return cursor.getItem();
       } finally {
           cursor.close();
       }
    }

    public void addItem(TaskItem item, String state) {
        ContentValues values = getContentValues(item);
        mDatabase.insert(
                ItemTable.NAME,
                null,
                values
        );
    }

    public void updateItem(TaskItem item, String state) {
        ContentValues values = getContentValues(item);
        mDatabase.update(
                ItemTable.NAME,
                values,
                ItemTable.Cols.UUID + " = ?",
                new String[]{item.getUUID().toString()}
        );
    }

    public void removeItem(TaskItem item, String state) {
        mDatabase.delete(
                ItemTable.NAME,
                ItemTable.Cols.UUID + " = ?",
                new String[]{item.getUUID().toString()}
        );
    }

    // This is for debugging purposes
    public int getSize() {
        return mDatabase.query(ItemTable.NAME, null, null, null, null, null, null).getCount();
    }

    private ContentValues getContentValues(TaskItem item) {
        ContentValues values = new ContentValues();

        values.put(ItemTable.Cols.UUID, item.getUUID().toString()); // string
        values.put(ItemTable.Cols.TITLE, item.getTitle()); // string
        values.put(ItemTable.Cols.LOCATION, item.getLocation()); // string
        values.put(ItemTable.Cols.TASK_STATE, item.getState()); // string
        values.put(ItemTable.Cols.TIME, item.getTime()); // int
        values.put(ItemTable.Cols.SET_DATE, item.getDueDate().getTime()); // long
        values.put(ItemTable.Cols.COMPLETED_DATE, item.getCompleteDate().getTime()); // long
        values.put(ItemTable.Cols.STATE, Boolean.toString(item.getAlarmStateChecked())); // string.
        values.put(ItemTable.Cols.DELTA_TIME, item.getDeltaTime());
        values.put(ItemTable.Cols.INTERVAL, item.getIntervalTime());
        values.put(ItemTable.Cols.PRIORITY_STATE, Boolean.toString(item.shouldRemindPriority()));
        values.put(ItemTable.Cols.CALL_TIME, item.getWakeUpCallTime());
        values.put(ItemTable.Cols.TYPE, item.getAlarmTypeChoice()); // int
        values.put(ItemTable.Cols.CALL_TIME_STATE, Boolean.toString(item.shouldWakeUpCall()));

        return values;
    }

    private TaskCursorWrapper queryItems(
            String table, String[] columns, String selection, String[] selectionArgs,
            String groupBy, String having, String orderBy, String limit) {

        Cursor cursor = mDatabase.query(
                table,
                columns,
                selection,
                selectionArgs,
                groupBy,
                having,
                orderBy,
                limit
        );

        return new TaskCursorWrapper(cursor);
    }
}