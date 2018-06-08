package com.aston.tanion.schedule.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.fragment.TaskCompletedDetailFragment;
import com.aston.tanion.schedule.utility.ActivityFragmentInteractionListener;

import java.util.UUID;

/**
 * Created by Aston Tanion on 13/04/2016.
 */
public class TaskCompletedDetailActivity extends SingleFragmentActivity
        implements ActivityFragmentInteractionListener{

    public static final String TAG ="TaskCompletedDetail";
    private static final String EXTRA_ID = "com.tanion.aston.rovery.com.aston.tanion.schedule.activity.ID";

    public static Intent newIntent(Context context, UUID id) {
        Intent i = new Intent(context, TaskCompletedDetailActivity.class);
        i.putExtra(EXTRA_ID, id);
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
        UUID id = (UUID) getIntent().getSerializableExtra(EXTRA_ID);
        return TaskCompletedDetailFragment.newInstance(id);
    }

    @Override
    public <L extends Fragment, D extends Fragment> void onFragmentDetailReady(L masterList, D masterDetail) {
        // Do nothing
    }

    @Override
    public <L extends Fragment, D extends Fragment> void onFragmentDetailMenuClick(L masterList, D masterDetail) {
        // Do nothing
    }

    @Override
    public <L extends Fragment, D extends Fragment> void onFragmentListItemClick(L masterList, D masterDetail) {
        // Do nothing
    }

    @Override
    public void onDialogFragmentResult(int requestCode, Intent data) {
        // Do nothing
    }
}