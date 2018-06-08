package com.aston.tanion.schedule.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aston.tanion.schedule.model.TimetableId;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Aston Tanion on 27/07/2016.
 */
public class TimetableMap {
    public static final String TAG = "TimetableMap";
    private static TimetableMap sTimetableMap;


    private static class ItemTable {
        public static final String NAME = "TIMETABLE_MAP";

        public static class Cols {
            public static final String UUID = "UUID";
            public static final String START = "START";
            public static final String END = "END";
            public static final String START_BEFORE = "START_BEFORE";
            public static final String END_BEFORE = "END_BEFORE";
        }
    }

    private static class DbHelper extends SQLiteOpenHelper {
        private static final int VERSION = 1;
        private static final String DATABASE_NAME = "timetable_map.db";

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + ItemTable.NAME + "("
                    + "_id integer primary key autoincrement, "
                    + ItemTable.Cols.UUID + ", "
                    + ItemTable.Cols.START + ", "
                    + ItemTable.Cols.END + ", "
                    + ItemTable.Cols.START_BEFORE + ", "
                    + ItemTable.Cols.END_BEFORE + ")");
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

        public TimetableId getItem() {

            int start = getInt(getColumnIndexOrThrow(ItemTable.Cols.START));
            int end = getInt(getColumnIndexOrThrow(ItemTable.Cols.END));
            int startBefore = getInt(getColumnIndexOrThrow(ItemTable.Cols.START_BEFORE));
            int endBefore = getInt(getColumnIndexOrThrow(ItemTable.Cols.END_BEFORE));

            int[] ids = new int[] {start, end, startBefore, endBefore};

            String uuid = getString(getColumnIndexOrThrow(ItemTable.Cols.UUID));

            return new TimetableId(UUID.fromString(uuid), ids);
        }
    }


    private SQLiteDatabase mDatabase;

    public static TimetableMap get(Context context) {
        if (sTimetableMap == null) {
            sTimetableMap = new TimetableMap(context);
        }
        return sTimetableMap;
    }

    private TimetableMap(Context context) {
        mDatabase = new DbHelper(context).getWritableDatabase();
    }

    public ConcurrentHashMap<UUID, int[]> getMap() {
        ConcurrentHashMap<UUID, int[]> map = new ConcurrentHashMap<>();

        DbWrapper cursor = queryItems(ItemTable.NAME, null, null, null, null, null, null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                TimetableId item = cursor.getItem();
                map.put(item.getUUID(), item.getIds());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return map;
    }

    public void put(UUID uuid, int[] ids) {
        ContentValues values = getValues(new TimetableId(uuid, ids));
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

    private ContentValues getValues(TimetableId item) {
        ContentValues values = new ContentValues();
        values.put(ItemTable.Cols.UUID, item.getUUID().toString());
        values.put(ItemTable.Cols.START, item.getIds()[0]);
        values.put(ItemTable.Cols.END, item.getIds()[1]);
        values.put(ItemTable.Cols.START_BEFORE, item.getIds()[2]);
        values.put(ItemTable.Cols.END_BEFORE, item.getIds()[3]);
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