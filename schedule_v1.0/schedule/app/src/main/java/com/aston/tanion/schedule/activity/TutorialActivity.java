package com.aston.tanion.schedule.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.fragment.TutorialFragment;

/**
 * Created by Aston Tanion on 05/08/2016.
 */
public class TutorialActivity extends SingleFragmentActivity {

    public static Intent newIntent(Context context) {
        return new Intent(context, TutorialActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected Fragment createFragment() {
        return new TutorialFragment();
    }
}
