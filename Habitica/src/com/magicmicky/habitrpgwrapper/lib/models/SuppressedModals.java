package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by viirus on 15/11/15.
 */
@Table(databaseName = HabitDatabase.NAME)
public class SuppressedModals extends BaseModel {

    @Column
    @PrimaryKey
    public String userId;

    private Boolean streak;
    private Boolean raisePet;
    private Boolean hatchPet;
    private Boolean levelUp;

    public Boolean getStreak() {return streak; }
    public Boolean getRaisePet() {return raisePet; }
    public Boolean getHatchPet() {return hatchPet; }
    public Boolean getLevelUp() {return levelUp; }

    public void setStreak(Boolean streak) {this.streak = streak; }
    public void setRaisePet(Boolean raisePet) {this.raisePet = raisePet; }
    public void setHatchPet(Boolean hatchPet) {this.hatchPet = hatchPet; }
    public void setLevelUp(Boolean levelUp) {this.levelUp = levelUp; }
}
