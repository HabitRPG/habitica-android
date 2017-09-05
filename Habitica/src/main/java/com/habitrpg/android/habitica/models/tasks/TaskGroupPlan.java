package com.habitrpg.android.habitica.models.tasks;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class TaskGroupPlan extends RealmObject {

    @PrimaryKey
    String task_id;

    public boolean approvalRequested, approvalApproved, approvalRequired;
}
