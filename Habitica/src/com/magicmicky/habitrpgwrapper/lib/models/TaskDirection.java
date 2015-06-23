package com.magicmicky.habitrpgwrapper.lib.models;

/**
 * Created by MagicMicky on 16/03/14.
 */
public enum TaskDirection {
    up("up"),
    down("down");
    private final String dir;
    private TaskDirection(String dir) {
        this.dir=dir;
    }
    public String toString() {
        return this.dir;
    }

}
