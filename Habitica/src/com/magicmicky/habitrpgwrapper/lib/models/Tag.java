package com.magicmicky.habitrpgwrapper.lib.models;

import com.habitrpg.android.habitica.HabitDatabase;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.raizlabs.android.dbflow.annotation.Column;
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

@Table(databaseName = HabitDatabase.NAME)
public class Tag extends BaseModel{

    @Column
    @PrimaryKey
    public String id;

    @Column
    String name;

    public List<TaskTag> tasks;

    public Tag() {
        this(null,null);
    }

    public Tag(String id, String name) {
        this.setId(id);
        this.setName(name);
    }

    public List<TaskTag> getTasks() {
        if(tasks == null) {
            tasks = new Select()
                    .from(TaskTag.class)
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

}
