package com.habitrpg.android.habitica.models.inventory;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.HashMap;

/**
 * Created by viirus on 06/07/15.
 */
@Table(databaseName = HabitDatabase.NAME)
public class QuestProgress extends BaseModel {

    @Column
    public double hp, rage;
    public HashMap<String, Integer> collect;
    @Column
    @PrimaryKey(autoincrement = true)
    long id;
    @Column
    private float down, up;

    private QuestProgress(float down, float up) {
        this.down = down;
        this.up = up;
    }

    public QuestProgress() {
    }

    public float getDown() {
        return down;
    }

    public void setDown(float down) {
        this.down = down;
    }

    public float getUp() {
        return up;
    }

    public void setUp(float up) {
        this.up = up;
    }
}
