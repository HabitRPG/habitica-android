package com.habitrpg.android.habitica.callbacks;

import android.util.Log;

import com.magicmicky.habitrpgwrapper.lib.models.TaskDirectionData;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Egg;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Food;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.HatchingPotion;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Item;
import com.magicmicky.habitrpgwrapper.lib.models.inventory.Pet;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.From;
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
    public void success(final TaskDirectionData taskDirectionData, Response response) {
        new Select().from(Task.class).where(Condition.column("id").eq(taskId))
                .async()
                .querySingle(new TransactionListener<Task>() {
                    @Override
                    public void onResultReceived(Task task) {
                        if(task != null && task.type != null && !task.type.equals("reward")) {
                            task.value = task.value + taskDirectionData.getDelta();

                            task.save();
                        }

                        mCallback.onTaskDataReceived(taskDirectionData, task);
                    }

                    @Override
                    public boolean onReady(BaseTransaction<Task> baseTransaction) {
                        return true;
                    }

                    @Override
                    public boolean hasResult(BaseTransaction<Task> baseTransaction, Task task) {
                        return task != null;
                    }
                });
        if (taskDirectionData.get_tmp() != null) {
            if (taskDirectionData.get_tmp().getDrop() != null) {
                String type = taskDirectionData.get_tmp().getDrop().getType();
                From from = null;

                switch (type.toLowerCase()) {
                    case "hatchingpotion":
                        from = new Select().from(HatchingPotion.class);
                        break;
                    case "food":
                        from = new Select().from(Food.class);
                        break;
                    case "egg":
                        from = new Select().from(Egg.class);
                        break;
                }

                if (from != null) {
                    from.where(Condition.column("key").eq(taskDirectionData.get_tmp().getDrop().getKey()))
                            .async()
                            .querySingle(new TransactionListener() {
                                @Override
                                public void onResultReceived(Object result) {
                                    if (result != null) {
                                        Item item = (Item)result;
                                        item.setOwned(item.getOwned()+1);
                                        item.save();
                                    }
                                }

                                @Override
                                public boolean onReady(BaseTransaction transaction) {
                                    return true;
                                }

                                @Override
                                public boolean hasResult(BaseTransaction transaction, Object result) {
                                    return true;
                                }
                            });
                }
            }
        }
    }

    @Override
    public void failure(RetrofitError error) {
        this.mCallback.onTaskScoringFailed();
        Log.w("TaskScoring", "Task scoring failed", error);
    }

    public interface OnTaskScored {
        void onTaskDataReceived(TaskDirectionData data, Task task);

        void onTaskScoringFailed();
    }
}
