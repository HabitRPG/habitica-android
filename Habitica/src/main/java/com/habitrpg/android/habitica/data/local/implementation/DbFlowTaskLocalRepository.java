package com.habitrpg.android.habitica.data.local.implementation;

import android.database.sqlite.SQLiteDoneException;

import com.habitrpg.android.habitica.data.local.TaskLocalRepository;
import com.habitrpg.android.habitica.events.TaskRemovedEvent;
import com.habitrpg.android.habitica.models.tasks.ChecklistItem;
import com.habitrpg.android.habitica.models.tasks.Days;
import com.habitrpg.android.habitica.models.tasks.RemindersItem;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.tasks.TaskTag;
import com.habitrpg.android.habitica.models.tasks.TasksOrder;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Delete;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.language.Where;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
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
    public Observable<List<Task>> getTasks(String userId) {
        return Observable.defer(() -> Observable.just(new Select().from(Task.class)
                .where(Condition.column("user_id").eq(userId))
                .and(Condition.CombinedCondition
                        .begin(Condition.column("completed").eq(false))
                        .or(Condition.column("type").eq("daily"))
                )
                .orderBy(OrderBy.columns("position", "dateCreated").descending())
                .queryList()));    }

    public Observable<Task> getTask(String taskID) {
        return Observable.defer(() -> Observable.just(new Select().from(Task.class)
                .where(Condition.column("id").eq(taskID))
                .querySingle()));
    }

    @Override
    public void markTaskCompleted(String taskId, boolean isCompleted) {
        getTask(taskId).subscribe(task -> {
            task.completed = isCompleted;
            saveTask(task);
        }, throwable -> {});
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

    @Override
    public void removeOldTasks(String userID, List<Task> onlineTaskList) {
        final ArrayList<String> onlineTaskIdList = new ArrayList<>();

        for (Task oTask : onlineTaskList) {
            onlineTaskIdList.add(oTask.getId());
        }

        Where<Task> query = new Select().from(Task.class).where(Condition.column("user_id").eq(userID));
        try {
            if (query.count() != onlineTaskList.size()) {

                // Load Database Tasks
                query.async().queryList(new TransactionListener<List<Task>>() {
                    @Override
                    public void onResultReceived(List<Task> tasks) {

                        ArrayList<Task> tasksToDelete = new ArrayList<>();

                        for (Task dbTask : tasks) {
                            if (!onlineTaskIdList.contains(dbTask.getId())) {
                                tasksToDelete.add(dbTask);
                            }
                        }

                        for (Task delTask : tasksToDelete) {
                            // TaskTag
                            new Delete().from(TaskTag.class).where(Condition.column("task_id").eq(delTask.getId())).async().execute();

                            // ChecklistItem
                            new Delete().from(ChecklistItem.class).where(Condition.column("task_id").eq(delTask.getId())).async().execute();

                            // Days
                            new Delete().from(Days.class).where(Condition.column("task_id").eq(delTask.getId())).async().execute();

                            // TASK
                            delTask.async().delete();

                            EventBus.getDefault().post(new TaskRemovedEvent(delTask.getId()));
                        }
                    }

                    @Override
                    public boolean onReady(BaseTransaction<List<Task>> baseTransaction) {
                        return false;
                    }

                    @Override
                    public boolean hasResult(BaseTransaction<List<Task>> baseTransaction, List<Task> tasks) {
                        return tasks != null && tasks.size() > 0;
                    }
                });
            }
        } catch (SQLiteDoneException ignored) {
            //Ignored
        }
    }

    @Override
    public void removeOldChecklists(List<ChecklistItem> onlineChecklistItems) {
        final ArrayList<String> onlineChecklistItemIdList = new ArrayList<>();

        for (ChecklistItem item : onlineChecklistItems) {
            onlineChecklistItemIdList.add(item.getId());
        }

        From<ChecklistItem> query = new Select().from(ChecklistItem.class);
        try {
            if (query.count() != onlineChecklistItems.size()) {

                // Load Database Checklist items
                query.async().queryList(new TransactionListener<List<ChecklistItem>>() {
                    @Override
                    public void onResultReceived(List<ChecklistItem> items) {

                        ArrayList<ChecklistItem> checkListItemsToDelete = new ArrayList<>();

                        for (ChecklistItem chItem : items) {
                            if (!onlineChecklistItemIdList.contains(chItem.getId())) {
                                checkListItemsToDelete.add(chItem);
                            }
                        }

                        for (ChecklistItem chItem : checkListItemsToDelete) {
                            chItem.async().delete();
                        }
                    }

                    @Override
                    public boolean onReady(BaseTransaction<List<ChecklistItem>> baseTransaction) {
                        return false;
                    }

                    @Override
                    public boolean hasResult(BaseTransaction<List<ChecklistItem>> baseTransaction, List<ChecklistItem> items) {
                        return items != null && items.size() > 0;
                    }
                });
            }
        } catch (SQLiteDoneException ignored) {
            //Ignored
        }
    }

    @Override
    public void removeOldTaskTags(List<TaskTag> onlineTaskTags) {
        final ArrayList<String> onlineTaskTagItemIdList = new ArrayList<>();

        for (TaskTag item : onlineTaskTags) {
            onlineTaskTagItemIdList.add(item.getId());
        }

        From<TaskTag> query = new Select().from(TaskTag.class);
        try {
            if (query.count() != onlineTaskTags.size()) {

                // Load Database Checklist items
                query.async().queryList(new TransactionListener<List<TaskTag>>() {
                    @Override
                    public void onResultReceived(List<TaskTag> items) {

                        ArrayList<TaskTag> checkListItemsToDelete = new ArrayList<>();

                        for (TaskTag ttag : items) {
                            if (!onlineTaskTagItemIdList.contains(ttag.getId())) {
                                checkListItemsToDelete.add(ttag);
                            }
                        }

                        for (TaskTag ttag : checkListItemsToDelete) {
                            ttag.async().delete();
                        }
                    }

                    @Override
                    public boolean onReady(BaseTransaction<List<TaskTag>> baseTransaction) {
                        return false;
                    }

                    @Override
                    public boolean hasResult(BaseTransaction<List<TaskTag>> baseTransaction, List<TaskTag> items) {
                        return items != null && items.size() > 0;
                    }
                });
            }
        } catch (SQLiteDoneException ignored) {
            //Ignored
        }
    }

    @Override
    public void removeOldReminders(List<RemindersItem> onlineReminders) {
        final ArrayList<String> onlineReminderIds = new ArrayList<>();

        for (RemindersItem item : onlineReminders) {
            onlineReminderIds.add(item.getId());
        }

        From<RemindersItem> query = new Select().from(RemindersItem.class);
        try {
            if (query.count() != onlineReminders.size()) {

                // Load Database Checklist items
                query.async().queryList(new TransactionListener<List<RemindersItem>>() {
                    @Override
                    public void onResultReceived(List<RemindersItem> items) {

                        ArrayList<RemindersItem> remindersToDelete = new ArrayList<>();

                        for (RemindersItem reminder : items) {
                            if (!onlineReminderIds.contains(reminder.getId())) {
                                remindersToDelete.add(reminder);
                            }
                        }

                        for (RemindersItem reminder : remindersToDelete) {
                            reminder.async().delete();
                        }
                    }

                    @Override
                    public boolean onReady(BaseTransaction<List<RemindersItem>> baseTransaction) {
                        return false;
                    }

                    @Override
                    public boolean hasResult(BaseTransaction<List<RemindersItem>> baseTransaction, List<RemindersItem> items) {
                        return items != null && items.size() > 0;
                    }
                });
            }
        } catch (SQLiteDoneException ignored) {
            //Ignored
        }
    }

    @Override
    public void deleteTask(String taskID) {
        getTask(taskID).subscribe(Task::delete, throwable -> {});
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
