package com.habitrpg.android.habitica.models.responses;

/**
 * Created by MagicMicky on 16/03/14.
 */
public enum TaskDirection {
    up("up"),
    down("down");
    private final String dir;

    TaskDirection(String dir) {
        this.dir = dir;
    }

    public String toString() {
        return this.dir;
    }

}
