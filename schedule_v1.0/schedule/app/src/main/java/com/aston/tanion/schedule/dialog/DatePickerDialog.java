package com.aston.tanion.schedule.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.aston.tanion.schedule.R;

/**
 * Created by Aston Tanion on 17/02/2016.
 */;import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DatePickerDialog extends DialogFragment {
    public static final String TAG = "DatePickerDialog";
    public static final String EXTRA_DATE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.dialog.DATE";
    private static final String ARG_DATE = "DATE";

    private DatePicker mDatePicker;

    public static DatePickerDialog newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        DatePickerDialog dialog = new DatePickerDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Date date = (Date) getArguments().getSerializable(ARG_DATE);
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_date_picker, null);
        mDatePicker = (DatePicker) view.findViewById(R.id.date_picker_dialog);

        // Initialize the datePicker dialog.
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        mDatePicker.init(year, month, day, null);


        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mDatePicker.clearFocus();

                        int year = mDatePicker.getYear();
                        int month = mDatePicker.getMonth();
                        int day = mDatePicker.getDayOfMonth();

                        Date date = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
                        sendResult(Activity.RESULT_OK, date);
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, Date date) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        Intent i = new Intent();
        i.putExtra(EXTRA_DATE, date);
        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, i);
    }
}