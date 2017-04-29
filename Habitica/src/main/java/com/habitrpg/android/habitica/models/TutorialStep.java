package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.models.user.Flags;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

import io.realm.RealmObject;

public class TutorialStep extends RealmObject {

    public Flags flags;
    @PrimaryKey
    private String key;
    private String tutorialGroup, identifier;
    private boolean wasCompleted;
    private Date displayedOn;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTutorialGroup() {
        return tutorialGroup;
    }

    public void setTutorialGroup(String group) {
        this.tutorialGroup = group;
        this.key = group + "_" + this.identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
        this.key = this.tutorialGroup + "_" + identifier;
    }

    public boolean getWasCompleted() {
        return wasCompleted;
    }

    public void setWasCompleted(boolean wasCompleted) {
        this.wasCompleted = wasCompleted;
    }

    public Date getDisplayedOn() {
        return displayedOn;
    }

    public void setDisplayedOn(Date displayedOn) {
        this.displayedOn = displayedOn;
    }

    public boolean shouldDisplay() {
        return  !this.getWasCompleted() && (this.getDisplayedOn() == null || (new Date().getTime() - this.getDisplayedOn().getTime()) > 86400000);
    }
}
