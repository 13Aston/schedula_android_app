package com.aston.tanion.schedule.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aston.tanion.schedule.model.TimetableItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by Aston Tanion on 05/02/2016.
 */
public class TimetableLab {
    public static final String TAG = "TimetableLab";
    private static TimetableLab sTimetableLab;
    private SQLiteDatabase mDatabase;


    private static final class ItemTable {
        public static final String NAME = "TIME_TABLE";

        public static final class Cols {

            // Item unique identification.
            public static final String UUID = "ID";
            // Day in which this item belongs to.
            public static final String DAY = "DAY";
            // Week in which this item belongs to.
            public static final String WEEK = "WEEK";
            // Position of the item relative to other item.
            public static final String POSITION = "POSITION";
            // Item title/subject.
            public static final String SUBJECT = "SUBJECT";
            // Item location.
            public static final String LOCATION = "LOCATION";
            // Alarm start state of the item (on or off).
            public static final String START_STATE = "START_STATE";
            // Alarm end state of the item (on or off).
            public static final String END_STATE = "END_STATE";
            // Format in which the time is represented, i.e 20:45-21:00.
            public static final String TIME_FORMAT = "TIME_FORMAT";
            // Time which the item have been set to start.
            public static final String START_TIME = "START_TIME";
            // Time which the item have been set to end.
            public static final String END_TIME = "END_TIME";
            // Interval of time which the alarm go off before the actual alarm set time.
            public static final String DELTA_TIME = "DELTA_TIME";
            // Alarm type for the item.
            public static final String TYPE = "TYPE";
        }
    }

    private class TimetableDbHelper extends SQLiteOpenHelper {
        private static final int VERSION = 1;
        private static final String DATABASE_NAME = "timetable.db";

        public TimetableDbHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " +
                    ItemTable.NAME + "(" +
                    " _id integer primary key autoincrement, " +
                    ItemTable.Cols.UUID + ", " +
                    ItemTable.Cols.DAY + ", " +
                    ItemTable.Cols.WEEK + ", " +
                    ItemTable.Cols.POSITION + ", " +
                    ItemTable.Cols.SUBJECT + ", " +
                    ItemTable.Cols.LOCATION + ", " +
                    ItemTable.Cols.START_STATE + ", " +
                    ItemTable.Cols.END_STATE + ", " +
                    ItemTable.Cols.START_TIME + ", " +
                    ItemTable.Cols.END_TIME + ", " +
                    ItemTable.Cols.DELTA_TIME + ", " +
                    ItemTable.Cols.TYPE + ", " +
                    ItemTable.Cols.TIME_FORMAT +
                    ")"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Do nothing.
        }
    }

    private class TimetableCursorWrapper extends CursorWrapper {

        public TimetableCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public TimetableItem getItem() {
            String uuidString = getString(getColumnIndexOrThrow(ItemTable.Cols.UUID));
            String title = getString(getColumnIndexOrThrow(ItemTable.Cols.SUBJECT));
            String location = getString(getColumnIndexOrThrow(ItemTable.Cols.LOCATION));
            String timeFormat = getString(getColumnIndexOrThrow(ItemTable.Cols.TIME_FORMAT));
            String alarmStartStateString = getString(getColumnIndexOrThrow(ItemTable.Cols.START_STATE));
            String alarmEndStateString = getString(getColumnIndexOrThrow(ItemTable.Cols.END_STATE));

            int startTime = getInt(getColumnIndexOrThrow(ItemTable.Cols.START_TIME));
            int endTime = getInt(getColumnIndexOrThrow(ItemTable.Cols.END_TIME));
            int alarmBefore = getInt(getColumnIndexOrThrow(ItemTable.Cols.DELTA_TIME));
            int alarmType = getInt(getColumnIndexOrThrow(ItemTable.Cols.TYPE));

            UUID id = UUID.fromString(uuidString);
            boolean isAlarmStartStateChecked = false;
            if (alarmStartStateString.equals("true")) {
                isAlarmStartStateChecked = true;
            } else if (alarmStartStateString.equals("false")) {
                isAlarmStartStateChecked = false;
            }

            boolean isAlarmEndStateChecked = false;
            if (alarmEndStateString.equals("true")) {
                isAlarmEndStateChecked = true;
            } else if (alarmEndStateString.equals("false")) {
                isAlarmEndStateChecked = false;
            }

            TimetableItem item = new TimetableItem(id);
            item.setTitle(title);
            item.setLocation(location);
            item.setTimeFormat(timeFormat);
            item.setAlarmStartState(isAlarmStartStateChecked);
            item.setAlarmEndState(isAlarmEndStateChecked);

            item.setStartTime(startTime);
            item.setEndTime(endTime);
            item.setDeltaTime(alarmBefore);
            item.setAlarmTypeChoice(alarmType);
            return item;
        }
    }

    public static TimetableLab get(Context context) {
        if (sTimetableLab == null) {
            sTimetableLab = new TimetableLab(context.getApplicationContext());
        }
        return sTimetableLab;
    }

    private TimetableLab(Context context) {
        mDatabase = new TimetableDbHelper(context).getWritableDatabase();
    }

    public List<TimetableItem> getItems(String day, String week) {
        List<TimetableItem> items = new ArrayList<>();

        TimetableCursorWrapper cursorWrapper = queryItems(
                ItemTable.NAME, // table
                null,   // columns[]
                ItemTable.Cols.DAY + " = ?" + " AND " + ItemTable.Cols.WEEK + " = ?", // selection
                new String[]{day, week},   // selectionArg[]
                null,   // groupBy
                null,   // having
                ItemTable.Cols.POSITION,   // orderBy
                null    // limit
        );

        try {
            cursorWrapper.moveToFirst();
            while (!cursorWrapper.isAfterLast()) {
                items.add(cursorWrapper.getItem());
                cursorWrapper.moveToNext();
            }
        } finally {
            cursorWrapper.close();
        }

        return items;
    }

    public TimetableItem getItem(UUID id, String day, String week) {
        TimetableCursorWrapper cursor = queryItems(
                ItemTable.NAME,     // table
                null,   // columns[]
                ItemTable.Cols.UUID + " = ?",   // selection
                new String[] {id.toString()},   // selectionArg[]
                null,     // groupBy
                null,   // having
                ItemTable.Cols.POSITION,   // orderBy
                null    // limit
        );

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

    public void addItem(TimetableItem item, String day, String week) {
        ContentValues values = getContentValues(item, day, week);
        mDatabase.insert(
                ItemTable.NAME,
                null,
                values
        );
    }

    public void updateItem(TimetableItem item, String day, String week) {
        ContentValues values = getContentValues(item, day, week);
        mDatabase.update(
                ItemTable.NAME,
                values,
                ItemTable.Cols.UUID + " = ?",
                new String[]{item.getUUID().toString()}
        );
    }

    public void removeItem(TimetableItem item, String day) {
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

    private static ContentValues getContentValues(TimetableItem item, String day, String week) {
        ContentValues values = new ContentValues();

        values.put(ItemTable.Cols.DAY, day); // string
        values.put(ItemTable.Cols.WEEK, week); // string
        values.put(ItemTable.Cols.UUID, item.getUUID().toString()); // string
        values.put(ItemTable.Cols.SUBJECT, item.getTitle()); // string
        values.put(ItemTable.Cols.LOCATION, item.getLocation()); // string
        values.put(ItemTable.Cols.TIME_FORMAT, item.getTimeFormat()); // string
        values.put(ItemTable.Cols.START_STATE, Boolean.toString(item.getAlarmStartState())); // string
        values.put(ItemTable.Cols.END_STATE, Boolean.toString(item.getAlarmEndState())); // string

        values.put(ItemTable.Cols.START_TIME, item.getStartTime()); // integer
        values.put(ItemTable.Cols.END_TIME, item.getEndTime()); // integer
        values.put(ItemTable.Cols.DELTA_TIME, item.getDeltaTime()); // integer
        values.put(ItemTable.Cols.POSITION, item.getStartTime()); // integer
        values.put(ItemTable.Cols.TYPE, item.getAlarmTypeChoice()); // integer

        return values;
    }

    private TimetableCursorWrapper queryItems(
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

        return new TimetableCursorWrapper(cursor);
    }
}