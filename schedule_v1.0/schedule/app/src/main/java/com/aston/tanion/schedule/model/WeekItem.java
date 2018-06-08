package com.aston.tanion.schedule.model;

import java.util.UUID;

/**
 * Created by Aston Tanion on 13/03/2016.
 */
public class WeekItem {

    private UUID mUUID;
    private String mTitle;
    private int Position;

    public WeekItem() {
        this(UUID.randomUUID());
    }

    public void setPosition(int position) {
        Position = position;
    }

    public int getPosition() {
        return Position;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public WeekItem(UUID id) {
        mUUID = id;
    }

    public UUID getUUID() {
        return mUUID;
    }
}