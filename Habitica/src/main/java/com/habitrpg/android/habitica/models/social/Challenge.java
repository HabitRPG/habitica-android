package com.habitrpg.android.habitica.models.social;

import com.google.gson.annotations.SerializedName;

import com.habitrpg.android.habitica.models.tasks.TasksOrder;
import com.habitrpg.android.habitica.models.user.User;

import java.util.HashMap;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Challenge extends RealmObject {

    public static final String TASK_ORDER_HABITS = "habits";
    public static final String TASK_ORDER_TODOS = "todos";
    public static final String TASK_ORDER_DAILYS = "dailys";
    public static final String TASK_ORDER_REWARDS = "rewards";

    @PrimaryKey
    public String id;
    public String name;
    public String shortName;
    public String description;
    public String leaderName;
    public String leaderId;
    public String groupName;
    public String groupId;
    public int prize;
    public boolean official;
    public int memberCount;
    @SerializedName("user_id")
    public String userId;
    public String todoList;
    public String habitList;
    public String dailyList;
    public String rewardList;

    public Group group;

    public User leader;
    @Ignore
    public TasksOrder tasksOrder;

    public HashMap<String, String[]> getTasksOrder() {
        HashMap<String, String[]> map = new HashMap();

        if (!dailyList.isEmpty()) {
            map.put(TASK_ORDER_DAILYS, dailyList.split(","));
        }

        if (!habitList.isEmpty()) {
            map.put(TASK_ORDER_HABITS, habitList.split(","));
        }

        if (!rewardList.isEmpty()) {
            map.put(TASK_ORDER_REWARDS, rewardList.split(","));
        }

        if (!todoList.isEmpty()) {
            map.put(TASK_ORDER_TODOS, todoList.split(","));
        }

        return map;
    }
}
