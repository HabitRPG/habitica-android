package com.magicmicky.habitrpgwrapper.lib.utils;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by viirus on 09/12/15.
 */
public class TaskListDeserializer implements JsonDeserializer<TaskList> {

    public TaskList deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
        TaskList tasks = new TaskList();
        List<Task> habits = new ArrayList<>();
        List<Task> dailies = new ArrayList<>();
        List<Task> todos = new ArrayList<>();
        List<Task> rewards = new ArrayList<>();

        for (JsonElement e : json.getAsJsonArray()) {
            Task task = ctx.deserialize(e, Task.class);
            switch(task.type) {
                case Task.TYPE_HABIT:
                    task.position = habits.size();
                    habits.add(task);
                    break;
                case Task.TYPE_DAILY:
                    task.position = dailies.size();
                    dailies.add(task);
                    break;
                case Task.TYPE_TODO:
                    task.position = todos.size();
                    todos.add(task);
                    break;
                case Task.TYPE_REWARD:
                    task.position = rewards.size();
                    rewards.add(task);
                    break;
            }
        }

        tasks.habits = habits;
        tasks.dailies = dailies;
        tasks.todos = todos;
        tasks.rewards = rewards;

        return tasks;
    }
}
