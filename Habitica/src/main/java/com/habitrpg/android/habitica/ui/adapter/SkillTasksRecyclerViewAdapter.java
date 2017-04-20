package com.habitrpg.android.habitica.ui.adapter;

import android.databinding.DataBindingUtil;
import android.databinding.ObservableArrayList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.databinding.SkillTaskItemCardBinding;
import com.habitrpg.android.habitica.helpers.ReactiveErrorHandler;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.ui.activities.TaskClickActivity;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SkillTasksRecyclerViewAdapter extends RecyclerView.Adapter<SkillTasksRecyclerViewAdapter.ViewHolder> {


    private static final int TYPE_CELL = 1;
    private final TaskRepository taskRepository;
    String taskType;
    TaskClickActivity activity;
    private ObservableArrayList<Task> observableContent;
    private RecyclerView.Adapter<ViewHolder> parentAdapter;
    private String userId;

    public SkillTasksRecyclerViewAdapter(TaskRepository taskRepository, String taskType, TaskClickActivity activity, String userId) {
        this.setHasStableIds(true);
        this.taskType = taskType;
        this.activity = activity;
        this.taskRepository = taskRepository;
        this.userId = userId;

        this.loadContent();

    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            default:
                return TYPE_CELL;
        }
    }

    @Override
    public long getItemId(int position) {
        Task task = observableContent.get(position);
        if (task.getId() != null && task.getId().length() == 36) {
            return UUID.fromString(task.getId()).getMostSignificantBits();
        }
        return UUID.randomUUID().getMostSignificantBits();
    }

    @Override
    public int getItemCount() {
        return observableContent.size();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.skill_task_item_card, parent, false);
        return new SkillTasksRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Task item = observableContent.get(position);

        holder.bindHolder(item, position);
    }

    // region ViewHolders

    public void loadContent() {
        this.loadContent(false);
    }

    // endregion

    public void loadContent(boolean forced) {

        if (this.observableContent == null || forced) {

            this.observableContent = new ObservableArrayList<>();
            taskRepository.getTasks(taskType, userId).subscribe(tasks -> {
                this.observableContent.addAll(tasks);
                if (parentAdapter != null) {
                    parentAdapter.notifyDataSetChanged();
                } else {
                    notifyDataSetChanged();
                }
            }, throwable -> {});
        } else {
            if (parentAdapter != null) {
                parentAdapter.notifyDataSetChanged();
            } else {
                notifyDataSetChanged();
            }
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public Task task;
        protected android.content.res.Resources resources;
        SkillTaskItemCardBinding binding;

        @BindView(R.id.notesTextView)
        TextView notesTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setClickable(true);

            ButterKnife.bind(this, itemView);
            binding = DataBindingUtil.bind(itemView);

            resources = itemView.getResources();
        }

        void bindHolder(Task habitItem, int position) {
            task = habitItem;
            if (habitItem.notes == null || habitItem.notes.length() == 0) {
                notesTextView.setHeight(0);
            } else {
                notesTextView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            }
            binding.setTask(task);
        }

        @Override
        public void onClick(View v) {
            if (v != itemView)
                return;
            activity.taskSelected(task.getId());
        }

    }
}
