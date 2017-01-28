package com.habitrpg.android.habitica.data;

import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.responses.HabitResponse;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskList;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TasksOrder;

import java.util.ArrayList;

import rx.Observable;

public interface TaskRepository extends BaseRepository  {
    Observable<ArrayList<Task>> getTasks(String taskType);

    Observable<TaskList> refreshTasks(TasksOrder tasksOrder);

    Observable<TaskDirectionData> scoreHabit(Task task, boolean up);
}
