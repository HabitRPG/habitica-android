package com.habitrpg.android.habitica.models.tasks;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class RemindersItem extends RealmObject {
    @PrimaryKey
    private String id;
    private Date startDate;
    private Date time;
    private Integer alarmId;

    //Use to store task type before a task is created
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getAlarmId() {
        return this.alarmId;
    }

    public void setAlarmId(Integer alarmId) {
        this.alarmId = alarmId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass().equals(RemindersItem.class)) {
            return this.id.equals(((RemindersItem)obj).id);
        }
        return super.equals(obj);
    }
}
