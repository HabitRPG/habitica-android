package com.habitrpg.android.habitica.data.local;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;

import java.util.ArrayList;

import rx.Observable;

public interface TaskLocalRepository extends BaseLocalRepository {

    Observable<ArrayList<Task>> getTasks(String taskType);

    void saveTasks(TasksOrder tasksOrder, TaskList tasks);

    void saveTask(Task task);
}
