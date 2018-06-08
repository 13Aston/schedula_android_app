package com.aston.tanion.schedule.model;

/**
 * Created by Aston Tanion on 20/03/2016.
 */
public enum State {
    ONGOING,
    COMPLETED;

    public String toString() {
        switch (this.ordinal()) {
            case 0:
                return "ONGOING";
            case 1:
                return "COMPLETED";
            default:
                return null;
        }
    }
}