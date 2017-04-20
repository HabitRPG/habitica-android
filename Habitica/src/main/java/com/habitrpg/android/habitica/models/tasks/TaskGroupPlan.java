package com.habitrpg.android.habitica.models.tasks;


import com.habitrpg.android.habitica.HabitDatabase;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ModelContainer;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

@ModelContainer
@Table(databaseName = HabitDatabase.NAME)
public class TaskGroupPlan extends BaseModel {

    @Column
    @PrimaryKey
    @NotNull
    String task_id;

    @Column
    public boolean approvalRequested, approvalApproved, approvalRequired;
}
