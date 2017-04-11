package com.habitrpg.android.habitica.data.local;

import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public interface TaskLocalRepository extends BaseLocalRepository {

    Observable<List<Task>> getTasks(String taskType, String userID);

    void saveTasks(TasksOrder tasksOrder, TaskList tasks);

    void saveTask(Task task);

    void removeOldTasks(String userID, List<Task> onlineTaskList);
    void removeOldChecklists(List<ChecklistItem> onlineChecklistItems);
    void removeOldTaskTags(List<TaskTag> onlineTaskTags);
    void removeOldReminders(List<RemindersItem> onlineReminders);

    void deleteTask(String taskID);
}
