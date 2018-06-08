package com.aston.tanion.schedule.service;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.database.SharedPrefs;
import com.aston.tanion.schedule.database.WeekLab;
import com.aston.tanion.schedule.model.WeekItem;
import com.aston.tanion.schedule.utility.Constant;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by Aston Tanion on 21/02/2016.
 */
public class DateChangeService extends IntentService {
    private static final String TAG = "DateChangeService";
    // The number of millisecond in a week
    private static final int WEEK = 604800000;

    public static Intent newIntent(Context context) {
        return new Intent(context, DateChangeService.class);
    }

    public DateChangeService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        SharedPrefs mPrefs = SharedPrefs.get(this);
        // Last week position.
        int position;
        // Last week ID.
        String lastWeekId = (String) mPrefs.read(Constant.WEEK_CURRENT_ID_PREF, "");

        Calendar rightNow = Calendar.getInstance();
        int today = rightNow.get(Calendar.DAY_OF_WEEK);

        // Check if today is Monday. And if it is, change the week.
        if (today == Constant.MONDAY) {
            // Note that we set the default value of position to be 0 because
            // we define the default/first week to be at position 0.
            position = (int) mPrefs.read(Constant.WEEK_CURRENT_POSITION_PREF, 0);

            // Get the number of week available.
            int weekCount = WeekLab.get(this).getItems().size();

            // Return if there is not week.
            if (weekCount == 0) {
                // Note that this is never meant to run.
                return;
            }

            // This is the new week position, and allows the weeks to rotate
            position = (position + 1) % weekCount;

            // Load what will be the current(new) week
            WeekItem item = WeekLab.get(this).getItem(position);
            // Set up this week as the current.
            updateWeek(this, item);
        }

        // Initialize the alarms.
        AlarmService.TimetableAlarm.initAlarms(this, lastWeekId, false);

        // Calling this here will create a rotation between days.
        onDayChange(this);
    }

    public static void onDayChange(Context context) {

        Calendar rightNow = Calendar.getInstance();
        rightNow.set(Calendar.HOUR, 0);
        rightNow.set(Calendar.MINUTE, 0);
        rightNow.set(Calendar.SECOND, 0);
        rightNow.set(Calendar.MILLISECOND, 0);
        rightNow.set(Calendar.AM_PM, Calendar.AM);
        rightNow.add(Calendar.DAY_OF_MONTH, 1);

        PendingIntent pi = PendingIntent.getService(context, 0,
                DateChangeService.newIntent(context), PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, rightNow.getTimeInMillis(), pi);
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, rightNow.getTimeInMillis(), pi);
        }
    }

    public static void onWeekChange(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SharedPrefs mPrefs = SharedPrefs.get(context);

                // When there is an update of weeks, we save the date of the update
                // as the early date of that week (Monday). This means that if the
                // week was update on Friday, we will pretend that the week was saved
                // on Monday. This date is written as 09/08/2016 and save in the preferences
                String[] dateComponent = ((String) mPrefs
                        .read(Constant.WEEK_CURRENT_DATE_PREF, ""))
                        .trim()
                        .split(":");

                // The position of the week that we are changing from.
                int lastWeekPosition = (int) mPrefs.read(Constant.WEEK_CURRENT_POSITION_PREF, 0);

                // The date component of the last week (as explained above)
                int previousYear = 0;
                int previousMonth = 0;
                int previousDay = 0;
                try {
                    previousYear = Integer.parseInt(dateComponent[0].trim());
                    previousMonth = Integer.parseInt(dateComponent[1].trim());
                    previousDay = Integer.parseInt(dateComponent[2].trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }

                Calendar previousCalendar = Calendar.getInstance();
                previousCalendar.set(previousYear, previousMonth, previousDay);

                Calendar rightNow = Calendar.getInstance();

                Date previousDate = previousCalendar.getTime();
                Date currentDate = rightNow.getTime();


                // Get the number of weeks available.
                List<WeekItem> weeks = WeekLab.get(context).getItems();
                int weekCount = weeks.size();

                // Calculate how many weeks have passed between the previous date
                // and the current date.
                long diff = currentDate.getTime() - previousDate.getTime();
                int weekPassed = (int) (diff / WEEK);

                // Find what should be the current week position
                int newPosition;
                // This takes care of whether the user move the date forward or backward
                if (weekPassed < 0) {
                    // Since we are working backward, we should start counting from the position
                    // before the last position, which is lastWeekPosition - 1.
                    newPosition = (lastWeekPosition + weekPassed - 1) % weekCount;
                } else {
                    newPosition = (lastWeekPosition + weekPassed) % weekCount;
                }
                // Making sure that the newPosition is not negative
                // after we took the modulo operation.
                while (newPosition < 0) {
                    newPosition += weekCount;
                }

                // Request what will become the current week.
                WeekItem currentWeek = weeks.get(newPosition);

                // Request what was last week.
                WeekItem lastItem = weeks.get(lastWeekPosition);

                // Set the currentWeek as the current week
                updateWeek(context, currentWeek);
                // Initial all alarms.
                new AlarmService
                        .InitTimetableAlarmThread(context, lastItem.getUUID().toString(), true)
                        .start();

                DateChangeService.onDayChange(context);
            }
        }).start();
    }

    public static void updateWeek(Context context, WeekItem currentWeek) {
        SharedPrefs mPrefs = SharedPrefs.get(context);

        mPrefs.write(Constant.WEEK_CURRENT_ID_PREF, currentWeek.getUUID().toString());
        mPrefs.write(Constant.WEEK_CURRENT_POSITION_PREF, currentWeek.getPosition());
        mPrefs.write(Constant.WEEK_CURRENT_NAME_PREF, currentWeek.getTitle());

        Calendar cal = Calendar.getInstance();
        int day = cal.get(Calendar.DAY_OF_MONTH);
        int month = cal.get(Calendar.MONTH);
        int year = cal.get(Calendar.YEAR);
        int today = cal.get(Calendar.DAY_OF_WEEK);
        // Since our week start on monday, we should always
        // set this day as MONDAY even if the user has change the
        // week on Friday or other days.
        // So deltaDay is the increment form monday to the day the
        // user has change the week.
        // We define Sunday to have a value of 8 because we consider
        // Sunday to be the last day of the week and Monday and the difference
        // between MONDAY = 2 and SUNDAY = 8 is kept as 6 as if we
        // have use the common way (MONDAY = 1 and SUNDAY = 7).
        int deltaDay = ((today == 1) ? 8 : today) - Constant.MONDAY;
        // Write today's date in the shared preference
        // This will be used to workout how many weeks have passed if the device have
        // been off or if the user have changed the date configuration.
        mPrefs.write(Constant.WEEK_CURRENT_DATE_PREF,
                String.format(context.getResources()
                        .getString(R.string.week_date_pref_format),
                        year, month, day - deltaDay));
    }
}