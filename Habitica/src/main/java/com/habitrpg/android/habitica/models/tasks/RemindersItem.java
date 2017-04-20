package com.habitrpg.android.habitica.models.tasks;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.database.ExcludeCheckListItem;
import com.habitrpg.android.habitica.events.ReminderDeleteEvent;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.List;

/**
 * Created by keithholliday on 5/31/16.
 */
@Table(databaseName = HabitDatabase.NAME)
public class RemindersItem extends BaseModel {
    @Column
    @ForeignKey(
            references = {@ForeignKeyReference(columnName = "task_id",
                    columnType = String.class,
                    foreignColumnName = "id")},
            saveForeignKeyModel = false)
    @ExcludeCheckListItem
    ForeignKeyContainer<Task> task;
    @Column
    @PrimaryKey
    private String id;
    @Column
    private Date startDate;
    @Column
    private Date time;
    @Column
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

    public Task getTask() {
        if (task != null) {
            //This will get all the task info
            Task taskModel = task.toModel();

            if (taskModel.getId() == null) {
                return taskModel;
            }

            List<Task> task = new Select()
                    .from(Task.class)
                    .where(Condition.column("id").eq(taskModel.getId()))
                    .queryList();

            return task.get(0);
        } else {
            return null;
        }
    }

    public void setTask(Task task) {
        this.task = new ForeignKeyContainer<>(Task.class);
        this.task.setModel(task);
        this.task.put("id", task.getId());
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public void save() {
        if (this.getId() == null) {
            return;
        }
        super.save();
    }

    @Override
    public void delete() {
        ReminderDeleteEvent event = new ReminderDeleteEvent();
        event.reminder = this;
        EventBus.getDefault().post(event);
        super.delete();
    }
}
