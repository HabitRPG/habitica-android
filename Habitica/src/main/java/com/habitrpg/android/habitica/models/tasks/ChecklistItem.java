package com.habitrpg.android.habitica.models.tasks;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.database.ExcludeCheckListItem;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;

/**
 * Created by viirus on 06/07/15.
 */
@Table(databaseName = HabitDatabase.NAME)
public class ChecklistItem extends BaseModel {

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
    private String text;
    @Column
    private boolean completed;
    @Column
    private int position;

    public ChecklistItem() {
        this(null, null);
    }

    public ChecklistItem(String id, String text) {
        this(id, text, false);
    }

    public ChecklistItem(String id, String text, boolean completed) {
        this.setText(text);
        this.setId(id);
        this.setCompleted(completed);
    }

    public ChecklistItem(String text) {
        this(null, text);
    }

    public ChecklistItem(ChecklistItem item) {
        this.text = item.getText();
        this.id = item.getId();
        this.completed = item.getCompleted();
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
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
        this.task.put("id", task.id);
    }

    @Override
    public void save() {
        if (this.getId() == null || this.getId().isEmpty()) {
            return;
        }
        super.save();
    }
}
