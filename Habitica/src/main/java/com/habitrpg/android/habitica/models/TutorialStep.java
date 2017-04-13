package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.Date;

@Table(databaseName = HabitDatabase.NAME)
public class TutorialStep extends BaseModel {

    @Column
    public String user_id;
    @PrimaryKey
    @Column
    private String key;
    @Column
    private String tutorialGroup, identifier;

    @Column
    private boolean wasCompleted;

    @Column
    private Date displayedOn;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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
