package com.aston.tanion.schedule.service;


import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.aston.tanion.schedule.R;
import com.aston.tanion.schedule.activity.NavigationDrawerActivity;
import com.aston.tanion.schedule.database.RandomId;
import com.aston.tanion.schedule.database.SharedPrefs;
import com.aston.tanion.schedule.database.TaskMap;
import com.aston.tanion.schedule.database.TimetableLab;
import com.aston.tanion.schedule.database.TimetableMap;
import com.aston.tanion.schedule.model.Day;
import com.aston.tanion.schedule.model.Notification;
import com.aston.tanion.schedule.model.TaskItem;
import com.aston.tanion.schedule.model.TimetableItem;
import com.aston.tanion.schedule.utility.CommonMethod;
import com.aston.tanion.schedule.utility.Constant;
import com.aston.tanion.schedule.utility.ItemNotification;

import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Created by Aston Tanion on 14/02/2016.
 */

public class AlarmService extends IntentService {
    private static final String TAG = "AlarmService";

    private static final String EXTRA_ALARM_TYPE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.TYPE";
    private static final String EXTRA_NOTIFICATION_ID =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.ID";
    private static final String EXTRA_DELTA_TIME =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.DELTA_TIME";
    private static final String EXTRA_TITLE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.EXTRA_TITLE";
    private static final String EXTRA_LOCATION =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.EXTRA_LOCATION";
    private static final String EXTRA_WHICH_ALARM =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.WHICH_TIME";
    private static final String EXTRA_IDENTIFIER =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.IDENTIFIER";
    private static final String EXTRA_PATH =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.PATH";
    private static final String EXTRA_PRIORITY =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.PRIORITY_STATE";
    private static final String EXTRA_WAKE_UP_STATE =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.WAKE_UP_STATE";
    private static final String EXTRA_WAKE_UP_TIME =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.EXTRA_WAKE_UP_TIME";
    private static final String EXTRA_CALENDAR =
            "com.tanion.aston.rovery.com.aston.tanion.schedule.service.EXTRA_CALENDAR";

    public static Intent newIntent(Context context) {
        return new Intent(context, AlarmService.class);
    }

    public AlarmService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Calendar rightNow = Calendar.getInstance();
        rightNow.set(Calendar.SECOND, 0);
        rightNow.set(Calendar.MILLISECOND, 0);

        // Get the alarm com.aston.tanion.schedule calendar.
        Calendar alarmCalendar = (Calendar) intent.getSerializableExtra(EXTRA_CALENDAR);

        if (alarmCalendar != null && alarmCalendar.before(rightNow)) {
            return;
        }


        String title = intent.getStringExtra(EXTRA_TITLE);
        String location = intent.getStringExtra(EXTRA_LOCATION);
        int identifier = intent.getIntExtra(EXTRA_IDENTIFIER, 0);
        int alarmType = intent.getIntExtra(EXTRA_ALARM_TYPE, 0);
        int before = intent.getIntExtra(EXTRA_DELTA_TIME, 0);

        Intent i = NavigationDrawerActivity.newIntent(this);
        PendingIntent intentLauncher = PendingIntent.getActivity(this, 0, i, 0);

        Notification notification = Notification.get(this)
                .createNotification(intentLauncher)
                .setType(alarmType);

        if (identifier == Constant.IDENTIFIER_TIMETABLE) {
            int whichAlarm = intent.getIntExtra(EXTRA_WHICH_ALARM, 0);

            notification
                    .addTicker(ItemNotification.getTicker(
                            this, Constant.IDENTIFIER_TIMETABLE))
                    .addTitle(ItemNotification.getTitle(
                            this, Constant.IDENTIFIER_TIMETABLE, title))
                    .addContentText(ItemNotification.getContentText(
                            this, before, whichAlarm, identifier, title, location))
                    .addStyle(ItemNotification.getContentText(
                            this, before, whichAlarm, identifier, title, location))
                    .addGroup(Constant.GROUP_KEY_SCHEDULE)
                    .ShowNotification(Constant.IDENTIFIER_TIMETABLE);

        } else if (identifier == Constant.IDENTIFIER_TASK) {
            int notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 0);
            int priority = intent.getIntExtra(EXTRA_PRIORITY, -1);
            boolean shouldWakeUpCall = intent.getBooleanExtra(EXTRA_WAKE_UP_STATE, false);

            String path = intent.getStringExtra(EXTRA_PATH);
            String message;

            notification
                    .addTicker(ItemNotification.getTicker(
                            this, Constant.IDENTIFIER_TASK))
                    .addTitle(ItemNotification.getTitle(
                            this, Constant.IDENTIFIER_TASK, title));

            if (priority != -1) {
                int priorityCode = (priority == 0)?
                        R.string.notification_priority_amber : R.string.notification_priority_red;

                String content = String.format(getResources().getString(
                        R.string.notification_priority_change),
                        getResources().getString(priorityCode));

                message = content
                        + "\n\n" + getResources().getString(
                        R.string.notification_alarm_detail_message)
                        + "\n" + ItemNotification.getDetailText(this, path);

                notification
                        .addContentText(content);

            } else if (shouldWakeUpCall) {
                String wakeUpTime = intent.getStringExtra(EXTRA_WAKE_UP_TIME);
                String content = String.format(getResources().getString(
                        R.string.notification_wake_up_call),
                        wakeUpTime);

                message = content
                        + "\n\n" + getResources().getString(
                        R.string.notification_alarm_detail_message)
                        + "\n" + ItemNotification.getDetailText(this, path);

                notification.addContentText(content);

            } else {
                message = ItemNotification.
                        getContentText(this, before, Constant.ALARM_START, identifier, title, location)
                        + "\n\n" + getResources().getString(R.string.notification_alarm_detail_message)
                        + "\n" + ItemNotification.getDetailText(this, path);

                notification
                        .addContentText(ItemNotification.getContentText(
                                this, before, Constant.ALARM_START, identifier, title, location));
            }

            notification
                    .addStyle(message)
                    .addGroup(Constant.GROUP_KEY_SCHEDULE)
                    .ShowNotification(notificationId);
        }
    }

    // Set up the AlarmManager.
    private static void setAlarm(Context context, PendingIntent pi, Long time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pi
            );
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    time,
                    pi
            );
        }
    }

    public static class TimetableAlarm {

        /**
         * This will run every mid-night
         * Remove every alarm that was set the day before
         * Replace them with the current day alarm
         * @param lastWeek a unique id (UUID) for the previous week if the weeks have rotated.
         *                 If the weeks haven't rotated, lastWeek is the same as the current week id
         * @param hasUserChangeWeek defines whether it is the user or the system
         *                          who has changed the week
         * */
        public static void initAlarms(Context context, String lastWeek, boolean hasUserChangeWeek) {

            Calendar rightNow = Calendar.getInstance();
            int today = rightNow.get(Calendar.DAY_OF_WEEK);

            // This is used to cancel all yesterday's alarm in case where the user has
            // changed the week.
            int yesterday;

            // If the user has change the week, then the day has not changed.
            if (hasUserChangeWeek) {
                yesterday = today;
            } else {
                if (today == 1) yesterday = 7;
                else yesterday = today - 1;
            }

            TimetableLab timetableLab = TimetableLab.get(context);

            // Retrieve yesterday's events.
            String lastDay = today(yesterday);
            List<TimetableItem> yesterdayItems = timetableLab.getItems(lastDay, lastWeek);

            // Remove all alarm bounded to yesterday's events.
            for (int i = 0; i < yesterdayItems.size(); i ++) {
                TimetableItem item = yesterdayItems.get(i);
                removeAlarm(context, item);
            }

            // Retrieve today's events.
            String currentDay = today(today);
            String thisWeek = (String) SharedPrefs.get(context)
                    .read(Constant.WEEK_CURRENT_ID_PREF, "");
            List<TimetableItem> todayEvents = timetableLab.getItems(currentDay, thisWeek);

            // Replace all yesterday's alarms with today's.
            for (int i = 0; i < todayEvents.size(); i ++) {
                TimetableItem event = todayEvents.get(i);

                // Add each start alarm
                if (event.getAlarmStartState()) {
                    addAlarm(context, event, Constant.ALARM_START, currentDay);
                }

                // Add each end alarm
                if (event.getAlarmEndState()) {
                    addAlarm(context, event, Constant.ALARM_END, currentDay);
                }
            }
        }

        /**
         * @param whichAlarm an integer which represent either the start or end of an event's alarm.
         * Its values are either 0 for start alarm or 1 for end alarm.
         * */
        public static void addAlarm(
                Context context, TimetableItem event, int whichAlarm, String day) {

            // This make sure to add today's alarm(s) only.
            if (!day.equals(today(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))) ||
                    event == null) return;

            UUID uuid = event.getUUID();

            int startTimeId = 0;
            int endTimeId = 0;
            int startBeforeTimeId = 0;
            int endBeforeTimeId = 0;

            TimetableMap map = TimetableMap.get(context);
            RandomId randomId = RandomId.get(context);

            // Note: timetableMap is a function that maps
            // f: uuid -> {start id, end id, start before id, end before id}.
            ConcurrentHashMap<UUID, int[]> timetableMap = map.getMap();

            // A collection of all the id for each alarm that have been used.
            // We use this to create a random ids generator.
            List<Integer> idList = RandomId.get(context).getID();

            // Initialise idList with 0, which will never be user as and id.
            if (!idList.contains(0)) {
                randomId.addId(0);
                // Update the list
                idList = randomId.getID();
            }

            // Create random id for each alarm to be used in the PendingIntent as an identifier.
            if (!timetableMap.containsKey(uuid)) {
                // Generate an id for the "start time".
                while (idList.contains(startTimeId) && idList.size() < Constant.ALARM_LIMIT) {
                    startTimeId = new Random().nextInt(Constant.ALARM_LIMIT);
                }

                idList.add(startTimeId);

                // Generate an id for "before start time"
                while (idList.contains(startBeforeTimeId) && idList.size() < Constant.ALARM_LIMIT) {
                    startBeforeTimeId = new Random().nextInt(Constant.ALARM_LIMIT);
                }

                idList.add(startBeforeTimeId);

                // Generate an id for "end time"
                while (idList.contains(endTimeId) && idList.size() < Constant.ALARM_LIMIT) {
                    endTimeId = new Random().nextInt(Constant.ALARM_LIMIT);
                }

                idList.add(endTimeId);

                // Generate an id for "before end time"
                while (idList.contains(endBeforeTimeId) && idList.size() < Constant.ALARM_LIMIT) {
                    endBeforeTimeId = new Random().nextInt(Constant.ALARM_LIMIT);
                }

                idList.add(endBeforeTimeId);
            }
            // If these ids already exist, use the existing ones.
            else if (timetableMap.containsKey(uuid)) {
                startTimeId = timetableMap.get(uuid)[whichAlarm];
                startBeforeTimeId = timetableMap.get(uuid)[whichAlarm + 2];
                endTimeId = timetableMap.get(uuid)[whichAlarm];
                endBeforeTimeId = timetableMap.get(uuid)[whichAlarm + 2];
            }

            if (idList.size() < Constant.ALARM_LIMIT) {

                // Check if this event doesn't already exist.
                if (!timetableMap.containsKey(uuid)) {
                    // Store all the id create in randomId database.
                    randomId.addId(startTimeId);
                    randomId.addId(endTimeId);
                    randomId.addId(startBeforeTimeId);
                    randomId.addId(endBeforeTimeId);

                    // Note: timetableMap is a function that maps
                    // f: uuid -> {start id, end id, start before id, end before id}
                    // Store this map in timetable map database
                    map.put(uuid,
                            new int[]{startTimeId, endTimeId, startBeforeTimeId, endBeforeTimeId});
                }

                // Update the map
                timetableMap = map.getMap();

                // A delta time that need to be subtract from the actual time
                // in order to create early reminder
                int deltaTime = event.getDeltaTime();

                int hour;
                int minute;
                int deltaHour = 0;
                int deltaMinute = 0;

                if (whichAlarm == Constant.ALARM_START) {
                    hour = event.getStartTime() / 60;
                    minute = event.getStartTime() % 60;

                    if (deltaTime > 0) {
                        deltaHour = (event.getStartTime() - deltaTime) / 60;
                        deltaMinute = (event.getStartTime() - deltaTime) % 60;
                    }

                } else if (whichAlarm == Constant.ALARM_END){
                    hour = event.getEndTime() / 60;
                    minute = event.getEndTime() % 60;

                    if (deltaTime > 0) {
                        deltaHour = (event.getEndTime() - deltaTime) / 60;
                        deltaMinute = (event.getEndTime() - deltaTime) % 60;
                    }

                    if (event.getStartTime() > event.getEndTime() - event.getDeltaTime()) {
                        deltaHour = event.getStartTime() / 60;
                        deltaMinute = (event.getStartTime() % 60) + 1;
                        deltaTime = event.getEndTime() - (deltaHour * 60 + deltaMinute);
                    }

                } else {
                    return;
                }

                // Current time.
                Calendar rightNow = Calendar.getInstance();
                rightNow.set(Calendar.SECOND, 0);
                rightNow.set(Calendar.MILLISECOND, 0);

                int currentTime = rightNow.get(Calendar.HOUR_OF_DAY) * 60
                        + rightNow.get(Calendar.MINUTE);

                // Check if the alarm is set in the past. And if it is, don't com.aston.tanion.schedule this alarm.
                if (hour * 60 + minute < currentTime) return;

                // Setting up actual alarm time.
                Calendar calendarAlarmSet = Calendar.getInstance();
                calendarAlarmSet.set(Calendar.HOUR_OF_DAY, hour);
                calendarAlarmSet.set(Calendar.MINUTE, minute);
                calendarAlarmSet.set(Calendar.SECOND, 0);
                calendarAlarmSet.set(Calendar.MILLISECOND, 0);

                // Get the identifier that will be used in the PendingIntent
                // from a map created above.
                int identifier = timetableMap.get(uuid)[whichAlarm];

                Intent intent = AlarmService.newIntent(context);
                intent.putExtra(EXTRA_ALARM_TYPE, event.getAlarmTypeChoice());
                intent.putExtra(EXTRA_TITLE, event.getTitle());
                intent.putExtra(EXTRA_LOCATION, event.getLocation());
                intent.putExtra(EXTRA_WHICH_ALARM, whichAlarm);
                intent.putExtra(EXTRA_IDENTIFIER, Constant.IDENTIFIER_TIMETABLE);
                intent.putExtra(EXTRA_CALENDAR, calendarAlarmSet);

                PendingIntent pi = PendingIntent.getService(context,
                        identifier, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                // Set up the alarm
                setAlarm(context, pi, calendarAlarmSet.getTimeInMillis());

                if (deltaTime > 0) {
                    // Setting the early reminder time.
                    Calendar deltaCalendar = Calendar.getInstance();
                    deltaCalendar.set(Calendar.HOUR_OF_DAY, deltaHour);
                    deltaCalendar.set(Calendar.MINUTE, deltaMinute);
                    deltaCalendar.set(Calendar.SECOND, 0);
                    deltaCalendar.set(Calendar.MILLISECOND, 0);

                    // Adjust the before time
                    if (deltaCalendar.before(rightNow)) {
                        if (whichAlarm == Constant.ALARM_START) {
                            deltaTime = event.getStartTime()- currentTime;
                        } else {
                            deltaTime = event.getEndTime()- currentTime;
                        }
                    }

                    int identifierBefore = timetableMap.get(uuid)[whichAlarm + 2];

                    intent.putExtra(EXTRA_DELTA_TIME, deltaTime);
                    intent.putExtra(EXTRA_CALENDAR, deltaCalendar);

                    PendingIntent piDelta = PendingIntent.getService(context,
                            identifierBefore, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                    // Set up the alarm
                    setAlarm(context, piDelta, deltaCalendar.getTimeInMillis());
                }
            }
        }

        /**
         * This method update a event by:
         * 1) Cancels all of its alarm, but keep those alarm ids
         * 2) Recreate the alarm using the same ids
         * This happens when the user update the event.
         * @param whichAlarm an integer which represent either the start or end of an event's alarm.
         * Its values are either 0 for start alarm or 1 for end alarm.
         * */
        public static void updateAlarm(
                Context context, TimetableItem event, int whichAlarm, String day) {

            // This makes sure to update today's alarm only.
            if (!day.equals(today(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))) ||
                    event == null) return;

            // Clean all alarm.
            clearAlarm(context, event, whichAlarm, day);
            // Recreate the alarm.
            addAlarm(context, event, whichAlarm, day);
        }

        /**
         * This method cancel all alarms but keep their ids.
         * This happens when the user toggle any alarm switch and update the event.
         * @param whichAlarm an integer which represent either the start or end of an event's alarm.
         * Its values are either 0 for start alarm or 1 for end alarm.
         * */
        public static void clearAlarm(
                Context context, TimetableItem event, int whichAlarm, String day) {

            // Note: timetableMap is a function that maps
            // f: uuid -> {start id, end id, start before id, end before id}
            ConcurrentHashMap<UUID, int[]> timetableMap = TimetableMap.get(context).getMap();

            // This makes sure to clear today's alarm only.
            if (!day.equals(today(Calendar.getInstance().get(Calendar.DAY_OF_WEEK))) ||
                    event == null) return;

            UUID uuid = event.getUUID();

            // Check whether this event has alarms bounded to it.
            if (!timetableMap.containsKey(uuid)) return;

            int id = timetableMap.get(uuid)[whichAlarm];

            Intent intent = AlarmService.newIntent(context);
            PendingIntent pi = PendingIntent.getService(context,
                    id, intent, PendingIntent.FLAG_NO_CREATE);

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

            // Cancel the all with the same pending intent as above.
            if (pi != null) {
                alarmManager.cancel(pi);
            }

            int idBefore = timetableMap.get(uuid)[whichAlarm + 2];
            PendingIntent piBefore = PendingIntent.getService(context,
                    idBefore, intent, PendingIntent.FLAG_NO_CREATE);

            // Cancel the all with the same pending intent as above.
            if (pi != null) {
                alarmManager.cancel(piBefore);
            }
        }

        /** This method cancel all alarm bounded to an event and remove all of the alarm id.
         * This happens when the user delete and event.
         * */
        public static void removeAlarm(Context context, TimetableItem event){

            UUID uuid = event.getUUID();

            TimetableMap map = TimetableMap.get(context);
            RandomId randomId = RandomId.get(context);

            // Note: timetableMap is a function that maps
            // f: uuid -> {start id, end id, start before id, end before id}
            ConcurrentHashMap<UUID, int[]> timetableMap = map.getMap();

            // Check whether this event has alarms bounded to it.
            if (!timetableMap.containsKey(uuid)) return;

            Intent intent = AlarmService.newIntent(context);

            for (int i = 0; i < timetableMap.get(uuid).length; i++) {
                int id = timetableMap.get(uuid)[i];

                PendingIntent pi = PendingIntent
                        .getService(context, id, intent, PendingIntent.FLAG_NO_CREATE);

                // Remove the start id from sID;
                if (randomId.getID().contains(id)) {
                    // Remove this id from randomId database
                    randomId.remove(id);
                }

                // Cancel the alarm with the same pending intent (piStart) as above.
                if (pi != null) {
                    ((AlarmManager) context.getSystemService(ALARM_SERVICE)).cancel(pi);
                }
            }

            // Remove this item from TimetableMap database.
            map.remove(uuid);
        }

        // Get the current day.
        private static String today(int day) {
            Day days[] = Day.values();
            switch (day) {
                case 1:
                    return days[6].toString();
                case 2:
                    return days[0].toString();
                case 3:
                    return days[1].toString();
                case 4:
                    return days[2].toString();
                case 5:
                    return days[3].toString();
                case 6:
                    return days[4].toString();
                case 7:
                    return days[5].toString();
                default:
                    return days[day % 7].toString();
            }
        }
    }

    public static class TaskAlarm {

        public static void addAlarm(Context context, TaskItem task) {
            if (task == null) return;

            UUID uuid = task.getUUID();
            int deltaTime = task.getDeltaTime();

            int id = 0;
            int idDelta = 0;
            int idAmberPriority = 0;
            int idRedPriority = 0;
            int idWakeUpCall = 0;

            TaskMap map = TaskMap.get(context);
            RandomId randomId = RandomId.get(context);

            // Note: sTaskMap is a function that maps
            // f: uuid -> {id, idDelta, idAmberPriority, idRedPriority, idWakeUpCall}.
            ConcurrentHashMap<UUID, int[]> taskMap = map.getMap();

            // A collection of all the id for each alarm that have been used.
            // We use this to create a random ids generator.
            List<Integer> idList = randomId.getID();

            // Init idList with 0, which will never be user as and id.
            if (!idList.contains(0)) {
                randomId.addId(0);
                // Update the id
                idList = randomId.getID();
            }

            // Create random id for each alarm to use in the PendingIntent as and identification.
            if (!taskMap.containsKey(uuid)) {
                while (idList.contains(id) && idList.size() < Constant.ALARM_LIMIT) {
                    id = new Random().nextInt(Constant.ALARM_LIMIT);
                }

                idList.add(id);

                while (idList.contains(idDelta) && idList.size() < Constant.ALARM_LIMIT) {
                    idDelta = new Random().nextInt(Constant.ALARM_LIMIT);
                }

                idList.add(idDelta);

                while (idList.contains(idAmberPriority) && idList.size() < Constant.ALARM_LIMIT) {
                    idAmberPriority = new Random().nextInt(Constant.ALARM_LIMIT);
                }

                idList.add(idAmberPriority);

                while (idList.contains(idRedPriority) && idList.size() < Constant.ALARM_LIMIT) {
                    idRedPriority = new Random().nextInt(Constant.ALARM_LIMIT);
                }

                idList.add(idRedPriority);

                while (idList.contains(idWakeUpCall) && idList.size() < Constant.ALARM_LIMIT) {
                    idWakeUpCall = new Random().nextInt(Constant.ALARM_LIMIT);
                }

                idList.add(idWakeUpCall);
            }
            // If these ids already exist, use the existing ones.
            else if (taskMap.containsKey(uuid)) {
                id = taskMap.get(uuid)[0];
                idDelta = taskMap.get(uuid)[1];
                idAmberPriority = taskMap.get(uuid)[2];
                idRedPriority = taskMap.get(uuid)[3];
                idWakeUpCall = taskMap.get(uuid)[4];
            }

            if (idList.size() < Constant.ALARM_LIMIT) {
                // Save the created id and create a map
                // f: uuid --> {id, idDelta, idAmberPriority, idRedPriority).
                if (!taskMap.containsKey(uuid)) {
                    // Store all the id create in the randomId database.
                    randomId.addId(id);
                    randomId.addId(idDelta);
                    randomId.addId(idAmberPriority);
                    randomId.addId(idRedPriority);
                    randomId.addId(idWakeUpCall);

                    // Note: sTaskMap is a function that maps
                    // f: uuid -> {id, idDelta, idAmberPriority, idRedPriority, idWakeUpCall}
                    // Store this map in task map database.
                    map.put(uuid,
                            new int[]{id, idDelta, idAmberPriority, idRedPriority, idWakeUpCall});
                }

                // Get the actual time which the alarm will go off.
                int hour = task.getTime() / 60;
                int minute = task.getTime() % 60;

                // Create the current calendar
                Calendar rightNow = Calendar.getInstance();
                int currentTime = rightNow.get(Calendar.HOUR_OF_DAY) * 60
                        + rightNow.get(Calendar.MINUTE);

                // Create the actual time calender which the alarm will go off.
                Calendar calendarAlarmSet = Calendar.getInstance();
                calendarAlarmSet.setTime(task.getDueDate());
                calendarAlarmSet.set(Calendar.HOUR_OF_DAY, hour);
                calendarAlarmSet.set(Calendar.MINUTE, minute);
                calendarAlarmSet.set(Calendar.SECOND, 0);
                calendarAlarmSet.set(Calendar.MILLISECOND, 0);

                // Check is this due date have passed
                if (calendarAlarmSet.before(rightNow)) return;

                // This intent sends information that will be used during for the notification.
                Intent intent = AlarmService.newIntent(context);
                intent.putExtra(EXTRA_IDENTIFIER, Constant.IDENTIFIER_TASK);
                intent.putExtra(EXTRA_TITLE, task.getTitle());
                intent.putExtra(EXTRA_LOCATION, task.getLocation());
                intent.putExtra(EXTRA_ALARM_TYPE, task.getAlarmTypeChoice());
                intent.putExtra(EXTRA_PATH, uuid.toString());
                intent.putExtra(EXTRA_NOTIFICATION_ID, id);
                intent.putExtra(EXTRA_CALENDAR, calendarAlarmSet);

                PendingIntent pi = PendingIntent.getService(context,
                        id, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                // Set the alarm
                setAlarm(context, pi, calendarAlarmSet.getTimeInMillis());

                if (deltaTime > 0) {
                    // Get the early time which the alarm should go off.
                    int hourBefore = (task.getTime() - deltaTime) / 60;
                    int minuteBefore = (task.getTime() - deltaTime) % 60;

                    // Create a time calendar which the alarm should go off as a early reminder.
                    Calendar deltaCalendar = Calendar.getInstance();
                    deltaCalendar.setTime(task.getDueDate());
                    deltaCalendar.set(Calendar.HOUR_OF_DAY, hourBefore);
                    deltaCalendar.set(Calendar.MINUTE, minuteBefore);
                    deltaCalendar.set(Calendar.SECOND, 0);
                    deltaCalendar.set(Calendar.MILLISECOND, 0);

                    // Adjust the before time
                    if (deltaCalendar.before(rightNow)) {
                        deltaTime = task.getTime() - currentTime;
                    }

                    intent.putExtra(EXTRA_DELTA_TIME, deltaTime);
                    intent.putExtra(EXTRA_CALENDAR, deltaCalendar);

                    PendingIntent piBefore = PendingIntent.getService(context,
                            idDelta, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                    // Set the alarm
                    setAlarm(context, piBefore, deltaCalendar.getTimeInMillis());
                }

                // Create a time calendar which the priority will go off.
                if (task.shouldRemindPriority()) {
                    Calendar priorityCalendar = Calendar.getInstance();
                    priorityCalendar.add(Calendar.MILLISECOND, (int) task.getIntervalTime());

                    intent.removeExtra(EXTRA_DELTA_TIME);
                    intent.putExtra(EXTRA_PRIORITY, Constant.PRIORITY_AMBER);
                    intent.putExtra(EXTRA_CALENDAR, priorityCalendar);

                    PendingIntent piAmberPriority = PendingIntent.getService(context,
                            idAmberPriority, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                    // Set the alarm
                    setAlarm(context, piAmberPriority, priorityCalendar.getTimeInMillis());

                    // This add two time the interval time to the calendar.
                    priorityCalendar.add(Calendar.MILLISECOND, (int) task.getIntervalTime());
                    intent.putExtra(EXTRA_PRIORITY, Constant.PRIORITY_RED);
                    intent.putExtra(EXTRA_CALENDAR, priorityCalendar);

                    PendingIntent piRedPriority = PendingIntent.getService(context,
                            idRedPriority, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                    // Set the alarm
                    setAlarm(context, piRedPriority, priorityCalendar.getTimeInMillis());
                }

                // Create a wake up call calendar
                if (task.shouldWakeUpCall()) {
                    int wakeUpHour = task.getWakeUpCallTime() / 60;
                    int wakeUpMinute = task.getWakeUpCallTime() % 60;

                    Calendar wakeUpCalendar = Calendar.getInstance();
                    wakeUpCalendar.setTime(task.getDueDate());
                    wakeUpCalendar.set(Calendar.HOUR_OF_DAY, wakeUpHour);
                    wakeUpCalendar.set(Calendar.MINUTE, wakeUpMinute);
                    wakeUpCalendar.set(Calendar.SECOND, 0);
                    wakeUpCalendar.set(Calendar.MILLISECOND, 0);

                    String message = CommonMethod.timeStringFormat(task.getTime());

                    intent.removeExtra(EXTRA_PRIORITY);
                    intent.putExtra(EXTRA_WAKE_UP_STATE, true);
                    intent.putExtra(EXTRA_WAKE_UP_TIME, message);
                    intent.putExtra(EXTRA_CALENDAR, wakeUpCalendar);

                    PendingIntent piWakeUp = PendingIntent.getService(context,
                            idWakeUpCall, intent, PendingIntent.FLAG_CANCEL_CURRENT);

                    // Set the alarm
                    setAlarm(context, piWakeUp, wakeUpCalendar.getTimeInMillis());
                }
            }
        }

        /** This method cancel all alarm bounded to an event and remove all of the alarm id.
         * This happens when the user delete and task.
         * */
        public static void removeAlarm(Context context, TaskItem task) {
            if (task == null) return;

            UUID uuid = task.getUUID();

            TaskMap map = TaskMap.get(context);
            RandomId randomId = RandomId.get(context);

            // Note: sTaskMap is a function that maps
            // f: uuid -> {id, idBefore, idAmberPriority, idRedPriority, idWakeUpCall}
            ConcurrentHashMap<UUID, int[]> taskMap = map.getMap();

            // Check whether this item has alarms bounded to it.
            if (!taskMap.containsKey(uuid)) return;

            Intent intent = AlarmService.newIntent(context);

            for (int i = 0; i < taskMap.get(uuid).length; i++) {
                int id = taskMap.get(uuid)[i];

                PendingIntent pi = PendingIntent
                        .getService(context, id, intent, PendingIntent.FLAG_NO_CREATE);

                // Remove the id from sID.
                if (randomId.getID().contains(id)) {
                    // Remove this id from randomId database.
                    randomId.remove(id);
                }

                // Cancel the alarm with the same pending intent as pi
                if (pi != null) {
                    ((AlarmManager) context.getSystemService(ALARM_SERVICE)).cancel(pi);
                }
            }

            // Remove this item from TaskMap database.
            map.remove(uuid);
        }
    }

    public static class InitTimetableAlarmThread extends Thread {
        private Context mContext;
        private String mLastWeek;
        boolean mHasUserChangedWeek;

        /**
         * @param lastWeek a unique id (UUID) for the previous week if the weeks have rotated.
         * If the weeks haven't rotated, lastWeek is the same as the current week id
         * */
        public InitTimetableAlarmThread(
                Context context, String lastWeek, boolean hasUserChangedWeek) {
            mContext = context;
            mLastWeek = lastWeek;
            mHasUserChangedWeek = hasUserChangedWeek;
        }

        @Override
        public void run() {
            TimetableAlarm.initAlarms(mContext, mLastWeek, mHasUserChangedWeek);
        }
    }
}