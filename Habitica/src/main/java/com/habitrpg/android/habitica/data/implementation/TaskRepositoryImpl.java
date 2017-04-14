package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.habitrpg.android.habitica.events.TaskCreatedEvent;
import com.habitrpg.android.habitica.events.TaskUpdatedEvent;
import com.habitrpg.android.habitica.models.responses.TaskDirection;
import com.habitrpg.android.habitica.models.responses.TaskDirectionData;
import com.habitrpg.android.habitica.models.tasks.ChecklistItem;
import com.habitrpg.android.habitica.models.tasks.RemindersItem;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.tasks.TaskTag;
import com.habitrpg.android.habitica.models.tasks.TasksOrder;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.List;

import rx.Observable;


public class TaskRepositoryImpl extends BaseRepositoryImpl<TaskLocalRepository> implements TaskRepository {

    private long lastTaskAction = 0;

    public TaskRepositoryImpl(TaskLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<List<Task>> getTasks(String taskType, String userID) {
        return this.localRepository.getTasks(taskType, userID);
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
        return apiClient.createItem(task)
                .doOnNext(task1 -> {
                    localRepository.saveTask(task1);
                    EventBus.getDefault().post(new TaskCreatedEvent(task1));
                });
    }

    @Override
    public Observable<Task> updateTask(Task task) {
        long now = new Date().getTime();
        if (lastTaskAction > now-500) {
            return Observable.empty();
        }
        return apiClient.updateTask(task.getId(), task)
                .doOnNext(task1 -> {
                    localRepository.saveTask(task1);
                    EventBus.getDefault().post(new TaskUpdatedEvent(task1));
                });
    }

    @Override
    public Observable<Void> deleteTask(String taskID) {
        return apiClient.deleteTask(taskID)
                .doOnNext(aVoid -> localRepository.deleteTask(taskID));
    }

    @Override
    public Observable<List<Task>> createTasks(List<Task> newTasks) {
        return apiClient.createTasks(newTasks);
    }

    @Override
    public void removeOldTasks(String userID, List<Task> onlineTaskList) {
        localRepository.removeOldTasks(userID, onlineTaskList);
    }

    @Override
    public void removeOldChecklists(List<ChecklistItem> onlineChecklistItems) {
        localRepository.removeOldChecklists(onlineChecklistItems);
    }

    @Override
    public void removeOldTaskTags(List<TaskTag> onlineTaskTags) {
        localRepository.removeOldTaskTags(onlineTaskTags);
    }

    @Override
    public void removeOldReminders(List<RemindersItem> onlineReminders) {
        localRepository.removeOldReminders(onlineReminders);
    }
}
