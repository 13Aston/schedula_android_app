package com.aston.tanion.schedule.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.fragment.WeekDetailFragment;
import com.aston.tanion.schedule.fragment.WeekListFragment;
import com.aston.tanion.schedule.model.WeekItem;

/**
 * Created by Aston Tanion on 07/06/2016.
 */
public class WeekActivity extends SingleFragmentActivity implements
        WeekListFragment.Callbacks, WeekDetailFragment.Callbacks {
    public static final String TAG = "WeekActivity";

    private FragmentManager mFM;
    private View mDetailContainerView;
    private FloatingActionButton mFAB;
    private static View mSnackBarView;

    public static Intent newIntent(Context context) {
        return new Intent(context, WeekActivity.class);
    }

    @Override
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.week_master_detail;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFM = getSupportFragmentManager();

        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDetailContainerView = findViewById(R.id.fragment_detail_container);
        mSnackBarView = findViewById(R.id.snack_bar_view);

        if (mDetailContainerView != null) {
            mFAB = (FloatingActionButton) findViewById(R.id.floating_action_button);
            mFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog dialog = WeekListFragment.showAddDialog(WeekActivity.this);
                    dialog.show();
                }
            });
        }
    }

    @Override
    protected Fragment createFragment() {
        return WeekListFragment.newInstance();
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = mFM.findFragmentById(R.id.fragment_container);
        if (fragment != null && fragment instanceof WeekDetailFragment && !isFinishing()) {
            mFM.beginTransaction()
                    .replace(R.id.fragment_container,
                            WeekListFragment.newInstance(),
                            WeekListFragment.TAG)
                    .commit();

            fragment = mFM.findFragmentById(R.id.fragment_container);
        }

        if (fragment != null && fragment instanceof WeekListFragment) {
            super.onBackPressed();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Fragment fragment = mFM.findFragmentByTag(WeekDetailFragment.TAG);
        if (fragment != null && !isFinishing()) {
            mFM.beginTransaction().remove(fragment).commit();
        }
    }

    @Override
    public void onItemSelected(final WeekItem item) {
        if (mDetailContainerView == null && !isFinishing()) {
            WeekDetailFragment fragment = WeekDetailFragment.newInstance(item.getPosition());
            mFM.beginTransaction()
                    .replace(R.id.fragment_container,
                            fragment,
                            WeekDetailFragment.TAG)
                    .commit();

        } else {
            Fragment fragment = WeekDetailFragment.newInstance(item.getPosition());
            if (!isFinishing()) {
                mFM.beginTransaction()
                        .replace(R.id.fragment_detail_container,
                                fragment,
                                WeekDetailFragment.TAG)
                        .commit();
            }
        }
    }

    @Override
    public void onViewCreated() {
        if (mDetailContainerView == null) {
            WeekListFragment.setFABVisibility(View.VISIBLE);
        } else {
            WeekListFragment.setFABVisibility(View.GONE);
        }
    }

    @Override
    public void onUpdateList() {
        Fragment fragmentDetail = mFM.findFragmentById(R.id.fragment_detail_container);

        if (fragmentDetail != null && !isFinishing()) {
            mFM.beginTransaction()
                    .remove(fragmentDetail)
                    .commit();

        }

        Fragment fragment = mFM.findFragmentById(R.id.fragment_container);
        if (fragment != null && fragment instanceof WeekDetailFragment && !isFinishing()) {
            mFM.beginTransaction()
                    .replace(R.id.fragment_container,
                            WeekListFragment.newInstance(),
                            WeekListFragment.TAG)
                    .commit();

            fragment = mFM.findFragmentById(R.id.fragment_container);
        }

        if (fragment != null && fragment instanceof WeekListFragment && !isFinishing()) {
            fragment.onResume();
        }
    }

    public static View getSnackBarView() {
        return mSnackBarView;
    }
}