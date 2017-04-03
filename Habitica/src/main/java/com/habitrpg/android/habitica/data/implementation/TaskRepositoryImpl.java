package com.habitrpg.android.habitica.data.implementation;

import com.magicmicky.habitrpgwrapper.lib.api.ApiClient;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirection;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;

import java.util.ArrayList;
import java.util.Date;

import rx.Observable;


public class TaskRepositoryImpl extends BaseRepositoryImpl<TaskLocalRepository> implements TaskRepository {

    private long lastTaskAction = 0;

    public TaskRepositoryImpl(TaskLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<ArrayList<Task>> getTasks(String taskType) {
        return this.localRepository.getTasks(taskType);
    }

    @Override
    public Observable<TaskList> refreshTasks(TasksOrder tasksOrder) {
        return this.apiClient.getTasks()
                .doOnNext(res -> this.localRepository.saveTasks(tasksOrder, res));
    }

    @Override
    public Observable<TaskDirectionData> taskChecked(Task task, boolean up) {
        long now = new Date().getTime();
        if (lastTaskAction > now-500) {
            return Observable.empty();
        }
        lastTaskAction = now;
        return this.apiClient.postTaskDirection(task.getId(), (up ? TaskDirection.up : TaskDirection.down).toString())
                .doOnNext(res -> {
                    // save local task changes
                    if (task.type != null && !task.type.equals("reward")) {
                        task.value = task.value + res.getDelta();

                        this.localRepository.saveTask(task);
                    }
                });
    }

    public Observable<Task> scoreChecklistItem(String taskId, String itemId){
        return apiClient.scoreChecklistItem(taskId, itemId).doOnNext(this.localRepository::saveTask);
    }

    @Override
    public Observable<Task> createTask(Task task) {
        long now = new Date().getTime();
        if (lastTaskAction > now-500) {
            return Observable.empty();
        }
        return apiClient.createItem(task);
    }

    @Override
    public Observable<Task> updateTask(String id, Task task) {
        long now = new Date().getTime();
        if (lastTaskAction > now-500) {
            return Observable.empty();
        }
        return apiClient.updateTask(id, task);
    }
}
