package com.aston.tanion.schedule.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.aston.tanion.schedule.model.WeekItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


/**
 * Created by Aston Tanion on 13/03/2016.
 */
public class WeekLab {
    private static final String TAG = "WeekLab";
    private static WeekLab sWeekLab;
    private SQLiteDatabase mDatabase;


    private static class ItemTable {
        public static final String NAME = "WEEK_TABLE";

        public static class Cols {
            // Week unique identification.
            public static final String UUID = "ID";
            // Week title/subject.
            public static final String SUBJECT = "SUBJECT";
            // Position of the week relative to other item.
            public static final String POSITION = "POSITION";
        }
    }

    private class WeekDbHelper extends SQLiteOpenHelper {
        private static final int VERSION = 1;
        private static final String DATABASE_NAME = "week.db";

        public WeekDbHelper(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("create table " + ItemTable.NAME + "(" +
                    " _id integer primary key autoincrement, " +
                    ItemTable.Cols.UUID + ", " +
                    ItemTable.Cols.SUBJECT + ", " +
                    ItemTable.Cols.POSITION +
                    ")"
            );
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Do nothing.
        }
    }

    private class WeekCursorWrapper extends CursorWrapper {

        public WeekCursorWrapper(Cursor cursor) {
            super(cursor);
        }

        public WeekItem getItem() {
            String uuidString = getString(getColumnIndex(ItemTable.Cols.UUID));
            String title = getString(getColumnIndex(ItemTable.Cols.SUBJECT));
            int position = getInt(getColumnIndex(ItemTable.Cols.POSITION));

            UUID uuid = UUID.fromString(uuidString);
            WeekItem item = new WeekItem(uuid);
            item.setTitle(title);
            item.setPosition(position);

            return item;
        }
    }


    public static WeekLab get(Context context) {
        if (sWeekLab == null) {
            sWeekLab = new WeekLab(context.getApplicationContext());
        }

        return sWeekLab;
    }

    private WeekLab(Context context) {
        mDatabase = new WeekDbHelper(context).getWritableDatabase();
    }

    public List<WeekItem> getItems() {
        List<WeekItem> items = new ArrayList<>();

        WeekCursorWrapper cursor = queryItems(
                ItemTable.NAME,     // table
                null,   // columns[]
                null,   // selection
                null,   // selectionArg[]
                null,   // groupBy
                null,   // having
                ItemTable.Cols.POSITION,   // orderBy
                null    // limit
        );

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

    public WeekItem getItem(int position) {
        WeekCursorWrapper cursor = queryItems(
                ItemTable.NAME,     // table
                null,   // columns[]
                null,   // selection
                null,   // selectionArg[]
                null,   // groupBy
                null,   // having
                ItemTable.Cols.POSITION,   // orderBy
                null    // limit
        );

        try {
            if (cursor.getCount() == 0) {
                return null;
            }
            cursor.moveToPosition(position);
            return cursor.getItem();
        } finally {
            cursor.close();
        }
    }

    public void addItem(WeekItem item) {
        ContentValues values = getContentValues(item);
        mDatabase.insert(
                ItemTable.NAME,
                null,
                values
        );
    }

    public void updateItem(WeekItem item) {
        ContentValues values = getContentValues(item);
        mDatabase.update(
                ItemTable.NAME,
                values,
                ItemTable.Cols.UUID + " = ?",
                new String[]{item.getUUID().toString()});
    }

    public void removeItem(String id) {

        WeekCursorWrapper cursor = queryItems(
                ItemTable.NAME,     // table
                null,   // columns[]
                null,   // selection
                null,   // selectionArg[]
                null,   // groupBy
                null,   // having
                null,   // orderBy
                null    // limit
        );

        if (cursor.getCount() < 2) {
            return;
        }

        mDatabase.delete(
                ItemTable.NAME,
                ItemTable.Cols.UUID + " = ?",
                new String[]{id}
        );
    }

    // This is for debugging purposes
    public int getSize() {
        return mDatabase.query(ItemTable.NAME, null, null, null, null, null, null).getCount();
    }

    private ContentValues getContentValues(WeekItem item) {
        ContentValues values = new ContentValues();
        values.put(ItemTable.Cols.UUID, item.getUUID().toString());
        values.put(ItemTable.Cols.SUBJECT, item.getTitle());
        values.put(ItemTable.Cols.POSITION, item.getPosition());
        return values;
    }

    private WeekCursorWrapper queryItems(
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

        return new WeekCursorWrapper(cursor);
    }
}