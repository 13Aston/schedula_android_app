package com.aston.tanion.schedule.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.aston.tanion.schedule.fragment.SettingsFragment;
import com.aston.tanion.schedule.utility.Constant;

/**
 * Created by Aston Tanion on 12/03/2016.
 */
public class SettingsActivity extends AppCompatActivity {
    public static final String TAG = "SettingsActivity";

    public static Intent newIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager()
                .beginTransaction().
                replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        onActivityResult(Constant.REQUEST_SETTINGS_ACTIVITY, Activity.RESULT_OK, null);
        super.onDestroy();
    }
}