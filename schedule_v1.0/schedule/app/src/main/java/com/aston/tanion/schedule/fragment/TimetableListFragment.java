package com.aston.tanion.schedule.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.aston.tanion.schedule.Handler.AlarmHandler;
import com.aston.tanion.schedule.Handler.DatabaseHandler;
import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.activity.NavigationDrawerActivity;
import com.aston.tanion.schedule.activity.TimetableDetailActivity;
import com.aston.tanion.schedule.model.TimetableItem;
import com.aston.tanion.schedule.utility.ActivityFragmentInteractionListener;
import com.aston.tanion.schedule.utility.Constant;

import java.util.List;

/**
 * Created by Aston Tanion on 05/02/2016.
 */
public class TimetableListFragment extends Fragment {
    private static final String TAG = "TimetableTabFragment";
    private static final String ARG_DAY = "day";
    private static final String ARG_WEEK = "week_id_string";

    private RecyclerView mRecyclerView;
    private TextView mTextView;
    private ItemAdapter mAdapter;
    private DatabaseHandler<TimetableItem> mDbHandler;
    private AlarmHandler<TimetableItem> mAlarmHandler;
    private ActivityFragmentInteractionListener mInteractionListener;

    private Context mContext;
    private FragmentManager mFragmentManager;
    private Resources mResources;
    private String mDay;
    private String mWeekIdString;
    public boolean mIsMasterDetailLayout = false;
    public TimetableDetailFragment mFragment = null;
    // Note that mPreviewItem is the last item clicked on the
    // master list fragment (recycler view).
    public ItemHolder mPreviousItem = null;

    public static TimetableListFragment newInstance(String day, String weekIdString) {
        Bundle args = new Bundle();
        args.putString(ARG_DAY, day);
        args.putString(ARG_WEEK, weekIdString);
        TimetableListFragment fragment = new TimetableListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    protected int getLayoutResId() {
        return R.layout.tab_list_master_detail;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mInteractionListener = (ActivityFragmentInteractionListener) context;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            try {
                mInteractionListener = (ActivityFragmentInteractionListener) activity;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDay = getArguments().getString(ARG_DAY, "");
        mWeekIdString = getArguments().getString(ARG_WEEK);
        mContext = getActivity();
        mResources = getResources();
        mFragmentManager = getChildFragmentManager();

        Handler resultHandler = new Handler();
        mDbHandler = new DatabaseHandler<>(mContext, resultHandler, mDay, mWeekIdString, "");
        mDbHandler.setOnItemRequest(new DatabaseHandler.RequestItems() {
            @Override
            public void onItemsRequest(List<?> items) {
                updateUI((List<TimetableItem>) items);
            }
        });
        mDbHandler.start();
        mDbHandler.getLooper();

        mAlarmHandler = new AlarmHandler<>(mContext, mDay);
        mAlarmHandler.start();
        mAlarmHandler.getLooper();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResId(), container, false);

        // Check if this is a master detail layout
        mIsMasterDetailLayout = (view.findViewById(R.id.fragment_detail_container) != null);
        if (mIsMasterDetailLayout) {

            Fragment fragment = mFragmentManager.findFragmentById(R.id.fragment_detail_container);

            if (fragment instanceof TimetableDetailFragment) {
                mFragment = (TimetableDetailFragment)
                        mFragmentManager.findFragmentById(R.id.fragment_detail_container);
            }
        }

        // Initialise the recycler mView
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mTextView = (TextView) view.findViewById(R.id.empty_recycler_view);

        return view;
    }

    private void updateUI(List<TimetableItem> items) {

        if (items.size() > 0) {
            mTextView.setVisibility(View.GONE);
        } else {
            mTextView.setVisibility(View.VISIBLE);
        }

        if (mAdapter == null) {
            mAdapter = new ItemAdapter(items);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setItems(items);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // This method request the list of item in the database and updates the UI.
        mDbHandler.queueRequest(
                null,
                Constant.IDENTIFIER_TIMETABLE,
                Constant.DATABASE_GET_ITEMS);
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ViewGroup mGroupText;
        private ViewGroup mGroupImage;

        private TextView mTitleTextView;
        private TextView mTimeTextView;
        private TextView mLocationTextView;
        private TextView mAlarmStateTextView;
        private ImageView mDeleteImageView;
        private ImageView mAlarmImageView;

        private TimetableItem mItem;
        private boolean mIsSelected;
        // mTouchedCount is the number of time the use have touch this view.
        // If the item is touched twice, it automatically dismisses
        // its detail view.
        public int mTouchedCount = 0;

        public ItemHolder(View itemView) {
            super(itemView);

            mGroupText = (ViewGroup) itemView.findViewById(R.id.timetable_text_view_group);
            mTitleTextView = (TextView) mGroupText.findViewById(R.id.timetable_item_title);
            mTimeTextView = (TextView) mGroupText.findViewById(R.id.timetable_item_time);
            mLocationTextView = (TextView) mGroupText.findViewById(R.id.timetable_item_location);
            mGroupText.setOnClickListener(this);

            mGroupImage = (ViewGroup) itemView.findViewById(R.id.timetable_image_view_group);
            mAlarmImageView = (ImageView) mGroupImage.findViewById(R.id.timetable_item_alarm);
            mAlarmImageView.setOnClickListener(this);

            mAlarmStateTextView =
                    (TextView) mGroupImage.findViewById(R.id.timetable_item_alarm_state);
            mAlarmStateTextView.setOnClickListener(this);
            mDeleteImageView = (ImageView) mGroupImage.findViewById(R.id.timetable_item_delete);
            mDeleteImageView.setOnClickListener(this);
        }

        private void bindItems(TimetableItem item) {
            mItem = item;
            mTitleTextView.setText(mItem.getTitle());
            mTimeTextView.setText(mItem.getTimeFormat());
            mLocationTextView.setText(mItem.getLocation());
            mIsSelected = mItem.getAlarmStartState() || mItem.getAlarmEndState();
            shouldSetAlarm(mIsSelected);
        }

        private void shouldSetAlarm(boolean isOn) {
            if (isOn) {
                mAlarmImageView.setColorFilter(mResources.getColor(R.color.greenColor));
                mAlarmStateTextView.setTextColor(mResources.getColor(R.color.greenColor));
                mAlarmStateTextView.setText(mResources.getString(R.string.alarm_on));
                mItem.setAlarmStartState(mItem.getAlarmStartState());
                mItem.setAlarmEndState(mItem.getAlarmEndState());
                mIsSelected = true;
            } else {
                mAlarmImageView.setColorFilter(null);
                mAlarmStateTextView.setTextColor(mTitleTextView.getTextColors());
                mAlarmStateTextView.setText(mResources.getString(R.string.alarm_off));
                mItem.setAlarmStartState(false);
                mItem.setAlarmEndState(false);
                mIsSelected = false;
            }

            // Update the item in the database.
            mDbHandler.queueRequest(mItem,
                    Constant.IDENTIFIER_TIMETABLE,
                    Constant.DATABASE_UPDATE_ITEM);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // When clicking on the item.
                case R.id.timetable_text_view_group:

                    if (mPreviousItem != null) {
                        if (mPreviousItem.equals(this)) {
                            mTouchedCount += 1;
                        } else {
                            mPreviousItem.mTouchedCount = 0;
                            mTouchedCount += 1;
                        }
                    } else {
                        mTouchedCount += 1;
                    }

                    if (mIsMasterDetailLayout) {
                        mFragment = TimetableDetailFragment
                                .newInstance(mItem.getUUID(), mDay, mWeekIdString);
                        mFragment.useAsMasterDetail();
                        mFragment.setMasterListUsed(TimetableListFragment.this);

                        if (mFragment != null && !getActivity().isFinishing()) {
                            mFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_detail_container, mFragment)
                                    .commit();
                        }

                        if (this.mTouchedCount == 2) {
                            this.mTouchedCount = 0;
                            if (mPreviousItem != null) mPreviousItem.mTouchedCount = 0;

                            if (mFragment != null && !getActivity().isFinishing()) {
                                mFragment.setHasOptionsMenu(false);
                                mFragmentManager.beginTransaction()
                                        .remove(mFragment)
                                        .commit();
                            }
                        } else {
                            mInteractionListener.onFragmentListItemClick(
                                    TimetableListFragment.this, mFragment);
                        }

                    } else {
                        Intent timetableDetailIntent = TimetableDetailActivity
                                .newIntent(mContext, mItem.getUUID(), mDay, mWeekIdString);
                        getActivity().startActivityForResult(timetableDetailIntent,
                                Constant.REQUEST_DESTROY_DETAIL);
                    }

                    mPreviousItem = this;
                    break;
                // When clicking on the alarm icon.
                case R.id.timetable_item_alarm:
                    alarmIconBehaviour();
                    break;
                // When clicking on the alarm text.
                case R.id.timetable_item_alarm_state:
                    alarmIconBehaviour();
                    break;
                // When removing (deleting) the item.
                case R.id.timetable_item_delete:
                    // Remove the item from the database
                    // mTimetableLab.remove(mItem, mDay);
                    mDbHandler.queueRequest(
                            mItem,
                            Constant.IDENTIFIER_TIMETABLE,
                            Constant.DATABASE_REMOVE_ITEM);

                    // Remove the alarm.
                    mAlarmHandler.queueMessage(mItem,
                            Constant.ALARM_REMOVE, Constant.IDENTIFIER_TIMETABLE, -1);

                    // Update the UI.
                    mDbHandler.queueRequest(
                            null,
                            Constant.IDENTIFIER_TIMETABLE,
                            Constant.DATABASE_GET_ITEMS);

                    // Show a snack bar.
                    Snackbar snackbar = Snackbar.make(
                            NavigationDrawerActivity.getSnackBarView(),
                            mResources.getString(R.string.snack_bar_delete_message),
                            Snackbar.LENGTH_LONG
                    );
                    snackbar.show();
                    snackbar.setAction(
                            mResources.getString(R.string.snack_bar_delete_action),
                            new SnackBarActionListener(mItem));

                    // Check if it is a master detail layout
                    if (mIsMasterDetailLayout && mFragment != null && !getActivity().isFinishing()) {
                        mFragmentManager.beginTransaction()
                                .remove(mFragment)
                                .commit();
                    }

                    break;
                default:
                    break;
            }
        }

        private void alarmIconBehaviour() {
            if (mAlarmStateTextView.getText().equals(mResources.getString(R.string.alarm_on))) {
                mItem.setAlarmStartState(false);
                mItem.setAlarmEndState(false);
                // Remove the alarm
                mAlarmHandler.queueMessage(mItem,
                        Constant.ALARM_REMOVE, Constant.IDENTIFIER_TIMETABLE, -1);
            } else if (mAlarmStateTextView.getText().equals(mResources.getString(R.string.alarm_off))) {
                mItem.setAlarmStartState(true);
                // Add the alarm
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_ADD,
                        Constant.IDENTIFIER_TIMETABLE, Constant.ALARM_START);
            }
            shouldSetAlarm(!mIsSelected);

            // Check if it is a master detail layout
            if (mIsMasterDetailLayout && mFragment != null && !getActivity().isFinishing()) {
                mFragmentManager.beginTransaction()
                        .remove(mFragment)
                        .commit();
            }
        }
    }

    private class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {
        private List<TimetableItem> mItems;

        public ItemAdapter(List<TimetableItem> items) {
            mItems = items;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View itemView = inflater.inflate(R.layout.timetable_recycler_view_item, parent, false);
            return new ItemHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            TimetableItem item = mItems.get(position);
            holder.bindItems(item);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        public void setItems(List<TimetableItem> items) {
            mItems = items;
        }
    }

    private class SnackBarActionListener implements View.OnClickListener {
        private TimetableItem item;
        public SnackBarActionListener(TimetableItem item) {
            this.item = item;
        }

        @Override
        public void onClick(View v) {
            // Add this item to a database.
            mDbHandler.queueRequest(
                    item,
                    Constant.IDENTIFIER_TIMETABLE,
                    Constant.DATABASE_ADD_ITEM);
            // Reset this alarm.
            if (item.getAlarmStartState()) {
                // Add start alarm
                mAlarmHandler.queueMessage(item, Constant.ALARM_ADD,
                        Constant.IDENTIFIER_TIMETABLE, Constant.ALARM_START);
            }

            if (item.getAlarmEndState()) {
                // Add end alarm
                mAlarmHandler.queueMessage(item, Constant.ALARM_ADD,
                        Constant.IDENTIFIER_TIMETABLE, Constant.ALARM_END);
            }

            // This method request the list of item in the database and updates the UI.
            mDbHandler.queueRequest(
                    null,
                    Constant.IDENTIFIER_TIMETABLE,
                    Constant.DATABASE_GET_ITEMS);
        }
    }
}