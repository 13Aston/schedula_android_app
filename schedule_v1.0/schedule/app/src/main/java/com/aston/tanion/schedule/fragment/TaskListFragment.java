package com.aston.tanion.schedule.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.aston.tanion.schedule.Handler.AlarmHandler;
import com.aston.tanion.schedule.Handler.DatabaseHandler;
import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.activity.NavigationDrawerActivity;
import com.aston.tanion.schedule.activity.TaskCompletedDetailActivity;
import com.aston.tanion.schedule.activity.TaskDetailActivity;
import com.aston.tanion.schedule.database.TasksLab;
import com.aston.tanion.schedule.model.State;
import com.aston.tanion.schedule.model.TaskItem;
import com.aston.tanion.schedule.utility.CommonMethod;
import com.aston.tanion.schedule.utility.Constant;
import com.aston.tanion.schedule.utility.ActivityFragmentInteractionListener;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Aston Tanion on 05/02/2016.
 */
public class TaskListFragment extends Fragment {
    public static final String TAG = "TaskTabOnGoingFragment";

    private static final String ARG_STATE = "state";

    private RecyclerView mRecyclerView;
    private TextView mEmptyRecyclerViewTextView;

    private Context mContext;
    private FragmentManager mFragmentManager;
    private DatabaseHandler<TaskItem> mDbHandler;
    private AlarmHandler<TaskItem> mAlarmHandler;
    private TasksLab mTasksLab;
    private Resources mResources;
    private ItemAdapter mAdapter;
    private ActivityFragmentInteractionListener mInteractionListener;

    public Fragment mFragment;
    private String mState = "";
    public boolean mIsMasterDetailLayout = false;
    public ItemHolder mPreviousViewHolder = null;

    public static TaskListFragment newInstance(String state) {
        Bundle args = new Bundle();
        args.putString(ARG_STATE, state);
        TaskListFragment fragment = new TaskListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @LayoutRes
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mContext = getContext();
        mFragmentManager = getChildFragmentManager();
        mTasksLab = TasksLab.get(getActivity());
        mState = getArguments().getString(ARG_STATE);
        mResources = getResources();

        Handler resultHandler = new Handler();
        mDbHandler = new DatabaseHandler<>(mContext, resultHandler, null, null, mState);
        mDbHandler.setOnItemRequest(new DatabaseHandler.RequestItems() {
            @Override
            public void onItemsRequest(List<?> items) {
                setUpAdapter((List<TaskItem>) items);
            }
        });
        mDbHandler.start();
        mDbHandler.getLooper();

        mAlarmHandler = new AlarmHandler<>(mContext, null);
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
            if (fragment instanceof TaskOngoingDetailFragment) mFragment = fragment;
            else if (fragment instanceof TaskCompletedDetailFragment) mFragment = fragment;
            else mFragment = null;
        }

        mEmptyRecyclerViewTextView = (TextView) view.findViewById(R.id.empty_recycler_view);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // This method request the list of item in the database and updates the UI.
        mDbHandler.queueRequest(null, Constant.IDENTIFIER_TASK, Constant.DATABASE_GET_ITEMS);
    }

    private void setUpAdapter(List<TaskItem> items) {

        if (items.size() > 0) {
            mEmptyRecyclerViewTextView.setVisibility(View.GONE);
        } else {
            mEmptyRecyclerViewTextView.setVisibility(View.VISIBLE);
        }

        if (mAdapter == null) {
            mAdapter = new ItemAdapter(items);
            mRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setItems(items);
            mAdapter.notifyDataSetChanged();
        }
    }

    public class ItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ViewGroup mTextGroup;
        private FrameLayout mPriorityView;
        private TextView mTitleTextView;
        private TextView mTimeTextView;
        private TextView mDateTextView;
        private TextView mAlarmStateTextView; // on - off.
        private ImageView mDeleteImageView;
        private ImageView mAlarmImageView;
        private CheckBox mCompletedCheckBox;

        private TaskItem mItem;
        private boolean mIsSelected;
        public int mTouchedCount = 0;

        public ItemHolder(View itemView) {
            super(itemView);

            mPriorityView = (FrameLayout) itemView.findViewById(R.id.task_item_priority);

            mTextGroup = (ViewGroup) itemView.findViewById(R.id.task_item_text_container);
            mTextGroup.setOnClickListener(this);
            mTitleTextView = (TextView) mTextGroup.findViewById(R.id.task_item_title);
            mTimeTextView = (TextView) mTextGroup.findViewById(R.id.task_item_time);
            mDateTextView = (TextView) mTextGroup.findViewById(R.id.task_item_date);

            mDeleteImageView = (ImageView) itemView.findViewById(R.id.task_item_delete);
            mDeleteImageView.setOnClickListener(this);

            mAlarmImageView = (ImageView) itemView.findViewById(R.id.task_item_alarm);
            mAlarmImageView.setOnClickListener(this);

            mAlarmStateTextView = (TextView) itemView.findViewById(R.id.task_item_alarm_stateText);
            mAlarmStateTextView.setOnClickListener(this);

            mCompletedCheckBox = (CheckBox) itemView.findViewById(R.id.task_item_completed);
            mCompletedCheckBox.setOnClickListener(this);
            mCompletedCheckBox.setChecked(false);
            mCompletedCheckBox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        // Remove the item from ON_GOING state and remove its alarm.
                        mTasksLab.removeItem(mItem, mState);
                        mAlarmHandler.queueMessage(mItem, Constant.ALARM_REMOVE,
                                Constant.IDENTIFIER_TASK, -1);
                        // Change the sate of this item from ON_GOING to COMPLETED state.
                        mItem.setState(State.values()[1].toString());
                        // Set the completed date to the current date.
                        mItem.setCompleteDate(new Date());
                        // Add the item to the database.
                        mTasksLab.addItem(mItem, State.values()[1].toString());
                        mCompletedCheckBox.setChecked(false);

                        // Update TaskTabCompletedFragment.
                        mInteractionListener.onFragmentDetailMenuClick(TaskListFragment.this, null);
                    }
                }
            });

            if (mState.equals(State.COMPLETED.toString())) {
                mCompletedCheckBox.setVisibility(View.GONE);
                mAlarmImageView.setVisibility(View.GONE);
                mAlarmStateTextView.setVisibility(View.GONE);
                mPriorityView.setVisibility(View.GONE);
            }
        }

        private void bindItems(TaskItem item) {
            mItem = item;
            SimpleDateFormat format = new SimpleDateFormat(
                    mResources.getString(R.string.date_format),
                    Locale.getDefault());

            mTitleTextView.setText(mItem.getTitle());
            mTimeTextView.setText(CommonMethod.timeStringFormat(mItem.getTime()));
            mDateTextView.setText(format.format(item.getDueDate()));
            mIsSelected = mItem.getAlarmStateChecked();
            shouldSetAlarm(mItem.getAlarmStateChecked());

            int hour = mItem.getTime() / 60;
            int minute = mItem.getTime() % 60;

            Calendar currentTime = Calendar.getInstance();

            Calendar dueTime = Calendar.getInstance();
            dueTime.setTime(mItem.getDueDate());
            dueTime.set(Calendar.HOUR_OF_DAY, hour);
            dueTime.set(Calendar.MINUTE, minute);
            dueTime.set(Calendar.SECOND, 0);
            dueTime.set(Calendar.MILLISECOND, 0);

            long interval = item.getIntervalTime();

            if (currentTime.getTimeInMillis() > dueTime.getTimeInMillis() - 2 * interval &&
                    currentTime.getTimeInMillis() < dueTime.getTimeInMillis() - interval) {
                mPriorityView.setBackgroundColor(mResources.getColor(R.color.amberColor));
            } else if (currentTime.getTimeInMillis() > dueTime.getTimeInMillis() - interval) {
                mPriorityView.setBackgroundColor(mResources.getColor(R.color.redColor));
            } else  {
                mPriorityView.setBackgroundColor(mResources.getColor(R.color.greenColor));
            }
        }

        private void shouldSetAlarm(boolean isOn) {
            if (isOn) {
                mAlarmImageView.setColorFilter(mResources.getColor(R.color.greenColor));
                mAlarmStateTextView.setTextColor(mResources.getColor(R.color.greenColor));
                mAlarmStateTextView.setText(mResources.getString(R.string.alarm_on));
                mItem.setAlarmStateChecked(true);
                mIsSelected = true;
            } else {
                mAlarmImageView.setColorFilter(null);
                mAlarmStateTextView.setTextColor(mTitleTextView.getTextColors());
                mAlarmStateTextView.setText(mResources.getString(R.string.alarm_off));
                mItem.setAlarmStateChecked(false);
                mIsSelected = false;
            }

            mTasksLab.updateItem(mItem, mState);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                // When clicking on the item.
                case R.id.task_item_text_container:

                    if (mPreviousViewHolder != null) {
                        if (mPreviousViewHolder.equals(this)) {
                            mTouchedCount += 1;
                        } else {
                            mPreviousViewHolder.mTouchedCount = 0;
                            mTouchedCount += 1;
                        }
                    } else {
                        mTouchedCount += 1;
                    }

                    if (mIsMasterDetailLayout) {
                        if (mState.equals(State.ONGOING.toString())) {
                            mFragment = TaskOngoingDetailFragment.newInstance(mItem.getUUID(), mState);
                            ((TaskOngoingDetailFragment) mFragment).useAsMasterDetail();
                            ((TaskOngoingDetailFragment) mFragment).setMasterListUsed(TaskListFragment.this);

                            if (mFragment != null && !getActivity().isFinishing()) {
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_detail_container, mFragment)
                                        .commit();
                            }
                        } else {
                            mFragment = TaskCompletedDetailFragment.newInstance(mItem.getUUID());

                            if (mFragment != null && !getActivity().isFinishing()) {
                                mFragmentManager.beginTransaction()
                                        .replace(R.id.fragment_detail_container, mFragment)
                                        .commit();
                            }
                        }

                        if (this.mTouchedCount == 2) {
                            this.mTouchedCount = 0;
                            if (mPreviousViewHolder != null) mPreviousViewHolder.mTouchedCount = 0;

                            if (mFragment != null && !getActivity().isFinishing()) {
                                mFragmentManager.beginTransaction()
                                        .remove(mFragment)
                                        .commit();
                            }
                        } else {
                            mInteractionListener.onFragmentListItemClick(TaskListFragment.this, mFragment);
                        }

                    } else {

                        if (mState.equals(State.ONGOING.toString())) {
                            Intent ongoingDetailActivityIntent = TaskDetailActivity
                                    .newIntent(mContext, mItem.getUUID(), mItem.getState());
                            getActivity().startActivityForResult(ongoingDetailActivityIntent,
                                    Constant.REQUEST_DESTROY_DETAIL);
                        } else {
                            Intent completedDetailActivityIntent = TaskCompletedDetailActivity
                                    .newIntent(mContext, mItem.getUUID());
                            getActivity().startActivityForResult(completedDetailActivityIntent,
                                    Constant.REQUEST_DESTROY_DETAIL);
                        }
                    }

                    mPreviousViewHolder = this;
                    break;
                // When clicking on the alarm icon.
                case R.id.task_item_alarm:
                    alarmIconBehaviour();
                    break;
                // When clicking on the alarm text.
                case R.id.task_item_alarm_stateText:
                    alarmIconBehaviour();
                    break;
                // When removing (deleting) the item.
                case R.id.task_item_delete:
                    // Remove this item from the database
                    mDbHandler.queueRequest(
                            mItem,
                            Constant.IDENTIFIER_TASK,
                            Constant.DATABASE_REMOVE_ITEM);


                    // Remove the alarm.
                    mAlarmHandler.queueMessage(mItem, Constant.ALARM_REMOVE,
                            Constant.IDENTIFIER_TASK, -1);

                    // Recover the alarm detail from a file. If the snack bar undo action have been
                    // triggered, use this detail to recreate the file.
                    String fileContent = CommonMethod.readFile(
                            mContext, mItem.getUUID().toString());

                    // Delete the file if it exists.
                    String path = mContext.getFilesDir().getAbsolutePath();
                    File file = new File(path + "/" + mItem.getUUID().toString());

                    if (file.exists()) {
                        file.delete();
                    }

                    // Update the UI
                    mDbHandler.queueRequest(
                            null,
                            Constant.IDENTIFIER_TASK,
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
                            new SnackBarActionListener(mItem, fileContent));

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
                mItem.setAlarmStateChecked(false);
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_REMOVE,
                        Constant.IDENTIFIER_TASK, -1);
            } else if (mAlarmStateTextView.getText().equals(mResources.getString(R.string.alarm_off))) {
                mItem.setAlarmStateChecked(true);
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_ADD,
                        Constant.IDENTIFIER_TASK, -1);
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
        private List<TaskItem> mItems;
        public ItemAdapter(List<TaskItem> items) {
            mItems = items;
        }

        @Override
        public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            View itemView =
                    inflater.inflate(R.layout.task_recycler_view_item, parent, false);
            return new ItemHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ItemHolder holder, int position) {
            TaskItem item = mItems.get(position);
            holder.bindItems(item);
        }

        @Override
        public int getItemCount() {
            return mItems.size();
        }

        private void setItems(List<TaskItem> items) {
            mItems = items;
        }
    }

    private class SnackBarActionListener implements View.OnClickListener {
        private TaskItem item;
        private String fileContent;
        public SnackBarActionListener(TaskItem item, String fileContent) {
            this.item = item;
            this.fileContent = fileContent;
        }

        @Override
        public void onClick(View v) {
            // Add this item to a database.
            mDbHandler.queueRequest(
                    item,
                    Constant.IDENTIFIER_TASK,
                    Constant.DATABASE_ADD_ITEM);

            // Reset this alarm.
            if (item.getAlarmStateChecked()) {
                // Add alarm
                mAlarmHandler.queueMessage(item, Constant.ALARM_ADD,
                        Constant.IDENTIFIER_TASK, -1);
            }

            // Recreate the file that have been deleted.
            CommonMethod.createFile(mContext, item.getUUID().toString(), fileContent);

            // This method request the list of item in the database and updates the UI.
            mDbHandler.queueRequest(
                    null,
                    Constant.IDENTIFIER_TASK,
                    Constant.DATABASE_GET_ITEMS);
        }
    }
}