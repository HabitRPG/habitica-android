package com.habitrpg.android.habitica.data.implementation;

import android.support.annotation.Nullable;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.responses.TaskDirection;
import com.habitrpg.android.habitica.models.responses.TaskScoringResult;
import com.habitrpg.android.habitica.models.tasks.RemindersItem;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.tasks.TasksOrder;
import com.habitrpg.android.habitica.models.user.Stats;
import com.habitrpg.android.habitica.models.user.User;

import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;


public class TaskRepositoryImpl extends BaseRepositoryImpl<TaskLocalRepository> implements TaskRepository {

    private long lastTaskAction = 0;

    public TaskRepositoryImpl(TaskLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<RealmResults<Task>> getTasks(String taskType, String userID) {
        return this.localRepository.getTasks(taskType, userID);
    }

    @Override
    public Observable<RealmResults<Task>> getTasks(String userId) {
        return this.localRepository.getTasks(userId);
    }

    @Override
    public void saveTasks(String userId, TasksOrder order, TaskList tasks) {
        localRepository.saveTasks(userId, order, tasks);
    }

    @Override
    public Observable<TaskList> refreshTasks(TasksOrder tasksOrder) {
        return this.apiClient.getTasks()
                .doOnNext(res -> this.localRepository.saveTasks(null, tasksOrder, res));
    }

    @Override
    public Observable<TaskScoringResult> taskChecked(@Nullable User user, Task task, boolean up, boolean force) {
        long now = new Date().getTime();
        if (lastTaskAction > now-500 || force) {
            return Observable.just(null);
        }
        lastTaskAction = now;
        return this.apiClient.postTaskDirection(task.getId(), (up ? TaskDirection.up : TaskDirection.down).toString())
                .map(res -> {
                    // save local task changes
                    TaskScoringResult result = new TaskScoringResult();
                    if (user != null) {
                        Stats stats = user.getStats();

                        result.taskValueDelta = res.getDelta();
                        result.healthDelta = res.hp - stats.getHp();
                        result.experienceDelta = res.exp - stats.getExp();
                        result.manaDelta = res.mp - stats.getMp();
                        result.goldDelta = res.gp - stats.getGp();
                        result.hasLeveledUp = res.lvl > stats.getLvl();
                        if (res.get_tmp() != null) {
                            result.drop = res.get_tmp().getDrop();
                        }
                        this.localRepository.executeTransaction(realm -> {
                            if (task.type != null && !task.type.equals("reward")) {
                                task.value = task.value + res.getDelta();
                                if (Task.TYPE_DAILY.equals(task.type) || Task.TYPE_TODO.equals(task.type)) {
                                    task.completed = up;
                                }
                            }
                            stats.setHp(res.hp);
                            stats.setExp(res.exp);
                            stats.setMp(res.mp);
                            stats.setGp(res.gp);
                            stats.setLvl(res.lvl);
                            user.setStats(stats);
                        });
                    }
                    return result;
                });
    }

    @Override
    public Observable<TaskScoringResult> taskChecked(User user, String taskId, boolean up, boolean force) {
        return localRepository.getTask(taskId)
                .flatMap(task -> taskChecked(user, task, up, force));
    }

    public Observable<Task> scoreChecklistItem(String taskId, String itemId){
        return apiClient.scoreChecklistItem(taskId, itemId)
                .zipWith(localRepository.getTask(taskId), (newTask, oldTask) -> {
                    newTask.position = oldTask.position;
                    return newTask;
                })
                .doOnNext(this.localRepository::saveTask);
    }

    @Override
    public Observable<Task> getTask(String taskId) {
        return localRepository.getTask(taskId);
    }

    @Override
    public Observable<Task> getTaskCopy(String taskId) {
        return localRepository.getTaskCopy(taskId);
    }

    @Override
    public Observable<Task> createTask(Task task) {
        long now = new Date().getTime();
        if (lastTaskAction > now-500) {
            return Observable.just(task);
        }
        lastTaskAction = now;
        return apiClient.createTask(task)
                .map(task1 -> {
                    task1.dateCreated = new Date();
                    return task1;
                })
                .doOnNext(localRepository::saveTask);
    }

    @Override
    public Observable<Task> updateTask(Task task) {
        long now = new Date().getTime();
        if (lastTaskAction > now-500) {
            return Observable.just(task);
        }
        lastTaskAction = now;
        return localRepository.getTaskCopy(task.getId()).first()
                .flatMap(task1 -> apiClient.updateTask(task1.getId(), task1))
                .map(task1 -> {
                    task1.position = task.position;
                    return task1;
                })
                .doOnNext(localRepository::saveTask);
    }

    @Override
    public Observable<Void> deleteTask(String taskID) {
        return apiClient.deleteTask(taskID)
                .doOnNext(aVoid -> localRepository.deleteTask(taskID));
    }

    @Override
    public void saveTask(Task task) {
        localRepository.saveTask(task);
    }

    @Override
    public Observable<List<Task>> createTasks(List<Task> newTasks) {
        return apiClient.createTasks(newTasks);
    }

    @Override
    public void markTaskCompleted(String taskId, boolean isCompleted) {
        localRepository.markTaskCompleted(taskId, isCompleted);
    }

    @Override
    public void saveReminder(RemindersItem remindersItem) {
        localRepository.saveReminder(remindersItem);
    }

    @Override
    public void executeTransaction(Realm.Transaction transaction) {
        localRepository.executeTransaction(transaction);
    }

    @Override
    public void swapTaskPosition(int firstPosition, int secondPosition) {
        localRepository.swapTaskPosition(firstPosition, secondPosition);
    }

    public Observable<List<String>> updateTaskPosition(int currentPosition) {
        return localRepository.getTaskAtPosition(currentPosition)
                .flatMap(task -> apiClient.postTaskNewPosition(task.getId(), currentPosition));
    }

    @Override
    public Observable<Task> getUnmanagedTask(String taskid) {
        return getTask(taskid)
                .map(localRepository::getUnmanagedCopy);
    }

    @Override
    public void updateTaskInBackground(Task task) {
        updateTask(task).subscribe(task1 -> {}, RxErrorHandler.handleEmptyError());
    }

    @Override
    public void createTaskInBackground(Task task) {
        createTask(task).subscribe(task1 -> {}, RxErrorHandler.handleEmptyError());
    }

    @Override
    public Observable<List<Task>> getTaskCopies(String userId) {
        return getTasks(userId)
                .map(localRepository::getUnmanagedCopy);
    }

    @Override
    public Observable<List<Task>> getTaskCopies(RealmResults<Task> tasks) {
        return Observable.just(localRepository.getUnmanagedCopy(tasks));
    }
}
