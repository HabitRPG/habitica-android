package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.habitrpg.android.habitica.models.tasks.ChecklistItem;
import com.habitrpg.android.habitica.models.tasks.RemindersItem;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.tasks.TaskTag;
import com.habitrpg.android.habitica.models.tasks.TasksOrder;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;

public class RealmTaskLocalRepository extends RealmBaseLocalRepository implements TaskLocalRepository {

    public RealmTaskLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<RealmResults<Task>> getTasks(String taskType, String userID) {
        return realm.where(Task.class).equalTo("type", taskType).equalTo("userId", userID).findAllSorted("position").sort("dateCreated", Sort.DESCENDING).asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public Observable<RealmResults<Task>> getTasks(String userId) {
        return realm.where(Task.class).equalTo("userId", userId).findAll().asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public void saveTasks(TasksOrder tasksOrder, TaskList tasks) {
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(tasks.tasks.values()));
    }

    @Override
    public void saveTask(Task task) {
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(task));
    }

    @Override
    public void removeOldTasks(String userID, List<Task> onlineTaskList) {

    }

    @Override
    public void removeOldChecklists(List<ChecklistItem> onlineChecklistItems) {

    }

    @Override
    public void removeOldTaskTags(List<TaskTag> onlineTaskTags) {

    }

    @Override
    public void removeOldReminders(List<RemindersItem> onlineReminders) {

    }

    @Override
    public void deleteTask(String taskID) {
        Task task = realm.where(Task.class).equalTo("id", taskID).findFirstAsync();
        realm.executeTransaction(realm1 -> task.deleteFromRealm());
    }

    @Override
    public Observable<Task> getTask(String taskId) {
        return realm.where(Task.class).equalTo("id", taskId).findFirstAsync().asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(Task.class);
    }

    @Override
    public Observable<Task> getTaskCopy(String taskId) {
        return getTask(taskId)
                .map(realm::copyFromRealm);
    }

    @Override
    public void markTaskCompleted(String taskId, boolean isCompleted) {
        Task task = realm.where(Task.class).equalTo("id", taskId).findFirstAsync();
        realm.executeTransaction(realm1 -> task.completed = true);
    }

    @Override
    public void saveReminder(RemindersItem remindersItem) {
        realm.executeTransaction(realm1 -> realm1.copyToRealm(remindersItem));
    }
}
