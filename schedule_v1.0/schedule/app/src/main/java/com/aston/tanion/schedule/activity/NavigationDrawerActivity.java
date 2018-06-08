package com.aston.tanion.schedule.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.database.SharedPrefs;
import com.aston.tanion.schedule.database.WeekLab;
import com.aston.tanion.schedule.dialog.WeekPreviewDialog;
import com.aston.tanion.schedule.fragment.TaskCompletedDetailFragment;
import com.aston.tanion.schedule.fragment.TaskListFragment;
import com.aston.tanion.schedule.fragment.TaskOngoingDetailFragment;
import com.aston.tanion.schedule.fragment.TimetableDetailFragment;
import com.aston.tanion.schedule.fragment.TimetableListFragment;
import com.aston.tanion.schedule.model.Day;
import com.aston.tanion.schedule.model.State;
import com.aston.tanion.schedule.utility.ViewPagerAdapter;
import com.aston.tanion.schedule.model.WeekItem;
import com.aston.tanion.schedule.service.DateChangeService;
import com.aston.tanion.schedule.utility.ActivityFragmentInteractionListener;
import com.aston.tanion.schedule.utility.Constant;

import java.util.Calendar;
import java.util.UUID;

/**
 * Created by Aston Tanion on 13/02/2016.
 */
public class NavigationDrawerActivity extends AppCompatActivity implements
        ActivityFragmentInteractionListener, ViewPagerAdapter.Callbacks {

    public static final String TAG = "NavigationDrawer";
    private static final String ARG_LAST_PAGE_SELECTED = "ARG_LAST_PAGE_SELECTED";
    private static final String ARG_IDENTIFIER = "ARG_IDENTIFIER";
    private static final String ARG_PREVIEW = "ARG_PREVIEW";
    private static final String ARG_PREVIEW_WEEK_TITLE = "ARG_PREVIEW_WEEK_TITLE";
    private static final String ARG_WEEK = "ARG_WEEK";

    private static View mSnackBarView;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mDrawerToggle;
    private static ViewPager mViewPager;
    private Toolbar mToolbar;
    private TabLayout mTabLayout;
    private FloatingActionButton mFloatingActionButton;
    private WeekPreviewDialog mWeekPreviewDialog;

    private SharedPrefs mPrefs;
    private Resources mResources;
    private int mIdentifier = 0;
    private int mLastPageSelected = -1;
    private String mCurrentWeekId;
    private boolean mIsPreviewVisible = false;
    private String mPreviewWeekTitle = "";

    private ViewPagerAdapter mAdapter;
    private Fragment mMasterListFragment;
    private Intent mFragmentData = null;

    public static Intent newIntent(Context context) {
        return new Intent(context, NavigationDrawerActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        mPrefs = SharedPrefs.get(this);
        mResources = getResources();
        // Use for the snack bar only
        mSnackBarView = findViewById(R.id.snack_bar_view);

        // Check whether this is a new user
        boolean userFirstVisit = (Boolean) mPrefs.read(Constant.USER_FIRST_VISIT_PREF, true);

        // Set up a default week.
        if (userFirstVisit) {
            // Create a new week/
            WeekItem week = new WeekItem();
            week.setTitle(mResources.getString(R.string.week_default_name));
            week.setPosition(0);
            // Add this week to the database.
            WeekLab.get(this).addItem(week);
            // Set this week as the current week
            DateChangeService.updateWeek(this, week);

            mPrefs.write(Constant.USER_FIRST_VISIT_PREF, false);

            // Show the tutorial to first users
            Intent tutorialIntent = TutorialActivity.newIntent(this);
            startActivity(tutorialIntent);
        }

        // Retrieve the save instance state.
        if (savedInstanceState != null) {
            mIdentifier = savedInstanceState.getInt(ARG_IDENTIFIER, 0);
            mLastPageSelected = savedInstanceState.getInt(ARG_LAST_PAGE_SELECTED, 0);
            mIsPreviewVisible = savedInstanceState.getBoolean(ARG_PREVIEW, false);

            // If the week preview is visible, keep it visible even if the
            // device have been rotated.
            if (mIsPreviewVisible) {
                mCurrentWeekId = savedInstanceState.getString(ARG_WEEK);
                mPreviewWeekTitle = savedInstanceState.getString(ARG_PREVIEW_WEEK_TITLE);
            } else {
                mCurrentWeekId = (String) mPrefs.read(Constant.WEEK_CURRENT_ID_PREF, "");
            }

        } else {
            mCurrentWeekId = (String) mPrefs.read(Constant.WEEK_CURRENT_ID_PREF, "");
        }

        // Start the date change service which will loop through day
        // and set up the correct alarm for each day.
        DateChangeService.onDayChange(this);

        // Initialise the table layout
        mTabLayout = (TabLayout) findViewById(R.id.table_layout);
        if (mTabLayout != null) {
            mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
            mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        }

        // Initialise the tool bar
        mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        updateToolBar(mIdentifier);
        setSupportActionBar(mToolbar);

        // Initialise the view pager adapter
        mAdapter = new ViewPagerAdapter(this, getSupportFragmentManager(), this);
        mAdapter.setCurrentWeekId(mCurrentWeekId);
        mAdapter.setIdentifier(mIdentifier);

        // Initialise the view pager
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        if (mViewPager != null) {
            mViewPager.setAdapter(mAdapter);
        }
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(
                    int position, float positionOffset, int positionOffsetPixels) {
                // Do nothing.
            }

            @Override
            public void onPageSelected(int position) {
                mLastPageSelected = position;
                // Hide the floating action button if you are in task complete fragment.
                if (mIdentifier == Constant.IDENTIFIER_TASK) {
                    int visibility = (position == 0) ? View.VISIBLE : View.GONE;
                    mFloatingActionButton.setVisibility(visibility);
                }

                // Remove any open master detail fragment.
                clearMasterDetailView();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                // Do nothing.
            }
        });

        // Initialise the floating action button.
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                if (mIdentifier == Constant.IDENTIFIER_TIMETABLE) {
                    // Get the day of the view pager's visible page.
                    String day = Day.values()[mLastPageSelected].toString();

                    // Request the view pager's current page fragment.
                    TimetableListFragment listFragment =
                            (TimetableListFragment) mAdapter.getFragment(mLastPageSelected);

                    // Check if the this is a master detail layout or landscape for large devices.
                    if (listFragment != null &&
                            listFragment.mIsMasterDetailLayout && !isFinishing()) {

                        // Get the last item click from the master list fragment
                        // and reset its touch count to 0.
                        TimetableListFragment.ItemHolder holder = listFragment.mPreviousItem;
                        if (holder != null) holder.mTouchedCount = 0;

                        TimetableDetailFragment detailFragment = TimetableDetailFragment
                                .newInstance(null, day, mCurrentWeekId);
                        detailFragment.useAsMasterDetail();
                        detailFragment.setMasterListUsed(listFragment);

                        // Place this fragment in a master detail layout container.
                        listFragment.getChildFragmentManager()
                                .beginTransaction()
                                .replace(R.id.fragment_detail_container, detailFragment)
                                .commit();
                    }
                    // If it is not a master detail layout/landscape, show
                    // the detail fragment in an activity.
                    else {
                        Intent timetableDetailIntent = TimetableDetailActivity
                                .newIntent(NavigationDrawerActivity.this, null,
                                        day, mCurrentWeekId);
                        startActivityForResult(timetableDetailIntent,
                                Constant.REQUEST_DESTROY_DETAIL);
                    }

                    mMasterListFragment = listFragment;

                } else if (mIdentifier == Constant.IDENTIFIER_TASK) {
                    // Get the current state of the view pager's current page.
                    String state = State.values()[mLastPageSelected].toString();
                    if (state != null && state.equals(State.ONGOING.toString())) {
                        // Request the view pager's current page fragment.
                        TaskListFragment listFragment =
                                (TaskListFragment) mAdapter.getFragment(mLastPageSelected);

                        // Check if the this is a master detail layout
                        // or landscape for large devices.
                        if (listFragment != null &&
                                listFragment.mIsMasterDetailLayout && !isFinishing()) {

                            // Get the last item click from the master list fragment
                            // and reset its touch count to 0.
                            TaskListFragment.ItemHolder holder = listFragment.mPreviousViewHolder;
                            if (holder != null) holder.mTouchedCount = 0;

                            TaskOngoingDetailFragment detailFragment =
                                    TaskOngoingDetailFragment.newInstance(null, state);
                            detailFragment.useAsMasterDetail();
                            detailFragment.setMasterListUsed(listFragment);

                            // Place this fragment in a master detail layout container.
                            listFragment.getChildFragmentManager()
                                    .beginTransaction()
                                    .replace(R.id.fragment_detail_container, detailFragment)
                                    .commitAllowingStateLoss();

                        }
                        // If it is not a master detail layout/landscape, show
                        // the detail fragment in an activity.
                        else {
                            Intent taskDetailIntent = TaskDetailActivity
                                    .newIntent(NavigationDrawerActivity.this, null, state);
                            startActivityForResult(
                                    taskDetailIntent, Constant.REQUEST_DESTROY_DETAIL);
                        }

                        mMasterListFragment = listFragment;
                    }

                }
            }
        });

        // Add the view pager to the table layout
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // Remove any open master detail fragment.
                clearMasterDetailView();
                // Change the view pager's current page.
                mLastPageSelected = tab.getPosition();
                mViewPager.setCurrentItem(mLastPageSelected);
                // Remove any open master detail fragment.
                clearMasterDetailView();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // do nothing
            }
        });

        // Initialise the drawer layout
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(MenuItem item) {
                        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {
                                return false;
                            }
                        });

                        item.setChecked(true);
                        // Close the drawer layout.
                        mDrawerLayout.closeDrawer(mNavigationView);

                        switch (item.getItemId()) {
                            case R.id.timetable_drawer_menu:
                                // Set the identifier as timetable.
                                mIdentifier = Constant.IDENTIFIER_TIMETABLE;
                                // Update the view pager base on this identifier.
                                updateViewPager(Constant.IDENTIFIER_TIMETABLE);
                                return true;
                            case R.id.task_drawer_menu:
                                // Set the identifier as task.
                                mIdentifier = Constant.IDENTIFIER_TASK;
                                // Update the view pager base on this identifier.
                                updateViewPager(Constant.IDENTIFIER_TASK);
                                return true;
                            case R.id.settings_drawer_menu:
                                // Remove any open master detail fragment.
                                clearMasterDetailView();
                                // Open the setting activity.
                                Intent settingsActivityIntent =
                                        SettingsActivity.newIntent(NavigationDrawerActivity.this);
                                startActivityForResult(
                                        settingsActivityIntent, Constant.REQUEST_SETTINGS_ACTIVITY);
                                return true;
                            case R.id.about_drawer_menu:
                                // Remove any open master detail fragment.
                                clearMasterDetailView();
                                // Open the about activity.
                                Intent aboutActivityIntent =
                                        AboutActivity.newIntent(NavigationDrawerActivity.this);
                                startActivity(aboutActivityIntent);
                                return true;
                            default:
                                return false;
                        }
                    }
                }
        );

        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // Update the tool bar
        updateToolBar(mIdentifier);
        // Set the view pager at a correct page.
        if (mLastPageSelected != -1) {
            mViewPager.setCurrentItem(mLastPageSelected);
        } else {
            // Set the view pager's current page based on either
            // the current day (for timetable) or the state (for task).
            setViewPagerPosition();
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.timetable_tab_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                // Remove any master detail fragment
                clearMasterDetailView();
                // Open the settings activity.
                Intent i = SettingsActivity.newIntent(this);
                startActivity(i);
                return true;
            case R.id.week_preview:
                // The week pager is related to the timetable.
                // So the week preview must only be open when
                // the timetable is visible.
                mIdentifier = Constant.IDENTIFIER_TIMETABLE;
                // Make the timetable visible.
                updateViewPager(Constant.IDENTIFIER_TIMETABLE);
                // Initialise the weekPreview dialog
                mWeekPreviewDialog = WeekPreviewDialog.newInstance();
                // Show the dialog
                mWeekPreviewDialog.show(getSupportFragmentManager(), WeekPreviewDialog.TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constant.REQUEST_SETTINGS_ACTIVITY) {
            // Update the view pager.
           updateViewPager(mIdentifier);
        }

        if (requestCode == Constant.REQUEST_DESTROY_DETAIL) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) return;
                mFragmentData = data;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Set the view pager current page as the last page
        // selected by the user.
        mViewPager.setCurrentItem(mLastPageSelected);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove any open master detail fragment
        clearMasterDetailView();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_IDENTIFIER, mIdentifier);
        outState.putInt(ARG_LAST_PAGE_SELECTED, mLastPageSelected);
        outState.putString(ARG_WEEK, mCurrentWeekId);
        outState.putBoolean(ARG_PREVIEW, mIsPreviewVisible);
        outState.putString(ARG_PREVIEW_WEEK_TITLE, mPreviewWeekTitle);
    }

    @Override
    public void onBackPressed() {
        // Check if this is a week preview.
        // And if it is, return to the normal week.
        if (mIsPreviewVisible) {
            updateViewPager(mIdentifier);
            mPreviewWeekTitle = "";
            mIsPreviewVisible = false;
            return;
        }
        // If not, leave the app.
        super.onBackPressed();
    }

    @Override
    public void onViewPagerFragmentReady(Fragment fragment, int position) {
        if (mFragmentData == null || position != mLastPageSelected) return;

        int itemRequest = 0;
        UUID uuid = null;
        String state = "";

        if (mIdentifier == Constant.IDENTIFIER_TIMETABLE) {
            itemRequest = mFragmentData.getIntExtra(
                    TimetableDetailFragment.EXTRA_ITEM_REQUEST, 0);
            uuid = (UUID) mFragmentData.getSerializableExtra(
                    TimetableDetailFragment.EXTRA_UUID);
        } else if (mIdentifier == Constant.IDENTIFIER_TASK) {
            itemRequest = mFragmentData.getIntExtra(
                    TaskOngoingDetailFragment.EXTRA_ITEM_REQUEST, 0);
            uuid = (UUID) mFragmentData.getSerializableExtra(
                    TaskOngoingDetailFragment.EXTRA_UUID);
            state = mFragmentData.getStringExtra(TaskOngoingDetailFragment.EXTRA_STATE);
        }

        if (mIdentifier == Constant.IDENTIFIER_TIMETABLE) {
            // Get the day of view pager visible page (last page selected by the user).
            String day = Day.values()[mLastPageSelected].toString();

            TimetableListFragment listFragment = (TimetableListFragment) fragment;

            // Check if this is a master detail layout.
            if (listFragment != null && listFragment.mIsMasterDetailLayout && !isFinishing()) {

                // Get the last item click from the master list fragment
                // and reset its touch count to 0.
                TimetableListFragment.ItemHolder holder = listFragment.mPreviousItem;
                if (holder != null) holder.mTouchedCount = 0;

                TimetableDetailFragment detailFragment;

                if (itemRequest == Constant.REQUEST_ITEM_CREATE) {
                    // Create a new timetable master detail fragment.
                    detailFragment = TimetableDetailFragment.newInstance(null, day, mCurrentWeekId);
                } else {
                    // Open an existing timetable master detail fragment.
                    detailFragment = TimetableDetailFragment.newInstance(uuid, day, mCurrentWeekId);
                }

                detailFragment.useAsMasterDetail();
                detailFragment.setMasterListUsed(listFragment);

                // Place this fragment in a master detail layout container.
                listFragment.getChildFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_detail_container, detailFragment)
                        .commit();
            }
        } else if (mIdentifier == Constant.IDENTIFIER_TASK) {
            TaskListFragment listFragment = (TaskListFragment) fragment;

            // Check if this is a master detail layout.
            if (listFragment != null && listFragment.mIsMasterDetailLayout && !isFinishing()) {

                // Get the last item click from the master list fragment
                // and reset its touch count to 0.
                TaskListFragment.ItemHolder holder = listFragment.mPreviousViewHolder;
                if (holder != null) holder.mTouchedCount = 0;


                if (state.equals(State.ONGOING.toString())) {
                    // Create a new task master detail fragment.
                    TaskOngoingDetailFragment detailFragment =
                            TaskOngoingDetailFragment.newInstance(null, state);
                    detailFragment.useAsMasterDetail();
                    detailFragment.setMasterListUsed(listFragment);

                    // Place this fragment in a master detail layout container.
                    listFragment.getChildFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_detail_container, detailFragment)
                            .commitAllowingStateLoss();
                } else {
                    // Open an existing task master detail fragment.
                    TaskCompletedDetailFragment detailFragment =
                            TaskCompletedDetailFragment.newInstance(uuid);
                    detailFragment.useAsMasterDetail();
                    detailFragment.setMasterListUsed(listFragment);

                    // Place this fragment in a master detail layout container.
                    listFragment.getChildFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_detail_container, detailFragment)
                            .commitAllowingStateLoss();
                }


            }
        }
    }

    @Override
    public <L extends Fragment, D extends Fragment> void onFragmentDetailReady(
            L masterList, D masterDetail) {

        if (mFragmentData == null || masterList == null
                || masterDetail == null || isFinishing()) return;

        if (masterDetail instanceof TimetableDetailFragment) {
            // Update the timetable master detail fragment.
            ((TimetableDetailFragment) masterDetail).updateDetail(mFragmentData);
        } else if (masterDetail instanceof TaskOngoingDetailFragment) {
            // Update the task master detail fragment.
            ((TaskOngoingDetailFragment) masterDetail).updateDetail(mFragmentData);
        }

        mFragmentData = null;
        mMasterListFragment = masterList;

    }

    @Override
    public <L extends Fragment, D extends Fragment> void onFragmentListItemClick(
            L masterList, D masterDetail) {
        mMasterListFragment = masterList;
    }

    @Override
    public <L extends Fragment, D extends Fragment> void onFragmentDetailMenuClick(
            L masterList, D masterDetail) {
        // Remove any open master detail fragment.
        clearMasterDetailView();

        // Remove the timetable master detail fragment when the menu have been clicked.
        if (masterList instanceof TimetableListFragment) {
            TimetableListFragment listFragment = (TimetableListFragment) masterList;
            if (masterDetail != null && !isFinishing()) {
                masterDetail.setHasOptionsMenu(false);

                // Remove this fragment in a master detail layout container.
                listFragment.getChildFragmentManager()
                        .beginTransaction()
                        .remove(masterDetail)
                        .commit();
            }

            listFragment.onResume();

        }
        // Remove the task master detail fragment when the menu have been clicked.
        else if (masterList instanceof TaskListFragment) {
            TaskListFragment listFragment = (TaskListFragment) masterList;
            if (masterDetail != null && !isFinishing()) {
                masterDetail.setHasOptionsMenu(false);

                // Remove this fragment in a master detail layout container.
                listFragment.getChildFragmentManager()
                        .beginTransaction()
                        .remove(masterDetail)
                        .commit();
            } else {
                FragmentManager fm = listFragment.getChildFragmentManager();
                Fragment fragment = fm.findFragmentById(R.id.fragment_detail_container);

                if (fragment != null && !isFinishing()) {
                    fm.beginTransaction()
                            .remove(fragment)
                            .commit();
                }

                mAdapter.setCurrentWeekId(mCurrentWeekId);
                mAdapter.setIdentifier(mIdentifier);
                mAdapter.notifyDataSetChanged();
            }

            listFragment.onResume();
        } else {
            if (masterList == null) return;
            FragmentManager fm = masterList.getChildFragmentManager();
            Fragment fragment = fm.findFragmentById(R.id.fragment_detail_container);

            if (fragment != null && !isFinishing()) {
                fragment.setHasOptionsMenu(false);

                // Remove this fragment in a master detail layout container.
                fm.beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }
    }

    @Override
    public void onDialogFragmentResult(int requestCode, Intent data) {
        // Listen to the week preview dialog.
        if (requestCode == Constant.REQUEST_WEEK_PREVIEW_DIALOG) {
            if (data == null) {
                return;
            }

            mIsPreviewVisible = true;
            mPreviewWeekTitle = data.getStringExtra(WeekPreviewDialog.EXTRA_TITLE);
            mCurrentWeekId = data.getStringExtra(WeekPreviewDialog.EXTRA_WEEK_ID);

            // Update the tool bar.
            mToolbar.setTitle(mResources.getString(R.string.timetable));
            mToolbar.setSubtitle(String.format(
                    mResources.getString(R.string.timetable_preview), mPreviewWeekTitle));
        }

        mAdapter.setCurrentWeekId(mCurrentWeekId);
        mAdapter.setIdentifier(mIdentifier);
        mAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();

        // Dismiss the dialog
        mWeekPreviewDialog.dismiss();
    }

    /**
     * Chose to show either a timetable of a task on the view pager
     * @param identifier a constant representing either a timetable or a task
     * */
    private void updateViewPager(int identifier) {
        // Show the floating action button
        mFloatingActionButton.setVisibility(View.VISIBLE);
        // Remove any week preview information
        mPreviewWeekTitle = "";
        mIsPreviewVisible = false;
        // Get the current week id
        mCurrentWeekId = (String) mPrefs.read(Constant.WEEK_CURRENT_ID_PREF, "");
        // Remove any open master detail fragment
        clearMasterDetailView();
        // Update the tool bar
        updateToolBar(identifier);
        // Update the view pager adapter
        mAdapter.setCurrentWeekId(mCurrentWeekId);
        mAdapter.setIdentifier(mIdentifier);
        mAdapter.notifyDataSetChanged();
        // Set the view pager's current page base on either the day (timetable)
        // or the state (task).
        setViewPagerPosition();
    }

    /**
     * Remove all left open master detail view
     * */
    private void clearMasterDetailView() {
        if (mMasterListFragment == null) return;

        // Get any master detail fragment attach to this master list.
        Fragment fragment = mMasterListFragment.getChildFragmentManager()
                .findFragmentById(R.id.fragment_detail_container);

        if (mMasterListFragment instanceof TimetableListFragment) {
            // Get the last item click from the master list fragment
            // and reset its touch count to 0.
            TimetableListFragment.ItemHolder itemHolder =
                    ((TimetableListFragment) mMasterListFragment).mPreviousItem;
            if (itemHolder != null) itemHolder.mTouchedCount = 0;
        } else if (mMasterListFragment instanceof TaskListFragment) {
            // Get the last item click from the master list fragment
            // and reset its touch count to 0.
            TaskListFragment.ItemHolder itemHolder =
                    ((TaskListFragment) mMasterListFragment).mPreviousViewHolder;
            if (itemHolder != null) itemHolder.mTouchedCount = 0;
        }

        if (fragment != null && !isFinishing()) {
            fragment.setHasOptionsMenu(false);

            // Remove this fragment in a master detail layout container.
            mMasterListFragment.getChildFragmentManager()
                    .beginTransaction()
                    .remove(fragment)
                    .commitAllowingStateLoss();
        }

        mMasterListFragment = null;
        invalidateOptionsMenu();
    }

    private void setViewPagerPosition() {
        if (mIdentifier == Constant.IDENTIFIER_TIMETABLE) {
            for (int i = 0; i < mTabLayout.getTabCount(); i ++) {
                String tabName = mTabLayout.getTabAt(i).getText().toString();
                if (tabName.equals(today())) {
                    mViewPager.setCurrentItem(i);
                    mLastPageSelected = i;
                    break;
                }
            }
        } else {
            mViewPager.setCurrentItem(0);
            mLastPageSelected = 0;
        }
    }

    private void updateToolBar(int identifier) {
        if (identifier == Constant.IDENTIFIER_TIMETABLE) {
            mIdentifier = Constant.IDENTIFIER_TIMETABLE;
            // Update the tab layout
            if (mTabLayout != null) {
                mTabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
                mTabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            }

            // Update the tool bar
            // Check if this is a week preview
            if (mIsPreviewVisible) {
                mToolbar.setTitle(mResources.getString(R.string.timetable));
                mToolbar.setSubtitle(String.format(
                        mResources.getString(R.string.timetable_preview), mPreviewWeekTitle));
            } else {
                mToolbar.setTitle(mResources.getString(R.string.timetable));
                mToolbar.setSubtitle((String) mPrefs.read(Constant.WEEK_CURRENT_NAME_PREF, ""));
            }

        } else if (identifier == Constant.IDENTIFIER_TASK) {
            mIdentifier = Constant.IDENTIFIER_TASK;
            // Update the table layout
            if (mTabLayout != null) {
                mTabLayout.setTabMode(TabLayout.MODE_FIXED);
                mTabLayout.setTabGravity(TabLayout.GRAVITY_CENTER);
            }

            // Update the tool bar
            mToolbar.setTitle(mResources.getString(R.string.task));
            mToolbar.setSubtitle("");
        }

        invalidateOptionsMenu();
    }

    /**
     * Return the current day name base on the calendar day
     * */
    private String today() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        String[] days = mResources.getStringArray(R.array.timetable_tab);

        switch (day) {
            case 1:
                return days[6];
            case 2:
                return days[0];
            case 3:
                return days[1];
            case 4:
                return days[2];
            case 5:
                return days[3];
            case 6:
                return days[4];
            case 7:
                return days[5];
            default:
                return days[day % 7];
        }
    }

    public static View getSnackBarView() {
        return mSnackBarView;
    }
}