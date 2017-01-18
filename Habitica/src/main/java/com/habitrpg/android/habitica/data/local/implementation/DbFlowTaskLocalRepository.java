package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;

public class DbFlowTaskLocalRepository implements TaskLocalRepository {

    @Override
    public Observable<ArrayList<Task>> getTasks(String taskType) {
        return null;
    }

    @Override
    public void saveTasks(TasksOrder tasksOrder, ArrayList<Task> tasks) {
        sortTasks(tasks, tasksOrder.getHabits());
        sortTasks(tasks, tasksOrder.getDailys());
        sortTasks(tasks, tasksOrder.getTodos());
        sortTasks(tasks, tasksOrder.getRewards());

        // Negue: once everything is refactored to DbFlowTaskLocalRepository, this will be used to save the tasks
        // not in HabitRPGUserCallback

        /*
        for (Task t : tasks){
            t.async().save();
        }
        */
    }

    private List<Task> sortTasks(List<Task> taskList, List<String> taskOrder) {
        List<Task> taskResult = new ArrayList<>();
        int position = 0;

        HashMap<String, Task> taskMap = new HashMap<>();

        for (Task t : taskList){
            taskMap.put(t.getId(), t);
        }

        for (String taskId : taskOrder) {
            Task task = taskMap.get(taskId);
            if (task != null) {
                task.position = position;
                taskResult.add(task);
                position++;
                taskMap.remove(taskId);
            }
        }
        return taskList;
    }

    @Override
    public void close() {

    }
}
