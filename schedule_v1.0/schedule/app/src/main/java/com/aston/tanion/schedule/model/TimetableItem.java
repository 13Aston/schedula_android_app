package com.aston.tanion.schedule.model;

import java.util.UUID;

/**
 * Created by Aston Tanion on 05/02/2016.
 */
public class TimetableItem {

    private String mTitle = "";
    private String mLocation = "";
    private String mTimeFormat = "";
    private int mStartTime = 0;
    private int mEndTime = 0;
    private int mDeltaTime = 0;
    private int mAlarmTypeChoice = 0;
    private boolean mIsAlarmStartStateChecked = false;
    private boolean mIsAlarmEndStateChecked = false;
    private UUID mUUID;

    public TimetableItem() {
        this(UUID.randomUUID());
    }

    public TimetableItem(UUID id) {
        mUUID = id;
    }

    public int getDeltaTime() {
        return mDeltaTime;
    }

    public void setDeltaTime(int deltaTime) {
        mDeltaTime = deltaTime;
    }

    public int getStartTime() {
        return mStartTime;
    }

    public void setStartTime(int startTime) {
        mStartTime = startTime;
    }

    public int getEndTime() {
        return mEndTime;
    }

    public void setEndTime(int endTime) {
        mEndTime = endTime;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String subject) {
        mTitle = subject;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        mLocation = location;
    }

    public boolean getAlarmStartState() {
        return mIsAlarmStartStateChecked;
    }

    public void setAlarmStartState(boolean isAlarmChecked) {
        this.mIsAlarmStartStateChecked = isAlarmChecked;
    }

    public boolean getAlarmEndState() {
        return mIsAlarmEndStateChecked;
    }

    public void setAlarmEndState(boolean isAlarmChecked) {
        mIsAlarmEndStateChecked = isAlarmChecked;
    }

    public String getTimeFormat() {
        return mTimeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        mTimeFormat = timeFormat;
    }

    public int getAlarmTypeChoice() {
        return mAlarmTypeChoice;
    }

    public void setAlarmTypeChoice(int alarmTypeChoice) {
        mAlarmTypeChoice = alarmTypeChoice;
    }

    public UUID getUUID() {
        return mUUID;
    }
}