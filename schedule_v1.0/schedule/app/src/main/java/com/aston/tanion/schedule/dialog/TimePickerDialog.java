package com.aston.tanion.schedule.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import com.aston.tanion.schedule.R;

import java.util.Calendar;

/**
 * Created by Aston Tanion on 06/02/2016.
 */
public class TimePickerDialog extends DialogFragment {
    public static final String TAG = "TimePickerDialog";
    private static final String ARG_WHICH_TIME = "which_time";
    public static final String EXTRA_HOUR =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.HOUR";
    public static final String EXTRA_MINUTE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.MINUTE";
    public static final String EXTRA_WHICH =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.WHICH";

    private TimePicker mTimePicker;
    private int mWhichTime;

    public static TimePickerDialog newInstance(int whichTime) {
        Bundle args = new Bundle();
        args.putInt(ARG_WHICH_TIME, whichTime);
        TimePickerDialog fragment = new TimePickerDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        mWhichTime = getArguments().getInt(ARG_WHICH_TIME, 0);

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_time_picker, null, false);

        // Initialize of timePicker dialog.
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        mTimePicker = (TimePicker) view.findViewById(R.id.time_picker_dialog);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            mTimePicker.setHour(hour);
            mTimePicker.setMinute(minute);
        } else {
            mTimePicker.setCurrentHour(hour);
            mTimePicker.setCurrentMinute(minute);
        }

        return new AlertDialog.Builder(getActivity())
                .setTitle(null)
                .setView(view)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mTimePicker.clearFocus();

                        int hour;
                        int minute;

                        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                            hour = mTimePicker.getHour();
                            minute = mTimePicker.getMinute();
                        } else {
                            hour = mTimePicker.getCurrentHour();
                            minute = mTimePicker.getCurrentMinute();
                        }

                        sendResult(Activity.RESULT_OK, hour, minute, mWhichTime);
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, int hour, int minute, int whichTime) {
        if (getTargetFragment() == null) return;

        Intent data = new Intent();
        data.putExtra(EXTRA_HOUR, hour);
        data.putExtra(EXTRA_MINUTE, minute);
        data.putExtra(EXTRA_WHICH, whichTime);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, data);
    }
}