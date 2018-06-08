package com.aston.tanion.schedule.model;

/**
 * Created by Aston Tanion on 07/02/2016.
 */
public enum Day {
    MONDAY,
    TUESDAY,
    WEDNESDAY,
    THURSDAY,
    FRIDAY,
    SATURDAY,
    SUNDAY;

    public String toString() {
        switch (this.ordinal()) {
            case 0:
                return "MONDAY";
            case 1:
                return "TUESDAY";
            case 2:
                return "WEDNESDAY";
            case 3:
                return "THURSDAY";
            case 4:
                return "FRIDAY";
            case 5:
                return "SATURDAY";
            case 6:
                return "SUNDAY";
            default:
                return null;
        }
    }
}