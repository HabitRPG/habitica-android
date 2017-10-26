package com.habitrpg.android.habitica.models.user;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class SuppressedModals extends RealmObject {

    @PrimaryKey
    private String userId;

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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
