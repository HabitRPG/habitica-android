package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.responses.TaskDirectionData;
import com.habitrpg.android.habitica.models.tasks.ChecklistItem;
import com.habitrpg.android.habitica.models.tasks.RemindersItem;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.models.tasks.TaskList;
import com.habitrpg.android.habitica.models.tasks.TaskTag;
import com.habitrpg.android.habitica.models.tasks.TasksOrder;

import java.util.List;

import rx.Observable;

public interface TaskRepository extends BaseRepository  {
    Observable<List<Task>> getTasks(String taskType, String userID);
    Observable<List<Task>> getTasks(String userId);

    Observable<TaskList> refreshTasks(TasksOrder tasksOrder);

    Observable<TaskDirectionData> taskChecked(Task task, boolean up);
    Observable<Task> scoreChecklistItem(String taskId, String itemId);

    Observable<Task> getTask(String taskId);
    Observable<Task> createTask(Task task);
    Observable<Task> updateTask(Task task);
    Observable<Void> deleteTask(String taskId);
    void saveTask(Task task);

    Observable<List<Task>> createTasks(List<Task> newTasks);

    void removeOldTasks(String userID, List<Task> onlineTaskList);
    void removeOldChecklists(List<ChecklistItem> onlineChecklistItems);
    void removeOldTaskTags(List<TaskTag> onlineTaskTags);
    void removeOldReminders(List<RemindersItem> onlineReminders);

    Observable<TaskDirectionData> postTaskDirection(String taskId, String direction);

    void markTaskCompleted(String taskId, boolean isCompleted);
}
