package com.habitrpg.android.habitica.callbacks;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by magicmicky on 18/02/15.
 */
public class TaskScoringCallback implements Callback<TaskDirectionData> {
    private final OnTaskScored mCallback;
    private final String taskId;

    public TaskScoringCallback(OnTaskScored callback, String taskId) {
        this.mCallback = callback;
        this.taskId = taskId;
    }

    @Override
    public void success(TaskDirectionData taskDirectionData, Response response) {
        Task task = new Select().from(Task.class).where(Condition.column("id").eq(taskId)).querySingle();
        task.value = task.value + taskDirectionData.getDelta();
        task.save();
        this.mCallback.onTaskDataReceived(taskDirectionData);
    }

    @Override
    public void failure(RetrofitError error) {
        Crashlytics.logException(error);

        this.mCallback.onTaskScoringFailed();
        Log.w("TaskScoring", "Task scoring failed " + error.getMessage());
    }

    public interface OnTaskScored {
        public void onTaskDataReceived(TaskDirectionData data);

        public void onTaskScoringFailed();
    }
}
