package com.habitrpg.android.habitica.models.user;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import io.realm.RealmObject;

public class SuppressedModals extends RealmObject {

    Preferences preferences;
    private Boolean streak, raisePet, hatchPet, levelUp;

    public Boolean getStreak() {
        return streak;
    }

    public void setStreak(Boolean streak) {
        this.streak = streak;
    }

    public Boolean getRaisePet() {
        return raisePet;
    }

    public void setRaisePet(Boolean raisePet) {
        this.raisePet = raisePet;
    }

    public Boolean getHatchPet() {
        return hatchPet;
    }

    public void setHatchPet(Boolean hatchPet) {
        this.hatchPet = hatchPet;
    }

    public Boolean getLevelUp() {
        return levelUp;
    }

    public void setLevelUp(Boolean levelUp) {
        this.levelUp = levelUp;
    }
}
