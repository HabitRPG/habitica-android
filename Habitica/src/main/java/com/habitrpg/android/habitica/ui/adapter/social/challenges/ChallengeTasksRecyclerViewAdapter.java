package com.habitrpg.android.habitica.ui.adapter.social.challenges;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.github.underscore.U;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.ui.adapter.tasks.SortableTasksRecyclerViewAdapter;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.DailyViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.HabitViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.RewardViewHolder;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.TodoViewHolder;

import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subjects.PublishSubject;

public class ChallengeTasksRecyclerViewAdapter
        extends SortableTasksRecyclerViewAdapter<BaseTaskViewHolder> {
    public static final String TASK_TYPE_ADD_ITEM = "ADD_ITEM";

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_HABIT = 1;
    private static final int TYPE_DAILY = 2;
    private static final int TYPE_TODO = 3;
    private static final int TYPE_REWARD = 4;
    private static final int TYPE_ADD_ITEM = 5;

    private int dailyResetOffset = 0;
    private PublishSubject<Task> addItemSubject = PublishSubject.create();
    private boolean openTaskDisabled;
    private boolean taskActionsDisabled;

    public ChallengeTasksRecyclerViewAdapter(@Nullable TaskFilterHelper taskFilterHelper, int layoutResource,
                                             Context newContext, String userID, @Nullable SortTasksCallback sortCallback,
                                             boolean openTaskDisabled, boolean taskActionsDisabled) {
        super("", taskFilterHelper, layoutResource, newContext, userID, sortCallback);
        this.openTaskDisabled = openTaskDisabled;
        this.taskActionsDisabled = taskActionsDisabled;
    }

    public void setDailyResetOffset(int newResetOffset) {
        dailyResetOffset = newResetOffset;
    }

    @Override
    protected void injectThis(AppComponent component) {
        component.inject(this);
    }

    @Override
    public boolean loadFromDatabase() {
        return false;
    }

    @Override
    public int getItemViewType(int position) {
        Task task = this.getFilteredContent().get(position);

        if (task.getType().equals(Task.TYPE_HABIT))
            return TYPE_HABIT;

        if (task.getType().equals(Task.TYPE_DAILY))
            return TYPE_DAILY;

        if (task.getType().equals(Task.TYPE_TODO))
            return TYPE_TODO;

        if (task.getType().equals(Task.TYPE_REWARD))
            return TYPE_REWARD;

        if (addItemSubject.hasObservers() && task.getType().equals(TASK_TYPE_ADD_ITEM))
            return TYPE_ADD_ITEM;

        return TYPE_HEADER;
    }

    public Flowable<Task> addItemObservable(){
        return addItemSubject.toFlowable(BackpressureStrategy.BUFFER);
    }

    public int addTaskUnder(Task taskToAdd, @Nullable Task taskAbove) {
        int position = U.findIndex(this.getContent(), t -> t.getId().equals(taskAbove.getId()));

        getContent().add(position + 1, taskToAdd);
        filter();

        return position;
    }

    @Override
    public BaseTaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        BaseTaskViewHolder viewHolder = null;

        switch (viewType) {
            case TYPE_HABIT:
                viewHolder = new HabitViewHolder(getContentView(parent, R.layout.habit_item_card));
                break;
            case TYPE_DAILY:
                viewHolder = new DailyViewHolder(getContentView(parent, R.layout.daily_item_card));
                break;
            case TYPE_TODO:
                viewHolder = new TodoViewHolder(getContentView(parent, R.layout.todo_item_card));
                break;
            case TYPE_REWARD:
                viewHolder = new RewardViewHolder(getContentView(parent, R.layout.reward_item_card));
                break;
            case TYPE_ADD_ITEM:
                viewHolder = new AddItemViewHolder(getContentView(parent, R.layout.challenge_add_task_item), addItemSubject);
                break;
            default:
                viewHolder = new DividerViewHolder(getContentView(parent, R.layout.challenge_task_divider));
                break;
        }

        viewHolder.setDisabled(openTaskDisabled, taskActionsDisabled);
        return viewHolder;
    }

    public List<Task> getTaskList(){
        return U.map(getContent(), t -> t);
    }


    /**
     * @param task
     * @return true if task found&updated
     */
    public boolean replaceTask(Task task) {
        int i;
        for (i = 0; i < this.getContent().size(); ++i) {
            if (getContent().get(i).getId().equals(task.getId())) {
                break;
            }
        }
        if (i < getContent().size()) {
            getContent().set(i, task);

            filter();
            return true;
        }

        return false;
    }

    public class AddItemViewHolder extends BaseTaskViewHolder {

        private Button addBtn;
        private PublishSubject<Task> callback;
        private Task newTask;

        AddItemViewHolder(View itemView, PublishSubject<Task> callback) {
            super(itemView);
            this.callback = callback;

            addBtn = itemView.findViewById(R.id.btn_add_task);
            addBtn.setClickable(true);
            addBtn.setOnClickListener(view -> callback.onNext(newTask));
            setContext(itemView.getContext());
        }

        @Override
        public void bindHolder(@NonNull Task newTask, int position) {
            this.newTask = newTask;
            addBtn.setText(newTask.getText());
        }
    }

    private class DividerViewHolder extends BaseTaskViewHolder {

        private TextView divider_name;

        DividerViewHolder(View itemView) {
            super(itemView);

            divider_name = itemView.findViewById(R.id.divider_name);

            setContext(itemView.getContext());
        }

        @Override
        public void bindHolder(@NonNull Task newTask, int position) {
            divider_name.setText(newTask.getText());
        }
    }
}
