package com.aston.tanion.schedule.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.aston.tanion.schedule.Handler.AlarmHandler;
import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.database.SharedPrefs;
import com.aston.tanion.schedule.database.TimetableLab;
import com.aston.tanion.schedule.dialog.TimePickerDialog;
import com.aston.tanion.schedule.model.TimetableItem;
import com.aston.tanion.schedule.utility.ActivityFragmentInteractionListener;
import com.aston.tanion.schedule.utility.CommonMethod;
import com.aston.tanion.schedule.utility.Constant;
import com.aston.tanion.schedule.utility.MasterDetailFragment;

import java.util.List;
import java.util.UUID;

/**
 * Created by Aston Tanion on 06/02/2016.
 */
public class TimetableDetailFragment extends MasterDetailFragment<TimetableListFragment>
        implements AdapterView.OnItemSelectedListener {
    public static final String TAG = "TimetableDetailFragment";
    private static final String ARG_ID = "time_table_id";
    private static final String ARG_DAY = "time_table_day";
    private static final String ARG_WEEK = "time_table_week";

    public static final String EXTRA_TITLE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.TITLE";
    public static final String EXTRA_LOCATION =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.LOCATION";
    public static final String EXTRA_TIME_FORMAT =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.TIME_FORMAT";
    public static final String EXTRA_START_TIME =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.START_TIME";
    public static final String EXTRA_END_TIME =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.END_TIME";
    public static final String EXTRA_DELTA_TIME =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.DELTA_TIME";
    public static final String EXTRA_ALARM_TYPE_CHOICE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.ALARM_TYPE_CHOICE";
    public static final String EXTRA_START_STATE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.START_STATE";
    public static final String EXTRA_END_STATE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.END_STATE";
    public static final String EXTRA_UUID =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.UUID";
    public static final String EXTRA_ITEM_REQUEST =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.fragment.ITEM_REQUEST";

    // title and location
    private TextInputLayout mTitleContainer;
    private TextInputEditText mTitleEditText;
    private TextInputEditText mLocationEditText;
    // start and end time
    private TextView mStartTimeTextView;
    private TextView mEndTimeTextView;
    private TextView mTimeErrorTextView;
    // start and end alarm switch
    private Switch mStartAlarmSwitch;
    private Switch mEndAlarmSwitch;
    // alarm before view group
    private ViewGroup mAlarmBeforeViewGroup;
    private TextView mAlarmBeforeTextView;
    private SeekBar mHourSeekBar;
    private SeekBar mMinuteSeekBar;
    // alarm type spinner
    private Spinner mAlarmTypeSpinner;


    // classes
    private TimetableItem mItem;
    private TimetableLab mTimetableLab;
    private SharedPrefs mPrefs;
    private AlarmHandler<TimetableItem> mAlarmHandler;
    private ActivityFragmentInteractionListener mInteractionListener;
    // Preference
    private boolean mShouldSetAlarm = false;
    private boolean mIsEarlyReminderChecked = false;
    // general
    private Context mContext;
    private Resources mResources;
    private UUID mId;
    private String mDay;
    private String mWeek;
    // Seek bar value
    private int mHourProgress = 0;
    private int mMinuteProgress = 0;
    //general
    private TimetableListFragment mMasterListFragment = null;
    private boolean mIsUsedAsMasterDetail = false;
    // This determined whether this fragment is been use to create or update the task.
    private int mItemRequest;

    public static TimetableDetailFragment newInstance(UUID uuid, String day, String week) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_ID, uuid);
        args.putString(ARG_DAY, day);
        args.putString(ARG_WEEK, week);
        TimetableDetailFragment fragment = new TimetableDetailFragment();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mContext = getActivity();
        mResources = getResources();
        mId = (UUID) getArguments().getSerializable(ARG_ID);
        mDay = getArguments().getString(ARG_DAY);
        mWeek = getArguments().getString(ARG_WEEK);
        mPrefs = SharedPrefs.get(mContext);
        mTimetableLab = TimetableLab.get(mContext);

        // This runs when creating an event
        if (mId == null) {
            mItemRequest = Constant.REQUEST_ITEM_CREATE;
            // Create a new event
            mItem = new TimetableItem();

            // Read the preference
            mShouldSetAlarm = (boolean) mPrefs.read(Constant.ALARM_PREF, true);
            mIsEarlyReminderChecked = (boolean) mPrefs.read(Constant.REMINDER_PREF, false);
        }
        // This runs when updating an existing event
        else {
            mItemRequest = Constant.REQUEST_ITEM_UPDATE;
            // Get the event from the database
            mItem = mTimetableLab.getItem(mId, mDay, mWeek);
        }

        mAlarmHandler = new AlarmHandler<>(mContext, mDay);
        mAlarmHandler.start();
        mAlarmHandler.getLooper();
    }

    @Nullable
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timetable_detail, container, false);

        // Initialise the title container
        mTitleContainer = (TextInputLayout) view.findViewById(R.id.timetable_detail_title_container);
        mTitleEditText = (TextInputEditText) view.findViewById(R.id.timetable_detail_title);
        mTitleEditText.addTextChangedListener(new EditTextListener(mTitleEditText));

        // Initialise the location edit text
        mLocationEditText = (TextInputEditText) view.findViewById(R.id.timetable_detail_location);
        mLocationEditText.addTextChangedListener(new EditTextListener(mLocationEditText));

        // Initialise the start time text view
        mStartTimeTextView = (TextView) view.findViewById(R.id.timetable_detail_start_time);
        mStartTimeTextView.setText(CommonMethod.timeStringFormat(mItem.getStartTime()));
        mStartTimeTextView.setOnClickListener(new ClickListener());

        // Initialise the end time text view
        mEndTimeTextView = (TextView) view.findViewById(R.id.timetable_detail_end_time);
        mEndTimeTextView.setText(CommonMethod.timeStringFormat(mItem.getEndTime()));
        mEndTimeTextView.setOnClickListener(new ClickListener());

        // Initialise the time error text view
        mTimeErrorTextView = (TextView) view.findViewById(R.id.timetable_time_errors);
        mTimeErrorTextView.setVisibility(View.GONE);

        // Initialise the start and end alarm switch
        mStartAlarmSwitch = (Switch) view.findViewById(R.id.timetable_detail_alarm_start_switch);
        mStartAlarmSwitch.setOnCheckedChangeListener(new CheckListener());

        mEndAlarmSwitch = (Switch) view.findViewById(R.id.timetable_detail_alarm_end_switch);
        mEndAlarmSwitch.setOnCheckedChangeListener(new CheckListener());

        // Initialise the alarm before view group
        mAlarmBeforeViewGroup = (ViewGroup) view.findViewById(R.id.timetable_detail_event_reminder);
        mAlarmBeforeViewGroup.setVisibility(View.GONE);
        mAlarmBeforeTextView = (TextView) view.findViewById(R.id.event_reminder_text);

        mHourSeekBar = (SeekBar) view.findViewById(R.id.event_reminder__hour_seek_bar);
        mHourSeekBar.setMax(12);
        mHourSeekBar.setOnSeekBarChangeListener(new SeekBarListener());

        mMinuteSeekBar = (SeekBar) view.findViewById(R.id.event_reminder_minute_seek_bar);
        mMinuteSeekBar.setMax(11);
        mMinuteSeekBar.setOnSeekBarChangeListener(new SeekBarListener());

        // Initialise the alarm type choice
        mAlarmTypeSpinner = (Spinner) view.findViewById(R.id.timetable_detail_alarm_type_choice);
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

        // Update the start and end time
        mStartTimeTextView.setText(CommonMethod.timeStringFormat(mItem.getStartTime()));
        mEndTimeTextView.setText(CommonMethod.timeStringFormat(mItem.getEndTime()));

        // Update the alarm state and end switch
        mStartAlarmSwitch.setChecked(mItem.getAlarmStartState());
        mEndAlarmSwitch.setChecked(mItem.getAlarmEndState());

        // Update the hour and minute seek bar
        mHourSeekBar.setProgress(mItem.getDeltaTime() / 60);
        mMinuteSeekBar.setProgress(mItem.getDeltaTime() % 60 / 5);

        // Update the spinner
        mAlarmTypeSpinner.setSelection(mItem.getAlarmTypeChoice());

        // Update base on preference
        if (mId == null && !mIsUsedAsMasterDetail) {
            mStartAlarmSwitch.setChecked(mShouldSetAlarm);
            mEndAlarmSwitch.setChecked(mShouldSetAlarm);

            int progress = (mIsEarlyReminderChecked && mShouldSetAlarm) ? 1 : 0;
            mMinuteSeekBar.setProgress(progress);
            mItem.setDeltaTime(progress * 5);

        }
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
                            mMasterListFragment, TimetableDetailFragment.this);
                    return true;
                }
                return false;
            case R.id.detail_menu_delete:
                if (mItem != null) {
                    // Delete the item from the database.
                    mTimetableLab.removeItem(mItem, mDay);
                    // Remove all alarm bounded with this item.
                    mAlarmHandler.queueMessage(mItem, Constant.ALARM_REMOVE,
                            Constant.IDENTIFIER_TIMETABLE, -1);
                }

                mInteractionListener.onFragmentDetailMenuClick(
                        mMasterListFragment, TimetableDetailFragment.this);
                return true;
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
                        mItem.setStartTime(hour * 60 + minute);
                        // Update the mStartTimeTextView
                        mStartTimeTextView.setText(CommonMethod
                                .timeStringFormat(hour * 60 + minute));
                        break;
                    // In this case, ALARM_END is the task wake up alarm
                    case Constant.ALARM_END:
                        // Update the item
                        mItem.setEndTime(hour * 60 + minute);
                        // Update the mEndTimeTextView
                        mEndTimeTextView.setText(CommonMethod.timeStringFormat(hour * 60 + minute));
                        break;
                    default:
                        break;
                }

                // Check for possible time errors
                checkTimeCondition();
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
            intent.putExtra(EXTRA_TIME_FORMAT, mItem.getTimeFormat());
            intent.putExtra(EXTRA_START_TIME, mItem.getStartTime());
            intent.putExtra(EXTRA_END_TIME, mItem.getEndTime());
            intent.putExtra(EXTRA_DELTA_TIME, mItem.getDeltaTime());
            intent.putExtra(EXTRA_ALARM_TYPE_CHOICE, mItem.getAlarmTypeChoice());
            intent.putExtra(EXTRA_START_STATE, mItem.getAlarmStartState());
            intent.putExtra(EXTRA_END_STATE, mItem.getAlarmEndState());
            intent.putExtra(EXTRA_UUID, mId);
            intent.putExtra(EXTRA_ITEM_REQUEST, mItemRequest);

            // send this to the caller activity
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

    private boolean checkTimeCondition() {

        // Check if the start time is greater than the end time/
        // If it is raise and error.
        if (mItem.getStartTime() >= mItem.getEndTime()) {

            mStartTimeTextView.setBackground(
                    ContextCompat.getDrawable(mContext, R.drawable.rectanglular_shape_error));

            mEndTimeTextView.setBackground(
                    ContextCompat.getDrawable(mContext, R.drawable.rectanglular_shape_error));

            mTimeErrorTextView.setVisibility(View.VISIBLE);
            mTimeErrorTextView.setText(
                    mResources.getString(R.string.error_time_equality));

            return false;
        }

        // Get a list of all event, use this to check that no two events overlap
        List<TimetableItem> itemsCompare = mTimetableLab.getItems(mDay, mWeek);

        // Do not compare the event with itself when updating it.
        if (mId != null) {
            for (TimetableItem i : itemsCompare) {
                if (i.getUUID().equals(mId)) {
                    itemsCompare.remove(i);
                    break;
                }
            }
        }

        // This loops through the events and check if their do not overlap
        // If two event overlap, raise an error.
        for (int i = 0; i < itemsCompare.size(); i++) {
            TimetableItem item = itemsCompare.get(i);
            if (mItem.getEndTime() <= item.getStartTime()) {
            } else if (mItem.getStartTime() >= item.getEndTime()) {
            } else {
                mStartTimeTextView.setBackground(ContextCompat
                        .getDrawable(mContext, R.drawable.rectanglular_shape_error));

                mEndTimeTextView.setBackground(ContextCompat
                        .getDrawable(mContext, R.drawable.rectanglular_shape_error));

                mTimeErrorTextView.setVisibility(View.VISIBLE);
                mTimeErrorTextView.setText(
                        mResources.getString(R.string.error_time_overlap));

                return false;
            }
        }

        // Update the view
        mTimeErrorTextView.setVisibility(View.GONE);
        mStartTimeTextView.setBackground(
                ContextCompat.getDrawable(mContext, R.drawable.rectangular_shape));
        mEndTimeTextView.setBackground(
                ContextCompat.getDrawable(mContext, R.drawable.rectangular_shape));

        return true;
    }

    private boolean saveItemCondition() {

        // The time condition must be satisfy before saving the item
        if (!checkTimeCondition()) return false;

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

        if (mItem.getLocation().isEmpty()) {
            mItem.setLocation(mResources.getString(R.string.no_location));
        }

        // This is use in TimetableTabFragment to display the time.
        mItem.setTimeFormat(CommonMethod.timeStringFormat(mItem.getStartTime()) + " - " +
                CommonMethod.timeStringFormat(mItem.getEndTime()));

        String currentWeekIdString = (String)
                SharedPrefs.get(mContext).read(Constant.WEEK_CURRENT_ID_PREF, "");

        // This runs when creating an event.
        if (mId == null) {
            // Create the item.
            mTimetableLab.addItem(mItem, mDay, mWeek);
            // Add the start alarm.
            if (mItem.getAlarmStartState() && mWeek.equals(currentWeekIdString)) {
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_ADD,
                        Constant.IDENTIFIER_TIMETABLE, Constant.ALARM_START);
            }
            // Add the end alarm
            if (mItem.getAlarmEndState() && mWeek.equals(currentWeekIdString)) {
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_ADD,
                        Constant.IDENTIFIER_TIMETABLE, Constant.ALARM_END);
            }
        }

        // This runs when updating an event.
        if (mId != null) {
            // Update the item.
            mTimetableLab.updateItem(mItem, mDay, mWeek);
            // Update the start alarms.
            if (mItem.getAlarmStartState() && mWeek.equals(currentWeekIdString)) {
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_UPDATE,
                        Constant.IDENTIFIER_TIMETABLE, Constant.ALARM_START);
            } else {
                // Clear start alarm
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_CLEAR,
                        Constant.IDENTIFIER_TIMETABLE, Constant.ALARM_START);
            }

            // Update the end alarm
            if (mItem.getAlarmEndState() && mWeek.equals(currentWeekIdString)) {
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_UPDATE,
                        Constant.IDENTIFIER_TIMETABLE, Constant.ALARM_END);
            } else if (mWeek.equals(currentWeekIdString)) {
                // Clear the end alarm
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_CLEAR,
                        Constant.IDENTIFIER_TIMETABLE, Constant.ALARM_END);
            }

            if (!mItem.getAlarmStartState() && !mItem.getAlarmEndState() &&
                    mWeek.equals(currentWeekIdString)) {
                // Remove alarm
                mAlarmHandler.queueMessage(mItem, Constant.ALARM_REMOVE,
                        Constant.IDENTIFIER_TIMETABLE, -1);
            }
        }

        return true;
    }

    @Override
    protected void onMasterListUsed(TimetableListFragment fragment) {
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
            mItem.setTimeFormat(intent.getStringExtra(EXTRA_TIME_FORMAT));
            mItem.setStartTime(intent.getIntExtra(EXTRA_START_TIME, 0));
            mItem.setEndTime(intent.getIntExtra(EXTRA_END_TIME, 0));
            mItem.setDeltaTime(intent.getIntExtra(EXTRA_DELTA_TIME, 0));
            mItem.setAlarmTypeChoice(intent.getIntExtra(EXTRA_ALARM_TYPE_CHOICE, 0));
            mItem.setAlarmStartState(intent.getBooleanExtra(EXTRA_START_STATE, false));
            mItem.setAlarmEndState(intent.getBooleanExtra(EXTRA_END_STATE, false));

            updateUI();
        }
    }


    /**
     * Responds to view clicks
     */
    private class ClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.timetable_detail_start_time:
                    // Open a time picker dialog
                    showDialog(Constant.ALARM_START);
                    break;
                case R.id.timetable_detail_end_time:
                    // Open a time picker dialog
                    showDialog(Constant.ALARM_END);
                    break;
                default:
                    break;
            }
        }

        /**
         * @param whichAlarm a constant representing either the start of the end alarm.
         */
        private void showDialog(int whichAlarm) {
            DialogFragment timePickerDialog = TimePickerDialog
                    .newInstance(whichAlarm);
            timePickerDialog.setTargetFragment(TimetableDetailFragment.this,
                    Constant.REQUEST_TIME_PICKER_DIALOG);
            timePickerDialog.show(getFragmentManager(), TimePickerDialog.TAG);
        }
    }

    private class CheckListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.timetable_detail_alarm_start_switch:
                    // Update the item
                    mItem.setAlarmStartState(isChecked);
                    // Update the alarm before view group
                    reminderBeforeState();
                    break;
                case R.id.timetable_detail_alarm_end_switch:
                    // Update the item
                    mItem.setAlarmEndState(isChecked);
                    // Update the alarm before view group
                    reminderBeforeState();
                    break;
                default:
                    break;
            }
        }

        private void reminderBeforeState() {
            if (!mItem.getAlarmStartState() && !mItem.getAlarmEndState()) {
                // Set the progress bar to 0 and hide the alarm before view group
                mHourSeekBar.setProgress(0);
                mMinuteSeekBar.setProgress(0);
                mAlarmBeforeViewGroup.setVisibility(View.GONE);
            } else {
                // Show the alarm before view group
                mAlarmBeforeViewGroup.setVisibility(View.VISIBLE);
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
                case R.id.timetable_detail_title:
                    // Update the item
                    mItem.setTitle(s.toString());
                    break;
                case R.id.timetable_detail_location:
                    mItem.setLocation(s.toString());
                    break;
                default:
                    break;
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (mEditText.getId()) {
                case R.id.timetable_detail_title:
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
                case R.id.timetable_detail_location:
                    if (mItem.getLocation().isEmpty()) {
                        mItem.setLocation(mResources.getString(R.string.no_location));
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