package com.aston.tanion.schedule.model;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

/**
 * Created by Aston Tanion on 05/02/2016.
 */
public class TaskItem {

    private String mTitle = "";
    private String mLocation = "";
    private String mState = "";
    private boolean mIsAlarmStateChecked = false;
    private boolean mShouldRemindPriority = false;
    private boolean mShouldWakeUpCall = false;
    private int mTime = 0;
    private int mWakeUpCallTime = 0;
    private int mDeltaTime = 0;
    private int mAlarmTypeChoice = 0;
    private long mIntervalTime = 0;
    private Date mDueDate;
    private Date mCompleteDate;
    private UUID mUUID;

    public TaskItem() {
        this(UUID.randomUUID());
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        mDueDate = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
        mCompleteDate = new GregorianCalendar(year, month, day, 0, 0, 0).getTime();
    }

    public TaskItem(UUID id) {
        mUUID = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public String getLocation() {
        return mLocation;
    }

    public String getState() {
        return mState;
    }

    public void setState(String state) {
        mState = state;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public int getTime() {
        return mTime;
    }

    public void setTime(int time) {
        mTime = time;
    }

    public Date getDueDate() {
        return mDueDate;
    }

    public void setDueDate(Date dueDate) {
        mDueDate = dueDate;
    }

    public int getDeltaTime() {
        return mDeltaTime;
    }

    public void setDeltaTime(int deltaTime) {
        mDeltaTime = deltaTime;
    }

    public boolean getAlarmStateChecked() {
        return mIsAlarmStateChecked;
    }

    public void setAlarmStateChecked(boolean alarmStateChecked) {
        mIsAlarmStateChecked = alarmStateChecked;
    }

    public UUID getUUID() {
        return mUUID;
    }

    public int getAlarmTypeChoice() {
        return mAlarmTypeChoice;
    }

    public void setAlarmTypeChoice(int alarmTypeChoice) {
        mAlarmTypeChoice = alarmTypeChoice;
    }

    public Date getCompleteDate() {
        return mCompleteDate;
    }

    public void setCompleteDate(Date completeDate) {
        mCompleteDate = completeDate;
    }

    public long getIntervalTime() {
        return mIntervalTime;
    }

    public void setIntervalTime(long intervalTime) {
        mIntervalTime = intervalTime;
    }

    public boolean shouldRemindPriority() {
        return mShouldRemindPriority;
    }

    public void setShouldRemindPriority(boolean shouldRemindPriority) {
        mShouldRemindPriority = shouldRemindPriority;
    }

    public int getWakeUpCallTime() {
        return mWakeUpCallTime;
    }

    public void setWakeUpCallTime(int wakeUpCallTime) {
        mWakeUpCallTime = wakeUpCallTime;
    }

    public boolean shouldWakeUpCall() {
        return mShouldWakeUpCall;
    }

    public void setShouldWakeUpCall(boolean shouldWakeUpCall) {
        mShouldWakeUpCall = shouldWakeUpCall;
    }
}