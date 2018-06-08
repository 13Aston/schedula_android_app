package com.aston.tanion.schedule.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.fragment.TaskOngoingDetailFragment;
import com.aston.tanion.schedule.utility.ActivityFragmentInteractionListener;

import java.util.UUID;

/**
 * Created by Aston Tanion on 17/02/2016.
 */
public class TaskDetailActivity extends SingleFragmentActivity implements
        ActivityFragmentInteractionListener {
    public static final String TAG = "TaskDetailActivity";
    private static final String EXTRA_ID = "com.tanion.aston.rovery.com.aston.tanion.schedule.activity.ID";
    private static final String EXTRA_STATE = "com.tanion.aston.rovery.com.aston.tanion.schedule.activity.TASK_STATE";

    public static Intent newIntent(Context context, UUID id, String state) {
        Intent intent = new Intent(context, TaskDetailActivity.class);
        intent.putExtra(EXTRA_ID, id);
        intent.putExtra(EXTRA_STATE, state);
        return intent;
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
        UUID id = (UUID) getIntent().getSerializableExtra(EXTRA_ID);
        String state = getIntent().getStringExtra(EXTRA_STATE);
        return TaskOngoingDetailFragment.newInstance(id, state);
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