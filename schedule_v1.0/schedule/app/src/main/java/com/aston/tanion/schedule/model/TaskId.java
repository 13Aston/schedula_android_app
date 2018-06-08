package com.aston.tanion.schedule.model;

import java.util.UUID;

/**
 * Created by Aston Tanion on 27/07/2016.
 */
public class TaskId {

    private UUID mUUID;
    private int[] mIds;

    public TaskId(UUID uuid, int[] ids) {
        mUUID = uuid;
        mIds = ids;
    }

    public UUID getUUID() {
        return mUUID;
    }

    public int[] getIds() {
        return mIds;
    }
}