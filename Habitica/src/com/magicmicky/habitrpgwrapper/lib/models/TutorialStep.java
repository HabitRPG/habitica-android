package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by viirus on 22/01/16.
 */
@Table(databaseName = HabitDatabase.NAME)
public class TutorialStep extends BaseModel {

    @PrimaryKey
    @Column
    private String key;

    @Column
    public String user_id;

    @Column
    private String group, identifier;

    @Column
    private boolean wasCompleted;

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

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean getWasCompleted() {
        return wasCompleted;
    }

    public void setWasCompleted(boolean wasCompleted) {
        this.wasCompleted = wasCompleted;
    }

}
