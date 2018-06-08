package com.aston.tanion.schedule.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.aston.tanion.schedule.Handler.AlarmHandler;
import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.database.SharedPrefs;
import com.aston.tanion.schedule.database.TasksLab;
import com.aston.tanion.schedule.dialog.DatePickerDialog;
import com.aston.tanion.schedule.dialog.TimePickerDialog;
import com.aston.tanion.schedule.model.TaskItem;
import com.aston.tanion.schedule.utility.ActivityFragmentInteractionListener;
import com.aston.tanion.schedule.utility.CommonMethod;
import com.aston.tanion.schedule.utility.Constant;
import com.aston.tanion.schedule.utility.MasterDetailFragment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by Aston Tanion on 17/02/2016.
 */
public class TaskOngoingDetailFragment extends MasterDetailFragment<TaskListFragment> implements
        AdapterView.OnItemSelectedListener {
    private static final String TAG = "TaskDetailFragment";
    private static final String ARG_ID = "id";
    private static final String ARG_STATE = "state";

    public static final String EXTRA_TITLE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.TITLE";
    public static final String EXTRA_LOCATION =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.LOCATION";
    public static final String EXTRA_DETAIL =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.DETAIL";
    public static final String EXTRA_STATE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.STATE";
    public static final String EXTRA_STATE_CHECKED =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.STATE_CHECK";
    public static final String EXTRA_PRIORITY =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.PRIORITY";
    public static final String EXTRA_WAKE_UP_CALL =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.WAKE_UP_CALL";
    public static final String EXTRA_TIME =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.TIME";
    public static final String EXTRA_WAKE_UP_CALL_TIME =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.WAKE_UP_CALL_TIME";
    public static final String EXTRA_DELTA_TIME =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.DELTA_TIME";
    public static final String EXTRA_ALARM_TYPE_CHOICE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.ALARM_TYPE_CHOICE";
    public static final String EXTRA_DUE_DATE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.SET_DATE";
    public static final String EXTRA_COMPLETED_DATE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.COMPLETED_DATE";
    public static final String EXTRA_UUID =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.UUID";
    public static final String EXTRA_ITEM_REQUEST =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.ITEM_REQUEST";

    // title and location
    private TextInputLayout mTitleContainer;
    private TextInputEditText mTitleEditText;
    private TextInputEditText mLocationEditText;
    // date and time
    private TextView mTimeTextView;
    private TextView mDateTextView;
    private TextView mDateTimeErrorTextView;
    // detail and alarm set switch
    private EditText mDetailEditText;
    private Switch mAlarmStateSwitch;
    // alarm before view group
    private ViewGroup mAlarmBeforeViewGroup;
    private TextView mAlarmBeforeTextView;
    private SeekBar mHourSeekBar;
    private SeekBar mMinuteSeekBar;
    // alarm priority
    private CheckBox mPriorityCheckBox;
    // alarm wake up call
    private Switch mWakeUpSwitch;
    private TextView mWakeUpErrorTextView;
    private TextView mWakeUpTimeTextView;
    private ViewGroup mWakeUpTimeViewGroup;
    // alarm type spinner
    private Spinner mAlarmTypeSpinner;

    // Classes
    private TaskItem mItem;
    private TasksLab mTaskLab;
    private SharedPrefs mPrefs;
    private AlarmHandler<TaskItem> mAlarmHandler;
    private ActivityFragmentInteractionListener mInteractionListener;
    // Preference
    private boolean mIsAlarmStateChecked = false;
    private boolean mIsPriorityChecked = false;
    private boolean mIsEarlyReminderChecked = false;
    private boolean mIsWakeUpCallChecked = false;
    // Seek bar value
    int mHourProgress = 0;
    int mMinuteProgress = 0;
    // general
    private Context mContext;
    private Resources mResources;
    private SimpleDateFormat mDateFormat;
    private UUID mId;
    private Handler mHandler;
    private String mDetailContent = "";
    private Fragment mMasterListFragment = null;
    private boolean mIsUsedAsMasterDetail = false;
    private int mItemRequest;
    // This determined whether this fragment is been use to create or update the task.

    public static TaskOngoingDetailFragment newInstance(UUID id, String state) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ID, id);
        args.putString(ARG_STATE, state);
        TaskOngoingDetailFragment fragment = new TaskOngoingDetailFragment();
        fragment.setArguments(args);
        return fragment;
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContext = getActivity();
        mResources = getResources();
        mId = (UUID) getArguments().getSerializable(ARG_ID);
        mTaskLab = TasksLab.get(mContext);
        mPrefs = SharedPrefs.get(mContext);
        mDateFormat = new SimpleDateFormat(
                mResources.getString(R.string.date_format),
                Locale.getDefault());

        // This runs when creating a task.
        if (mId == null) {
            mItemRequest = Constant.REQUEST_ITEM_CREATE;
            // Create a new task.
            mItem = new TaskItem();

            // Read the preference.
            mIsAlarmStateChecked = (boolean) mPrefs.read(Constant.ALARM_PREF, true);
            mIsPriorityChecked = (boolean) mPrefs.read(Constant.PRIORITY_PREF, false);
            mIsEarlyReminderChecked = (boolean) mPrefs.read(Constant.REMINDER_PREF, false);
            mIsWakeUpCallChecked = (boolean) mPrefs.read(Constant.WAKE_UP_CALL_SWITCH_PREF, false);
        }
        // This runs when updating an existing task.
        else {
            mItemRequest = Constant.REQUEST_ITEM_UPDATE;
            // Get the event from the database
            mItem = mTaskLab.getItem(mId);
        }

        mItem.setState((String) getArguments().get(ARG_STATE));
        mHandler = new Handler();

        mAlarmHandler = new AlarmHandler<>(mContext, null);
        mAlarmHandler.start();
        mAlarmHandler.getLooper();

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_ongoing_detail, container, false);

        // Initialise the title container
        mTitleContainer = (TextInputLayout) view.findViewById(R.id.task_detail_title_container);
        mTitleEditText = (TextInputEditText) view.findViewById(R.id.task_detail_title);
        mTitleEditText.addTextChangedListener(new EditTextListener(mTitleEditText));

        // Initialise the location edit text
        mLocationEditText = (TextInputEditText) view.findViewById(R.id.task_detail_location);
        mLocationEditText.addTextChangedListener(new EditTextListener(mLocationEditText));

        // Initialise time text view
        mTimeTextView = (TextView) view.findViewById(R.id.task_detail_time);
        mTimeTextView.setText(CommonMethod.timeStringFormat(mItem.getTime()));
        mTimeTextView.setOnClickListener(new ClickListener());

        // Initialise time date view
        mDateTextView = (TextView) view.findViewById(R.id.task_detail_date);
        mDateTextView.setText(mDateFormat.format(mItem.getDueDate()));
        mDateTextView.setOnClickListener(new ClickListener());

        // Initialise time/date error text view
        mDateTimeErrorTextView = (TextView) view.findViewById(R.id.task_detail_date_time_error);
        mDateTimeErrorTextView.setVisibility(View.GONE);

        // Initialise the detail text view
        mDetailEditText = (EditText) view.findViewById(R.id.task_detail_detail_container);
        mDetailEditText.addTextChangedListener(new EditTextListener(mDetailEditText));
        mDetailEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (v.getId() == R.id.task_detail_detail_container) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_UP:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                }
                return false;
            }
        });

        // Initialise the alarm state switch
        mAlarmStateSwitch = (Switch) view.findViewById(R.id.task_detail_alarm_state);
        mAlarmStateSwitch.setOnCheckedChangeListener(new CheckListener());

        // Initialise the alarm before view group
        mAlarmBeforeViewGroup = (ViewGroup) view.findViewById(R.id.task_detail_event_reminder);
        mAlarmBeforeViewGroup.setVisibility(View.GONE);
        mAlarmBeforeTextView = (TextView) view.findViewById(R.id.event_reminder_text);

        mHourSeekBar = (SeekBar) view.findViewById(R.id.event_reminder__hour_seek_bar);
        mHourSeekBar.setMax(12);
        mHourSeekBar.setOnSeekBarChangeListener(new SeekBarListener());

        mMinuteSeekBar = (SeekBar) view.findViewById(R.id.event_reminder_minute_seek_bar);
        mMinuteSeekBar.setMax(11);
        mMinuteSeekBar.setOnSeekBarChangeListener(new SeekBarListener());

        // Initialise the alarm priority
        mPriorityCheckBox = (CheckBox) view.findViewById(R.id.task_detail_priority_check_box);
        mPriorityCheckBox.setOnCheckedChangeListener(new CheckListener());

        // Initialise the alarm wake up call
        mWakeUpSwitch = (Switch) view.findViewById(R.id.task_detail_wake_up_call_switch);
        mWakeUpSwitch.setOnCheckedChangeListener(new CheckListener());

        mWakeUpTimeViewGroup = (ViewGroup) view.findViewById(R.id.task_detail_wake_up_call_container);
        mWakeUpTimeViewGroup.setVisibility(View.GONE);

        mWakeUpTimeTextView = (TextView) view.findViewById(R.id.task_detail_wake_up_call_time);
        mWakeUpTimeTextView.setText(CommonMethod.timeStringFormat(mItem.getWakeUpCallTime()));
        mWakeUpTimeTextView.setOnClickListener(new ClickListener());

        mWakeUpErrorTextView = (TextView) view.findViewById(R.id.task_detail_wake_up_time_error);
        mWakeUpErrorTextView.setVisibility(View.GONE);

        // Initialise the alarm type choice
        mAlarmTypeSpinner = (Spinner) view.findViewById(R.id.task_detail_alarm_type_choice);
        mAlarmTypeSpinner.setSelection(mItem.getAlarmTypeChoice());
        mAlarmTypeSpinner.setOnItemSelectedListener(this);
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(
                mContext,
                R.array.alarm_option_type,
                android.R.layout.simple_spinner_item);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAlarmTypeSpinner.setAdapter(adapterType);

        updateUI();

        return view;
    }

    private void updateUI() {
        // Update the title and location
        mTitleEditText.setText(mItem.getTitle());
        mLocationEditText.setText(mItem.getLocation());

        // Update the date and time
        mTimeTextView.setText(CommonMethod.timeStringFormat(mItem.getTime()));
        mDateTextView.setText(mDateFormat.format(mItem.getDueDate()));

        // Update the detail
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mId == null) {
                    mDetailEditText.setText(mDetailContent);
                } else {
                    mDetailEditText.setText(
                            CommonMethod.readFile(mContext, mItem.getUUID().toString()));
                }
            }
        });

        // Update the alarm state switch
        mAlarmStateSwitch.setChecked(mItem.getAlarmStateChecked());

        // Update the hour and minute seek bar
        mHourSeekBar.setProgress(mItem.getDeltaTime() / 60);
        mMinuteSeekBar.setProgress(mItem.getDeltaTime() % 60 / 5);

        // Update the priority
        mPriorityCheckBox.setChecked(mItem.shouldRemindPriority());

        // Update the wake up call
        mWakeUpSwitch.setChecked(mItem.shouldWakeUpCall());
        mWakeUpTimeTextView.setText(CommonMethod.timeStringFormat(mItem.getWakeUpCallTime()));

        // Update the spinner
        mAlarmTypeSpinner.setSelection(mItem.getAlarmTypeChoice());

        // Update base on preference
        if (mId == null && !mIsUsedAsMasterDetail) {
            mAlarmStateSwitch.setChecked(mIsAlarmStateChecked);
            mPriorityCheckBox.setChecked(mIsPriorityChecked && mIsAlarmStateChecked);
            mWakeUpSwitch.setChecked(mIsWakeUpCallChecked && mIsAlarmStateChecked);

            int progress = (mIsEarlyReminderChecked && mIsAlarmStateChecked) ? 1 : 0;
            mMinuteSeekBar.setProgress(progress);
            mItem.setDeltaTime(progress * 5);

            String timeFormat = String.format(
                    mResources.getString(R.string.hour_format),
                    CommonMethod.timeComponent(getWakeUpCallTime()/ 60),
                    CommonMethod.timeComponent(getWakeUpCallTime() % 60));
            mWakeUpTimeTextView.setText(timeFormat);
        }

        if (!mAlarmStateSwitch.isChecked()) {
            mPriorityCheckBox.setChecked(false);
            mPriorityCheckBox.setEnabled(false);

            mWakeUpSwitch.setChecked(false);
            mWakeUpSwitch.setEnabled(false);

        } else {
            mPriorityCheckBox.setChecked(mItem.shouldRemindPriority());
            mPriorityCheckBox.setEnabled(true);

            mWakeUpSwitch.setChecked(mItem.shouldWakeUpCall());
            mWakeUpSwitch.setEnabled(true);
        }
    }

    private int getWakeUpCallTime() {
        String[] component = ((String) mPrefs
                .read(Constant.WAKE_UP_CALL_EDIT_PREF, "")).trim().split(":");

        int hour = 0;
        int minute = 0;

        try {
            hour = Integer.parseInt(component[0].trim());
            minute = Integer.parseInt(component[1].trim());
        } catch (NumberFormatException e) {
           e.printStackTrace();
        }
        return hour * 60 + minute;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.detail_fragment_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.detail_menu_save:
                if (saveItemCondition()) {
                    mInteractionListener.onFragmentDetailMenuClick(
                            mMasterListFragment, TaskOngoingDetailFragment.this);
                }
                return false;
            case R.id.detail_menu_delete:
                if (mId != null && !mItem.getState().isEmpty()) {
                    // Delete the item from the database.
                    mTaskLab.removeItem(mItem, mItem.getState());
                    // Remove all alarm bounded with this item.
                    mAlarmHandler.queueMessage(mItem, Constant.ALARM_REMOVE,
                            Constant.IDENTIFIER_TASK, -1);

                    // Delete the file.
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String path = mContext.getFilesDir().getAbsolutePath();
                            File file = new File(path + "/" + mItem.getUUID().toString());

                            if (file.exists()) file.delete();
                        }
                    }).start();
                }

                mInteractionListener.onFragmentDetailMenuClick(
                        mMasterListFragment, TaskOngoingDetailFragment.this);
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMasterListFragment != null) {
            mInteractionListener.onFragmentDetailReady(mMasterListFragment, this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constant.REQUEST_TIME_PICKER_DIALOG) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) return;

                int which = data.getIntExtra(TimePickerDialog.EXTRA_WHICH, -1);
                int hour = data.getIntExtra(TimePickerDialog.EXTRA_HOUR, 0);
                int minute = data.getIntExtra(TimePickerDialog.EXTRA_MINUTE, 0);

                switch (which) {
                    // In this case, ALARM_START is the task alarm
                    case Constant.ALARM_START:
                        // Update the item
                        mItem.setTime(hour * 60 + minute);
                        // Update mTimeTextView
                        mTimeTextView.setText(CommonMethod.timeStringFormat(hour * 60 + minute));
                        break;
                    // In this case, ALARM_END is the task wake up alarm
                    case Constant.ALARM_END:
                        // Update the item
                        mItem.setWakeUpCallTime(hour * 60 + minute);
                        // Update mWakeUpTimeTextView
                        mWakeUpTimeTextView.setText(CommonMethod.timeStringFormat(hour * 60 + minute));
                        break;
                    default:
                        break;
                }

                // Check for possible date and time errors.
                checkDateTimeCondition();
            }
        }

        if (requestCode == Constant.REQUEST_DATE_PICKER_DIALOG) {
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) return;

                Date date = (Date) data.getSerializableExtra(DatePickerDialog.EXTRA_DATE);

                mItem.setDueDate(date);
                mDateTextView.setText(mDateFormat.format(date));

                // Check for possible date and time errors.
                checkDateTimeCondition();
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!mIsUsedAsMasterDetail) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_TITLE, mItem.getTitle());
            intent.putExtra(EXTRA_LOCATION, mItem.getLocation());
            intent.putExtra(EXTRA_DETAIL, mDetailContent);
            intent.putExtra(EXTRA_STATE, mItem.getState());
            intent.putExtra(EXTRA_STATE_CHECKED, mItem.getAlarmStateChecked());
            intent.putExtra(EXTRA_PRIORITY, mItem.shouldRemindPriority());
            intent.putExtra(EXTRA_WAKE_UP_CALL, mItem.shouldWakeUpCall());
            intent.putExtra(EXTRA_TIME, mItem.getTime());
            intent.putExtra(EXTRA_WAKE_UP_CALL_TIME, mItem.getWakeUpCallTime());
            intent.putExtra(EXTRA_DELTA_TIME, mItem.getDeltaTime());
            intent.putExtra(EXTRA_ALARM_TYPE_CHOICE, mItem.getAlarmTypeChoice());
            intent.putExtra(EXTRA_DUE_DATE, mItem.getDueDate());
            intent.putExtra(EXTRA_COMPLETED_DATE, mItem.getCompleteDate());
            intent.putExtra(EXTRA_UUID, mId);
            intent.putExtra(EXTRA_ITEM_REQUEST, mItemRequest);
            // Send this to the caller activity.
            getActivity().setResult(Activity.RESULT_OK, intent);
            getActivity().finish();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mItem.setAlarmTypeChoice(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Do nothing
    }

    @Override
    protected void onMasterListUsed(TaskListFragment fragment) {
        mMasterListFragment = fragment;
    }

    @Override
    protected void onUseAsMasterDetail(boolean isMasterDetail) {
        mIsUsedAsMasterDetail = isMasterDetail;
    }

    public void updateDetail(Intent intent) {
        if (mIsUsedAsMasterDetail) {
            mItem.setTitle(intent.getStringExtra(EXTRA_TITLE));
            mItem.setLocation(intent.getStringExtra(EXTRA_LOCATION));
            mItem.setState(intent.getStringExtra(EXTRA_STATE));
            mItem.setAlarmStateChecked(intent.getBooleanExtra(EXTRA_STATE_CHECKED, false));
            mItem.setShouldRemindPriority(intent.getBooleanExtra(EXTRA_PRIORITY, false));
            mItem.setShouldWakeUpCall(intent.getBooleanExtra(EXTRA_WAKE_UP_CALL, false));
            mItem.setTime(intent.getIntExtra(EXTRA_TIME, 0));
            mItem.setWakeUpCallTime(intent.getIntExtra(EXTRA_WAKE_UP_CALL_TIME, 0));
            mItem.setDeltaTime(intent.getIntExtra(EXTRA_DELTA_TIME, 0));
            mItem.setAlarmTypeChoice(intent.getIntExtra(EXTRA_ALARM_TYPE_CHOICE, 0));
            mItem.setDueDate((Date) intent.getSerializableExtra(EXTRA_DUE_DATE));
            mItem.setCompleteDate((Date) intent.getSerializableExtra(EXTRA_COMPLETED_DATE));
            mDetailContent = intent.getStringExtra(EXTRA_DETAIL);
            updateUI();
        }
    }

    private boolean checkDateTimeCondition() {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Date today = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();

        Date date = mItem.getDueDate();
        int time = mItem.getTime();
        int wakeUpCallTime = mItem.getWakeUpCallTime();

        // Check if the date is in the past
        // Note: the date shall not be set in the past
        if (date.before(today)) {
            // Raise an error
            mDateTimeErrorTextView.setVisibility(View.VISIBLE);
            mDateTimeErrorTextView.setText(mResources.getString(R.string.error_date_past));
            mDateTextView.setBackground(
                    ContextCompat.getDrawable(mContext, R.drawable.rectanglular_shape_error));
            return false;
        }
        // Check the time if the date is set as today.
        // Note: the time shall not be set in the past
        else if (date.equals(today)) {
            calendar.setTime(new Date());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            if (time < hour * 60 + minute) {
                // Raise an error
                mDateTimeErrorTextView.setVisibility(View.VISIBLE);
                mDateTimeErrorTextView.setText(mResources.getString(R.string.error_time_past));
                mTimeTextView.setBackground(
                        ContextCompat.getDrawable(mContext, R.drawable.rectanglular_shape_error));
                return false;
            }
        }

        // Check if the wake up call time is in the past
        if (wakeUpCallTime > time && mWakeUpSwitch.isChecked()) {
            // Raise an error.
            mDateTimeErrorTextView.setVisibility(View.VISIBLE);
            mDateTimeErrorTextView.setText(mResources.getString(R.string.error_wake_up_call_time));

            mWakeUpErrorTextView.setVisibility(View.VISIBLE);
            mWakeUpErrorTextView.setText(mResources.getString(R.string.error_wake_up_call_time));

            mWakeUpTimeTextView.setBackground(
                    ContextCompat.getDrawable(mContext, R.drawable.rectanglular_shape_error));
            mTimeTextView.setBackground(
                    ContextCompat.getDrawable(mContext, R.drawable.rectanglular_shape_error));

            return false;
        } else if (wakeUpCallTime < time && date.equals(today) && mWakeUpSwitch.isChecked()) {
            calendar.setTime(new Date());
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            if (wakeUpCallTime < hour * 60 + minute) {
                // Raise an error.
                mDateTimeErrorTextView.setVisibility(View.VISIBLE);
                mDateTimeErrorTextView.setText(mResources.getString(R.string.error_wake_up_call_time_pass));

                mWakeUpErrorTextView.setVisibility(View.VISIBLE);
                mWakeUpErrorTextView.setText(mResources.getString(R.string.error_wake_up_call_time_pass));

                mWakeUpTimeTextView.setBackground(
                        ContextCompat.getDrawable(mContext, R.drawable.rectanglular_shape_error));

                return false;
            }
        }

        mDateTimeErrorTextView.setVisibility(View.GONE);
        mTimeTextView.setBackground(
                ContextCompat.getDrawable(mContext, R.drawable.rectangular_shape));
        mDateTextView.setBackground(
                ContextCompat.getDrawable(mContext, R.drawable.rectangular_shape));

        mWakeUpErrorTextView.setVisibility(View.GONE);
        mWakeUpTimeTextView.setBackground(
                ContextCompat.getDrawable(mContext, R.drawable.rectangular_shape));

        return true;
    }

    private boolean saveItemCondition() {

        // Date and time condition must satisfy before saving the item.
        if (!checkDateTimeCondition()) return false;

        // The title edit text must not be empty
        if (mItem.getTitle().isEmpty()) {
            // Raise an error
            mTitleContainer.setErrorEnabled(true);
            mTitleContainer.setError(mResources.getString(R.string.error_title_required));
            return false;
        } else {
            mTitleContainer.setErrorEnabled(false);
            mTitleContainer.setError(null);
        }

        // Create a file containing the detail of the task;
        CommonMethod.createFile(mContext, mItem.getUUID().toString(), mDetailContent);

        // Calculate the interval of time at which the task color must change.
        int hour = mItem.getTime() / 60;
        int minute = mItem.getTime() % 60;

        Calendar currentTime = Calendar.getInstance();

        Calendar dueTime = Calendar.getInstance();
        dueTime.setTime(mItem.getDueDate());
        dueTime.set(Calendar.HOUR_OF_DAY, hour);
        dueTime.set(Calendar.MINUTE, minute);
        dueTime.set(Calendar.SECOND, 0);
        dueTime.set(Calendar.MILLISECOND, 0);

        long interval = (dueTime.getTimeInMillis() - currentTime.getTimeInMillis()) / 3;

        // Update the interval
        mItem.setIntervalTime(interval);

        String state = mItem.getState();

        // Save the task
        if (mId == null && !state.isEmpty()) {
            // Add item into the database.
            mTaskLab.addItem(mItem, mItem.getState());

            // Add alarm.
            if (mAlarmStateSwitch.isChecked()) {
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_ADD,
                        Constant.IDENTIFIER_TASK, -1);
            }
        }

        if (mId != null && !state.isEmpty()) {
            // Update item.
            mTaskLab.updateItem(mItem, state);

            // Update alarm.
            if (mAlarmStateSwitch.isChecked()) {
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_ADD,
                        Constant.IDENTIFIER_TASK, -1);
            } else {
                // Remove alarm
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_REMOVE,
                        Constant.IDENTIFIER_TASK, -1);
            }
        }

        return true;
    }

    /**
     * Responds to view clicks
     */
    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.task_detail_time:
                    // Open a time picker dialog
                    // Constant.ALARM_START represents the actual alarm time
                    showDialog(Constant.ALARM_START);
                    break;
                case R.id.task_detail_date:
                    // Open a date picker dialog
                    DialogFragment datePickerDialog = DatePickerDialog.newInstance(mItem.getDueDate());
                    datePickerDialog.setTargetFragment(TaskOngoingDetailFragment.this,
                            Constant.REQUEST_DATE_PICKER_DIALOG);
                    datePickerDialog.show(getFragmentManager(), DatePickerDialog.TAG);
                    break;
                case R.id.task_detail_wake_up_call_time:
                    // Open a time picker dialog
                    // Constant.ALARM_END represents the wake up alarm time
                    showDialog(Constant.ALARM_END);
                    break;
                default:
                    break;
            }
        }

        /**
         * @param whichAlarm a constant representing either the actual alarm time or
         *                   the wake up call alarm time
         */
        private void showDialog(int whichAlarm) {
            DialogFragment timePickerDialog = TimePickerDialog
                    .newInstance(whichAlarm);
            timePickerDialog.setTargetFragment(TaskOngoingDetailFragment.this,
                    Constant.REQUEST_TIME_PICKER_DIALOG);
            timePickerDialog.show(getFragmentManager(), TimePickerDialog.TAG);
        }
    }

    /**
     * Responds to view check
     */
    private class CheckListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.task_detail_alarm_state:
                    mItem.setAlarmStateChecked(isChecked);

                    if (!isChecked) {
                        // Set the progress bar to 0 and hide the alarm before view group
                        mHourSeekBar.setProgress(0);
                        mMinuteSeekBar.setProgress(0);
                        mAlarmBeforeViewGroup.setVisibility(View.GONE);
                        // Disable the alarm priority check box
                        mPriorityCheckBox.setChecked(false);
                        mPriorityCheckBox.setEnabled(false);
                        // Disable the wake up call
                        mWakeUpSwitch.setChecked(false);
                        mWakeUpSwitch.setEnabled(false);
                    } else {
                        // Show the alarm before view group
                        mAlarmBeforeViewGroup.setVisibility(View.VISIBLE);
                        // Enable the priority check box
                        mPriorityCheckBox.setEnabled(true);
                        // Enable the wake up call
                        mWakeUpSwitch.setEnabled(true);
                    }
                    break;
                case R.id.task_detail_priority_check_box:
                    mItem.setShouldRemindPriority(isChecked);
                    break;
                case R.id.task_detail_wake_up_call_switch:
                    mItem.setShouldWakeUpCall(isChecked);

                    if (!isChecked) {
                        // Set the wake up call time to 00:00
                        mItem.setWakeUpCallTime(0);
                        // Hide the wake up call view group
                        mWakeUpTimeViewGroup.setVisibility(View.GONE);
                        // Hide the wake up call error
                        mWakeUpErrorTextView.setVisibility(View.GONE);
                        // Hide the date time error
                        mDateTimeErrorTextView.setVisibility(View.GONE);
                        mTimeTextView.setBackground(
                                ContextCompat.getDrawable(mContext, R.drawable.rectangular_shape));
                        // Update the wake up call time text view
                        mWakeUpTimeTextView.setText(CommonMethod.timeStringFormat(0));

                    } else {
                        // Show the wake up call view group
                        mWakeUpTimeViewGroup.setVisibility(View.VISIBLE);
                        int wakeUpCall;
                        // If the wake up call setting is on use the wake up call setting values
                        // Else set the wake up call time to 00:00
                        if (mId == null && mIsWakeUpCallChecked) {
                            wakeUpCall = getWakeUpCallTime();
                            mItem.setWakeUpCallTime(wakeUpCall);
                        } else {
                            wakeUpCall = mItem.getWakeUpCallTime();
                        }
                        mWakeUpTimeTextView.setText(CommonMethod.timeStringFormat(wakeUpCall));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Responds to the change of text in an edit text
     */
    private class EditTextListener implements TextWatcher {
        private EditText mEditText;

        public EditTextListener(EditText editText) {
            mEditText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // Remove the error
            mTitleContainer.setErrorEnabled(false);
            mTitleContainer.setError(null);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            switch (mEditText.getId()) {
                case R.id.task_detail_title:
                    // Update the item
                    mItem.setTitle(s.toString());
                    break;
                case R.id.task_detail_location:
                    mItem.setLocation(s.toString());
                    break;
                case R.id.task_detail_detail_container:
                    mDetailContent = s.toString();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (mEditText.getId()) {
                case R.id.task_detail_title:
                    // check if the title edit text is empty
                    if (mItem.getTitle().isEmpty()) {
                        // Raise an error
                        mTitleContainer.setErrorEnabled(true);
                        mTitleContainer.setError(mResources.getString(R.string.error_title_required));
                    } else {
                        // Remove the error
                        mTitleContainer.setErrorEnabled(false);
                        mTitleContainer.setError(null);
                    }
                    break;
                case R.id.task_detail_location:
                    if (mItem.getLocation().isEmpty()) {
                        mItem.setLocation(mResources.getString(R.string.no_location));
                    }
                    break;
                case R.id.task_detail_detail_container:
                    if (mDetailContent.equals("")) {
                        mDetailContent = mResources.getString(
                                R.string.notification_alarm_detail_message_not_given);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class SeekBarListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            switch (seekBar.getId()) {
                case R.id.event_reminder__hour_seek_bar:
                    mHourProgress = progress;
                    break;
                case R.id.event_reminder_minute_seek_bar:
                    mMinuteProgress = progress * 5;
                    break;
                default:
                    break;
            }

            if (mHourProgress == 0 && mMinuteProgress == 0) {
                mAlarmBeforeTextView.setText(mResources.getString(R.string.alarm_before));
            } else if (mHourProgress > 0 && mMinuteProgress == 0) {
                String plural = (mHourProgress > 1) ? "s" : "";
                mAlarmBeforeTextView.setText(String.format(
                        mResources.getString(R.string.alarm_before_hour),
                        CommonMethod.timeComponent(mHourProgress),
                        plural));
            } else if (mHourProgress == 0 && mMinuteProgress > 0) {
                mAlarmBeforeTextView.setText(String.format(
                        mResources.getString(R.string.alarm_before_minute),
                        CommonMethod.timeComponent(mMinuteProgress)));
            } else {
                mAlarmBeforeTextView.setText(String.format(
                        mResources.getString(R.string.alarm_before_time),
                        CommonMethod.timeComponent(mHourProgress),
                        CommonMethod.timeComponent(mMinuteProgress)));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // Do nothing
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mItem.setDeltaTime(mHourProgress * 60 + mMinuteProgress);
        }
    }
}