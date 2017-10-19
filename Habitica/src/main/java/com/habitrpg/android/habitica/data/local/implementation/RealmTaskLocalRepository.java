package com.habitrpg.android.habitica.data.local.implementation;

import android.support.annotation.Nullable;

import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.habitrpg.android.habitica.models.tasks.ChecklistItem;
import com.habitrpg.android.habitica.models.tasks.RemindersItem;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.tasks.TasksOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.realm.OrderedRealmCollectionSnapshot;
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
        return realm.where(Task.class)
                .equalTo("type", taskType)
                .equalTo("userId", userID)
                .findAllSorted("position")
                .sort("dateCreated", Sort.DESCENDING)
                .asObservable()
                .filter(RealmResults::isLoaded)
                .retry(1);
    }

    @Override
    public Observable<RealmResults<Task>> getTasks(String userId) {
        return realm.where(Task.class).equalTo("userId", userId).findAll().asObservable()
                .filter(RealmResults::isLoaded);
    }

    @Override
    public void saveTasks(@Nullable String userId, TasksOrder tasksOrder, TaskList tasks) {
        List<Task> sortedTasks = new ArrayList<>();
        if (tasks != null) {
            if (tasksOrder != null) {
                sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.getHabits()));
                sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.getDailys()));
                sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.getTodos()));
                sortedTasks.addAll(sortTasks(tasks.tasks, tasksOrder.getRewards()));
            } else {
                sortedTasks.addAll(tasks.tasks.values());
            }
        }
        if (userId != null) {
            removeOldTasks(userId, sortedTasks);

            List<ChecklistItem> allChecklistItems = new ArrayList<>();
            for (Task t : sortedTasks) {
                if (t.checklist != null) {
                    allChecklistItems.addAll(t.checklist);
                }
            }
            removeOldChecklists(allChecklistItems);

            List<RemindersItem> allReminders = new ArrayList<>();
            for (Task t : sortedTasks) {
                if (t.getReminders() != null) {
                    allReminders.addAll(t.getReminders());
                }
            }
            removeOldReminders(allReminders);
        }
        realm.executeTransactionAsync(realm1 -> realm1.insertOrUpdate(sortedTasks));
    }

    private List<Task> sortTasks(Map<String, Task> taskMap, List<String> taskOrder) {
        List<Task> taskList = new ArrayList<>();
        int position = 0;
        for (String taskId : taskOrder) {
            Task task = taskMap.get(taskId);
            if (task != null) {
                task.position = position;
                taskList.add(task);
                position++;
                taskMap.remove(taskId);
            }
        }
        return taskList;
    }

    @Override
    public void saveTask(Task task) {
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(task));
    }

    private void removeOldTasks(String userID, List<Task> onlineTaskList) {
        OrderedRealmCollectionSnapshot<Task> localTasks = realm.where(Task.class).equalTo("userId", userID).findAll().createSnapshot();
        List<Task> tasksToDelete = new ArrayList<>();
        for (Task localTask : localTasks) {
            if (!onlineTaskList.contains(localTask)) {
                tasksToDelete.add(localTask);
            }
        }
        realm.executeTransaction(realm1 -> {
            for (Task localTask : tasksToDelete) {
                if (localTask.checklist != null) {
                    localTask.checklist.deleteAllFromRealm();
                }
                if (localTask.reminders != null) {
                    localTask.reminders.deleteAllFromRealm();
                }
                localTask.deleteFromRealm();
            }
        });
    }

    private void removeOldChecklists(List<ChecklistItem> onlineItems) {
        OrderedRealmCollectionSnapshot<ChecklistItem> localItems = realm.where(ChecklistItem.class).findAll().createSnapshot();
        List<ChecklistItem> itemsToDelete = new ArrayList<>();
        for (ChecklistItem localItem : localItems) {
            if (!onlineItems.contains(localItem)) {
                itemsToDelete.add(localItem);
            }
        }
        realm.executeTransaction(realm1 -> {
            for (ChecklistItem item : itemsToDelete) {
                item.deleteFromRealm();
            }
        });
    }

    private void removeOldReminders(List<RemindersItem> onlineReminders) {
        OrderedRealmCollectionSnapshot<RemindersItem> localReminders = realm.where(RemindersItem.class).findAll().createSnapshot();
        List<RemindersItem> itemsToDelete = new ArrayList<>();
        for (RemindersItem localItem : localReminders) {
            if (!onlineReminders.contains(localItem)) {
                itemsToDelete.add(localItem);
            }
        }
        realm.executeTransaction(realm1 -> {
            for (RemindersItem item : itemsToDelete) {
                item.deleteFromRealm();
            }
        });
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
                .map(task -> {
                    if (task.isManaged() && task.isValid()) {
                        return realm.copyFromRealm(task);
                    } else {
                        return task;
                    }
                });
    }

    @Override
    public void markTaskCompleted(String taskId, boolean isCompleted) {
        Task task = realm.where(Task.class).equalTo("id", taskId).findFirstAsync();
        realm.executeTransaction(realm1 -> task.completed = true);
    }

    @Override
    public void saveReminder(RemindersItem remindersItem) {
        realm.executeTransaction(realm1 -> realm1.insertOrUpdate(remindersItem));
    }

    @Override
    public void swapTaskPosition(int firstPosition, int secondPosition) {
        Task firstTask = realm.where(Task.class).equalTo("position", firstPosition).findFirst();
        Task secondTask = realm.where(Task.class).equalTo("position", secondPosition).findFirst();
        if (firstTask != null && secondTask != null && firstTask.isValid() && secondTask.isValid()) {
            realm.executeTransaction(realm1 -> {
                firstTask.position = secondPosition;
                secondTask.position = firstPosition;
            });
        }
    }

    @Override
    public Observable<Task> getTaskAtPosition(int currentPosition) {
        return realm.where(Task.class).equalTo("position", currentPosition).findFirstAsync().asObservable()
                .filter(realmObject -> realmObject.isLoaded())
                .cast(Task.class);
    }

    @Override
    public Observable<TaskList> updateIsdue(TaskList dailies) {
        return Observable.just(realm.where(Task.class).equalTo("type", "daily").findAll())
                .first()
                .map(tasks -> {
                    realm.beginTransaction();
                    for (Task task : tasks) {
                        if (dailies.tasks.containsKey(task.getId())) {
                            task.isDue = dailies.tasks.get(task.getId()).isDue;
                        }
                    }
                    realm.commitTransaction();
                    return dailies;
                });
    }

    @Override
    public void updateTaskPositions(List<String> taskOrder) {
        if (taskOrder.size() > 0) {
            RealmResults<Task> tasks = realm.where(Task.class).in("id", taskOrder.toArray(new String[0])).findAll();
            realm.executeTransaction(realm1 -> {
                for (Task task : tasks) {
                    if (taskOrder.contains(task.getId())) {
                        task.position = taskOrder.indexOf(task.getId());
                    }
                }
            });
        }
    }
}
