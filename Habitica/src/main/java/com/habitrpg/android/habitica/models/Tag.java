package com.habitrpg.android.habitica.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.models.tasks.TaskTag;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.OneToMany;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.List;

/**
 * Description of a Tag in HabitRPG
 * Created by MagicMicky on 16/03/14.
 */

@ModelContainer
@Table(databaseName = HabitDatabase.NAME)
public class Tag extends BaseModel {

    @Column
    @PrimaryKey
    @NotNull
    public String id;

    @Column
    @NotNull
    public String user_id;
    public List<TaskTag> tasks;
    @Column
    String name;

    public Tag() {
        this(null, null);
    }

    public Tag(String id, String name) {
        this.setId(id);
        this.setName(name);
    }

    @OneToMany(methods = {OneToMany.Method.SAVE, OneToMany.Method.DELETE}, variableName = "tasks")
    public List<TaskTag> getTasks() {
        if (tasks == null) {
            tasks = new Select()
                    .from(TaskTag.class)
                    .where(Condition.column("tag_id").eq(this.id)).and(Condition.column("task_id").isNotNull())
                    .queryList();
        }
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
    public void delete() {
        if (getTasks() != null) {
            for (TaskTag tt : getTasks()) {
                tt.delete();
            }
        }
        super.delete();
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass().equals(Tag.class)) {
            Tag otherTag = (Tag) o;
            return this.getId().equals(otherTag.getId());
        }
        return super.equals(o);
    }

    @Override
    public void save() {
        if (user_id == null) {
            return;
        }
        super.save();
    }
}
