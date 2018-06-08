package com.aston.tanion.schedule.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Aston Tanion on 27/07/2016.
 */
public class RandomId {
    public static final String TAG = "RandomId";
    private static RandomId sID;


    private static class ItemTable {
        public static final String NAME = "RANDOM_ID";

        public static class Cols {
            public static final String ID = "ID";
        }
    }

    private static class DbHelper extends SQLiteOpenHelper {
        private static final int VERSION = 1;
        private static final String DATABASE_NAME = "random_id.db";

        public DbHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + ItemTable.NAME + "("
                    + "_id integer primary key autoincrement, "
                    + ItemTable.Cols.ID + ")");
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

        public int getItem() {
            return getInt(getColumnIndexOrThrow(ItemTable.Cols.ID));
        }
    }


    private SQLiteDatabase mDatabase;

    public static RandomId get(Context context) {
        if (sID == null) {
            sID = new RandomId(context);
        }
        return sID;
    }

    private RandomId(Context context) {
        mDatabase = new DbHelper(context).getWritableDatabase();
    }

    public List<Integer> getID() {
        List<Integer> id = new ArrayList<>();

        DbWrapper cursor = queryItems(ItemTable.NAME, null, null, null, null, null, null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                id.add(cursor.getItem());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }

        return id;
    }

    public void addId(int id) {
        ContentValues values = new ContentValues();
        values.put(ItemTable.Cols.ID, id);
        mDatabase.insert(ItemTable.NAME, null, values);
    }

    public void remove(int id) {
        mDatabase.delete(ItemTable.NAME,
                ItemTable.Cols.ID + " = ?",
                new String[]{Integer.toString(id)});
    }

    // This is for debugging purposes
    public int getSize() {
        return mDatabase.query(ItemTable.NAME, null, null, null, null, null, null).getCount();
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