package com.habitrpg.android.habitica.models.social;

import com.habitrpg.android.habitica.HabitDatabase;
import com.habitrpg.android.habitica.models.user.User;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.NotNull;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;

import java.util.HashMap;

@Table(databaseName = HabitDatabase.NAME)
public class Challenge extends BaseModel {

    public static final String TASK_ORDER_HABITS = "habits";
    public static final String TASK_ORDER_TODOS = "todos";
    public static final String TASK_ORDER_DAILYS = "dailys";
    public static final String TASK_ORDER_REWARDS = "rewards";

    @NotNull
    @PrimaryKey
    @Column
    public String id;

    @Column
    public String name;

    @Column
    public String shortName;

    @Column
    public String description;

    @Column
    public String leaderName;

    @Column
    public String leaderId;

    @Column
    public String groupName;

    @Column
    public String groupId;

    @Column
    public int prize;

    @Column
    public boolean official;

    @Column
    public int memberCount;

    @Column
    public String user_id;

    @Column
    public String todoList;

    @Column
    public String habitList;

    @Column
    public String dailyList;

    @Column
    public String rewardList;

    public Group group;

    public User leader;

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
