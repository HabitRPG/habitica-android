package com.habitrpg.android.habitica.data;

import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.RemindersItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public interface TaskRepository extends BaseRepository  {
    Observable<List<Task>> getTasks(String taskType, String userID);

    Observable<TaskList> refreshTasks(TasksOrder tasksOrder);

    Observable<TaskDirectionData> taskChecked(Task task, boolean up);
    Observable<Task> scoreChecklistItem(String taskId, String itemId);

    Observable<Task> createTask(Task task);
    Observable<Task> updateTask(Task task);
    Observable<Void> deleteTask(String taskID);

    Observable<List<Task>> createTasks(List<Task> newTasks);

    void removeOldTasks(String userID, List<Task> onlineTaskList);
    void removeOldChecklists(List<ChecklistItem> onlineChecklistItems);
    void removeOldTaskTags(List<TaskTag> onlineTaskTags);
    void removeOldReminders(List<RemindersItem> onlineReminders);
}
