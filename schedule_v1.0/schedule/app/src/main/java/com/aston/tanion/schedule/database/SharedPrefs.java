package com.aston.tanion.schedule.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Aston Tanion on 14/03/2016.
 */
public class SharedPrefs {
    public static final String TAG = "SharedPrefs";
    private static SharedPrefs sSharedPrefs;

    private SharedPreferences mSharedPreferences;

    public static SharedPrefs get(Context context) {
        if (sSharedPrefs == null) {
            sSharedPrefs = new SharedPrefs(context.getApplicationContext());
        }
        return sSharedPrefs;
    }

    private SharedPrefs(Context context) {
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public <V> void write(String key, V value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();

        if (value instanceof Integer) {
            editor.putInt(key,(Integer) value);
        } else if (value instanceof Long) {
            editor.putLong(key, (Long) value);
        } else if (value instanceof Boolean) {
            editor.putBoolean(key, (Boolean) value);
        } else if (value instanceof String) {
            editor.putString(key, (String) value);
        } else {
            return;
        }
        editor.apply();
    }

    public <V> Object read(String key, V defaultValue) {
        Object object;

        if (defaultValue instanceof Integer) {
            object = mSharedPreferences.getInt(key,(Integer) defaultValue);
        } else if (defaultValue instanceof Long) {
            object = mSharedPreferences.getLong(key, (Long) defaultValue);
        } else if (defaultValue instanceof Boolean) {
            object = mSharedPreferences.getBoolean(key, (Boolean) defaultValue);
        } else if (defaultValue instanceof String) {
            object = mSharedPreferences.getString(key, (String) defaultValue);
        } else {
            return null;
        }
        return object;
    }
}