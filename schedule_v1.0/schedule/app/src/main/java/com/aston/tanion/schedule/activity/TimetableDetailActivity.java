package com.aston.tanion.schedule.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.fragment.TimetableDetailFragment;
import com.aston.tanion.schedule.utility.ActivityFragmentInteractionListener;

import java.util.UUID;

/**
 * Created by Aston Tanion on 06/02/2016.
 */
public class TimetableDetailActivity extends SingleFragmentActivity
        implements ActivityFragmentInteractionListener {

    public static final String TAG = "TimetableDetailActivity";
    private static final String EXTRA_ID =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.activity.ID";
    private static final String EXTRA_DAY =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.activity.DAY";
    private static final String EXTRA_WEEK =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.activity.WEEK";


    public static Intent newIntent(Context context, UUID id, String day, String week) {
        Intent i = new Intent(context, TimetableDetailActivity.class);
        i.putExtra(EXTRA_ID, id);
        i.putExtra(EXTRA_DAY, day);
        i.putExtra(EXTRA_WEEK, week);
        return i;
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
        UUID uuid = (UUID) getIntent().getSerializableExtra(EXTRA_ID);
        String day = getIntent().getStringExtra(EXTRA_DAY);
        String week = getIntent().getStringExtra(EXTRA_WEEK);
        return TimetableDetailFragment.newInstance(uuid, day, week);
    }

    @Override
    public <L extends Fragment, D extends Fragment> void onFragmentDetailReady(
            L masterList, D masterDetail) {
        // Do nothing.
    }

    @Override
    public <L extends Fragment, D extends Fragment> void onFragmentListItemClick(
            L masterList, D masterDetail) {
        // Do nothing.
    }

    @Override
    public <L extends Fragment, D extends Fragment> void onFragmentDetailMenuClick(
            L masterList, D masterDetail) {
        this.finish();
    }

    @Override
    public void onDialogFragmentResult(int requestCode, Intent data) {
        // Do nothing.
    }
}