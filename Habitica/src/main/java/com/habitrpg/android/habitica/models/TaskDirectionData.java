package com.habitrpg.android.habitica.models;

/**
 * This class represent the data sent back from the API when calling /user/tasks/{id}/{direction}.
 * It holds almost the same thing as Stats, except toNextLevel & maxHealth & maxHP.
 * It also holds a delta, which represent the task value modification.
 * Created by MagicMicky on 12/06/2014.
 */
public class TaskDirectionData extends PlayerMinStats {
    private float delta;
    private TaskDirectionDataTemp _tmp;

    public float getDelta() {
        return delta;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

    public TaskDirectionDataTemp get_tmp() {
        return _tmp;
    }

    public void set_tmp(TaskDirectionDataTemp tmp) {
        this._tmp = tmp;
    }
}
