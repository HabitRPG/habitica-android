package com.magicmicky.habitrpgwrapper.lib.models.tasks;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.database.ExcludeCheckListItem;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import java.util.Date;

/**
 * Created by keithholliday on 5/31/16.
 */
@Table(databaseName = HabitDatabase.NAME)
public class RemindersItem extends BaseModel {
    @Column
    @PrimaryKey
    private String id;

    @Column
    private Date startDate;

    @Column
    private Date time;

    @Column
    private Integer alarmId;

    @Column
    @ForeignKey(
            references = {@ForeignKeyReference(columnName = "task_id",
                    columnType = String.class,
                    foreignColumnName = "id")},
            saveForeignKeyModel = false)
    @ExcludeCheckListItem
    ForeignKeyContainer<Task> task;

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

    public Task getTask() {
        if (task != null) {
            return task.toModel();
        } else {
            return null;
        }
    }

    public void setTask(Task task) {
        this.task = new ForeignKeyContainer<>(Task.class);
        this.task.setModel(task);
        this.task.put("id", task.getId());
    }

    @Override
    public void save() {
        if (this.getId() == null) {
            return;
        }
        super.save();
    }
}
