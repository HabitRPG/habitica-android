package com.habitrpg.android.habitica.models.tasks;


import com.habitrpg.android.habitica.models.Tag;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by viirus on 08/08/15.
 */

public class TaskTag extends RealmObject {

    public Tag tag;
    public Task task;
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
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
        tagId = tag.getId();
        updatePrimaryKey();
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;

        taskId = task.getId();
        updatePrimaryKey();
    }

    private void updatePrimaryKey() {
        this.id = taskId + "_" + tagId;
    }
}
