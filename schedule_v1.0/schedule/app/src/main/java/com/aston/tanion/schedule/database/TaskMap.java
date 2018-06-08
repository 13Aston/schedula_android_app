package com.aston.tanion.schedule.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aston.tanion.schedule.model.TaskId;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Aston Tanion on 27/07/2016.
 */
public class TaskMap {
    public static final String TAG = "TaskMap";
    private static TaskMap sTaskMap;


    private static class ItemTable {
        public static final String NAME = "TASK_TABLE";

        public static class Cols {
            public static final String UUID = "UUID";
            public static final String START = "START";
            public static final String BEFORE = "DELTA_TIME";
            public static final String AMBER_PRIORITY = "AMBER_PRIORITY";
            public static final String RED_PRIORITY = "RED_PRIORITY";
            public static final String WAKE_UP = "WAKE_UP";
        }
    }

    private static class DbHelper extends SQLiteOpenHelper {
        private static final int VERSION = 1;
        private static final String DATABASE_NAME = "task_map.db";

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + ItemTable.NAME + "("
                    + "_id integer primary key autoincrement, "
                    + ItemTable.Cols.UUID + ", "
                    + ItemTable.Cols.START + ", "
                    + ItemTable.Cols.BEFORE + ", "
                    + ItemTable.Cols.AMBER_PRIORITY + ", "
                    + ItemTable.Cols.RED_PRIORITY + ", "
                    + ItemTable.Cols.WAKE_UP + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Do nothing
        }
    }

    private static class DbWrapper extends CursorWrapper {

        public DbWrapper(Cursor cursor) {
            super(cursor);
        }

        public TaskId getItem() {

            int start = getInt(getColumnIndexOrThrow(ItemTable.Cols.START));
            int before = getInt(getColumnIndexOrThrow(ItemTable.Cols.BEFORE));
            int amberPriority = getInt(getColumnIndexOrThrow(ItemTable.Cols.AMBER_PRIORITY));
            int redPriority = getInt(getColumnIndexOrThrow(ItemTable.Cols.RED_PRIORITY));
            int wakeUp = getInt(getColumnIndexOrThrow(ItemTable.Cols.WAKE_UP));

            int[] ids = new int[] {start, before, amberPriority, redPriority, wakeUp};

            String uuid = getString(getColumnIndexOrThrow(ItemTable.Cols.UUID));

            return new TaskId(UUID.fromString(uuid), ids);
        }
    }


    private SQLiteDatabase mDatabase;

    public static TaskMap get(Context context) {
        if (sTaskMap == null) {
            sTaskMap = new TaskMap(context);
        }
        return sTaskMap;
    }

    private TaskMap(Context context) {
        mDatabase = new DbHelper(context).getWritableDatabase();
    }

    public ConcurrentHashMap<UUID, int[]> getMap() {
        ConcurrentHashMap<UUID, int[]> map = new ConcurrentHashMap<>();

        DbWrapper cursor = queryItems(ItemTable.NAME, null, null, null, null, null, null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                TaskId item = cursor.getItem();
                map.put(item.getUUID(), item.getIds());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return map;
    }

    public void put(UUID uuid, int[] ids) {
        ContentValues values = getValues(new TaskId(uuid, ids));
        mDatabase.insert(ItemTable.NAME, null, values);
    }

    public void remove(UUID uuid) {
        mDatabase.delete(ItemTable.NAME,
                ItemTable.Cols.UUID + " = ?",
                new String[]{uuid.toString()});
    }

    // This is for debugging purposes
    public int getSize() {
        return mDatabase.query(ItemTable.NAME, null, null, null, null, null, null).getCount();
    }

    private ContentValues getValues(TaskId item) {
        ContentValues values = new ContentValues();
        values.put(ItemTable.Cols.UUID, item.getUUID().toString());
        values.put(ItemTable.Cols.START, item.getIds()[0]);
        values.put(ItemTable.Cols.BEFORE, item.getIds()[1]);
        values.put(ItemTable.Cols.AMBER_PRIORITY, item.getIds()[2]);
        values.put(ItemTable.Cols.RED_PRIORITY, item.getIds()[3]);
        values.put(ItemTable.Cols.WAKE_UP, item.getIds()[4]);
        return values;
    }

    private DbWrapper queryItems(
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

        return new DbWrapper(cursor);
    }
}