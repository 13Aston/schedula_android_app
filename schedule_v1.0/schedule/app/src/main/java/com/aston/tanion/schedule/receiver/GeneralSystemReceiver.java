package com.aston.tanion.schedule.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.aston.tanion.schedule.service.DateChangeService;

/**
 * Created by Aston Tanion on 09/02/2016.
 */
public class GeneralSystemReceiver extends BroadcastReceiver {
    private static final String TAG = "GeneralSystemReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        DateChangeService.onWeekChange(context);
    }
}