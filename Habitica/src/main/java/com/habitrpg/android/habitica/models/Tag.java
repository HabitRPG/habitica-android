package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.models.tasks.TaskTag;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Description of a Tag in HabitRPG
 * Created by MagicMicky on 16/03/14.
 */

public class Tag extends RealmObject {

    @PrimaryKey
    public String id;

    public String userId;
    public RealmList<TaskTag> tasks;
    String name;

    public List<TaskTag> getTasks() {
        return tasks;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    @Override
    public boolean equals(Object o) {
        if (o.getClass().equals(Tag.class)) {
            Tag otherTag = (Tag) o;
            return this.getId().equals(otherTag.getId());
        }
        return super.equals(o);
    }
}
