package com.habitrpg.android.habitica.callbacks;

import com.habitrpg.android.habitica.data.InventoryRepository;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.models.responses.TaskDirectionData;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.Locale;

import rx.functions.Action1;

/**
 * Created by magicmicky on 18/02/15.
 */
public class TaskScoringCallback implements Action1<TaskDirectionData> {
    private final OnTaskScored mCallback;
    private final String taskId;
    private final TaskRepository taskRepository;
    private final InventoryRepository inventoryRepository;

    public TaskScoringCallback(TaskRepository taskRepository, InventoryRepository inventoryRepository, OnTaskScored callback, String taskId) {
        this.mCallback = callback;
        this.taskId = taskId;
        this.taskRepository = taskRepository;
        this.inventoryRepository = inventoryRepository;
    }

    @Override
    public void call(TaskDirectionData taskDirectionData) {
        taskRepository.getTask(taskId).subscribe(task -> {
            if (task != null && task.type != null && !task.type.equals("reward")) {
                task.value = task.value + taskDirectionData.getDelta();

                taskRepository.saveTask(task);
            }

            mCallback.onTaskDataReceived(taskDirectionData, task);
        });
        if (taskDirectionData.get_tmp() != null) {
            if (taskDirectionData.get_tmp().getDrop() != null) {
                String type = taskDirectionData.get_tmp().getDrop().getType();
                String key = taskDirectionData.get_tmp().getDrop().getKey();
                inventoryRepository.changeOwnedCount(type.toLowerCase(Locale.US), key, 1);
            }
        }
    }

    public interface OnTaskScored {
        void onTaskDataReceived(TaskDirectionData data, Task task);
    }
}
