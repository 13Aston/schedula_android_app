package com.aston.tanion.schedule.utility;

/**
 * Created by Aston Tanion on 14/03/2016.
 */
public class Constant {

    //========================================
    // Identifier constant
    //========================================

    public static final int IDENTIFIER_TIMETABLE = 0;
    public static final int IDENTIFIER_TASK = 1;
    public static final int IDENTIFIER_WEEK = 2;

    //========================================
    // Request code constant
    //========================================

    public static final int REQUEST_TIME_PICKER_DIALOG = 0;
    public static final int REQUEST_DATE_PICKER_DIALOG = 1;
    public static final int REQUEST_SETTINGS_ACTIVITY = 3;
    public static final int REQUEST_WEEK_PREVIEW_DIALOG = 4;
    public static final int REQUEST_DESTROY_DETAIL = 5;
    public static final int REQUEST_ITEM_CREATE = 6;
    public static final int REQUEST_ITEM_UPDATE = 7;

    //========================================
    // Database constants
    //========================================

    // Database operation constant
    public static final int DATABASE_ADD_ITEM = 0;
    public static final int DATABASE_REMOVE_ITEM = 1;
    public static final int DATABASE_UPDATE_ITEM = 2;
    public static final int DATABASE_GET_ITEM = 3;
    public static final int DATABASE_GET_ITEMS = 4;

    //========================================
    // Shared Preference constant
    //========================================

    public static final String USER_FIRST_VISIT_PREF = "USER_FIRST_TIME"; // boolean
    public static final String WEEK_CURRENT_ID_PREF = "WEEK_CURRENT_ID_PREF"; // String
    public static final String WEEK_CURRENT_NAME_PREF = "WEEK_CURRENT_NAME_PREF"; // String
    public static final String WEEK_CURRENT_POSITION_PREF = "WEEK_CURRENT_POSITION_PREF"; //int
    public static final String WEEK_CURRENT_DATE_PREF = "WEEK_CURRENT_DATE_PREF"; // String
    // Settings preference
    public static final String WEEK_PREF = "WEEK_PREF";
    public static final String PRIORITY_PREF = "PRIORITY_PREF";
    public static final String WAKE_UP_CALL_PREF = "WAKE_UP_CALL_PREF";
    public static final String WAKE_UP_CALL_SWITCH_PREF = "WAKE_UP_CALL_SWITCH_PREF";
    public static final String WAKE_UP_CALL_EDIT_PREF = "WAKE_UP_CALL_EDIT_PREF";
    public static final String SOUND_PREF = "SOUND_PREF";
    public static final String VIBRATOR_PREF = "VIBRATOR_PREF";
    public static final String ALARM_PREF = "ALARM_PREF";
    public static final String REMINDER_PREF = "REMINDER_PREF";

    //========================================
    // Alarm constant
    //========================================

    // Timetable alarm constant
    public static final int ALARM_START = 0; // do not modify this value
    public static final int ALARM_END = 1; // do not modify this value
    // The maximum number of alarms that can be created a day.
    public static final int ALARM_LIMIT = 2000;
    // Alarm operation CONSTANT
    public static final int ALARM_ADD = 0;
    public static final int ALARM_UPDATE = 1;
    public static final int ALARM_REMOVE = 2;
    public static final int ALARM_CLEAR = 3;

    //========================================
    // Notification constant
    //========================================

    // Notification choice
    public static final int NOTIFICATION_DEFAULT = 0;
    public static final int NOTIFICATION_SOUND = 1;
    public static final int NOTIFICATION_VIBRATOR = 2;
    // Notification group key
    public static final String GROUP_KEY_SCHEDULE = "GROUP_KEY_SCHEDULE";
    // Notification priority.
    public static final int PRIORITY_AMBER = 0;
    public static final int PRIORITY_RED = 1;

    //========================================
    // Day constant
    //========================================
    public static final int MONDAY = 2;
}