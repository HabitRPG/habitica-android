package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;
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
    public void saveTasks(TasksOrder tasksOrder, TaskList tasks) {
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

    @Override
    public void saveTask(Task task) {
        task.async().save();
    }

    private List<Task> sortTasks(TaskList taskList, List<String> taskOrder) {
        List<Task> taskResult = new ArrayList<>();
        int position = 0;


        for (String taskId : taskOrder) {
            Task task = taskList.tasks.get(taskId);
            if (task != null) {
                task.position = position;
                taskResult.add(task);
                position++;
                taskList.tasks.remove(taskId);
            }
        }
        return taskResult;
    }

    @Override
    public void close() {

    }
}
