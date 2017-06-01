package com.habitrpg.android.habitica.callbacks;

import com.habitrpg.android.habitica.models.responses.TaskDirectionData;
import com.habitrpg.android.habitica.models.inventory.Egg;
import com.habitrpg.android.habitica.models.inventory.Food;
import com.habitrpg.android.habitica.models.inventory.HatchingPotion;
import com.habitrpg.android.habitica.models.inventory.Item;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.From;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.Locale;

import rx.functions.Action1;

/**
 * Created by magicmicky on 18/02/15.
 */
public class TaskScoringCallback implements Action1<TaskDirectionData> {
    private final OnTaskScored mCallback;
    private final String taskId;

    public TaskScoringCallback(OnTaskScored callback, String taskId) {
        this.mCallback = callback;
        this.taskId = taskId;
    }

    @Override
    public void call(TaskDirectionData taskDirectionData) {
        new Select().from(Task.class).where(Condition.column("id").eq(taskId))
                .async()
                .querySingle(new TransactionListener<Task>() {
                    @Override
                    public void onResultReceived(Task task) {
                        if (task != null && task.type != null && !task.type.equals("reward")) {
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

                switch (type.toLowerCase(Locale.US)) {
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
                                        Item item = (Item) result;
                                        item.setOwned(item.getOwned() + 1);
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

    public interface OnTaskScored {
        void onTaskDataReceived(TaskDirectionData data, Task task);
    }
}
