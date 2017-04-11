package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import rx.Observable;

public class DbFlowTaskLocalRepository implements TaskLocalRepository {

    @Override
    public Observable<List<Task>> getTasks(String taskType, String userID) {
        return Observable.defer(() -> Observable.just(new Select().from(Task.class)
                .where(Condition.column("type").eq(taskType))
                .and(Condition.CombinedCondition
                        .begin(Condition.column("completed").eq(false))
                        .or(Condition.column("type").eq("daily"))
                )
                .and(Condition.column("user_id").eq(userID))
                .orderBy(OrderBy.columns("position", "dateCreated").descending())
                .queryList()));
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
