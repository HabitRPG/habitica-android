package com.magicmicky.habitrpgwrapper.lib.models.tasks;

import com.habitrpg.android.habitica.HabitDatabase;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.ForeignKeyReference;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.raizlabs.android.dbflow.structure.container.ForeignKeyContainer;


/**
 * Created by viirus on 08/08/15.
 */

@Table(databaseName = HabitDatabase.NAME)
public class TaskTag extends BaseModel {

    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "tag_id",
            columnType = String.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    public ForeignKeyContainer<Tag> tag;
    @Column
    @ForeignKey(references = {@ForeignKeyReference(columnName = "task_id",
            columnType = String.class,
            foreignColumnName = "id")},
            saveForeignKeyModel = false)
    public ForeignKeyContainer<Task> task;
    @Column
    @PrimaryKey
    String id;
    private String tagId = "";
    private String taskId = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Tag getTag() {
        return tag.toModel();
    }

    public void setTag(Tag tag) {
        this.tag = new ForeignKeyContainer<>(Tag.class);
        this.tag.setModel(tag);
        this.tag.put("id", tag.id);

        tagId = tag.id;
        updatePrimaryKey();
    }

    public Task getTask() {
        return task.toModel();
    }

    public void setTask(Task task) {
        this.task = new ForeignKeyContainer<>(Task.class);
        this.task.setModel(task);
        this.task.put("id", task.id);

        taskId = task.id;
        updatePrimaryKey();
    }

    private void updatePrimaryKey() {
        this.id = taskId + "_" + tagId;
    }
}
