package com.habitrpg.android.habitica.ui.adapter;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.databinding.SkillTaskItemCardBinding;
import com.habitrpg.android.habitica.models.tasks.Task;

import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import rx.Observable;
import rx.subjects.PublishSubject;


public class SkillTasksRecyclerViewAdapter extends RealmRecyclerViewAdapter<Task, SkillTasksRecyclerViewAdapter.ViewHolder> {


    private static final int TYPE_CELL = 1;
    private PublishSubject<Task> taskSelectionEvents = PublishSubject.create();

    public SkillTasksRecyclerViewAdapter(@Nullable OrderedRealmCollection<Task> data, boolean autoUpdate) {
        super(data, autoUpdate);
    }

    @Override
    public long getItemId(int position) {
        if (getData() != null) {
            Task task = getData().get(position);
            if (task.getId() != null && task.getId().length() == 36) {
                return UUID.fromString(task.getId()).getMostSignificantBits();
            }
        }
        return UUID.randomUUID().getMostSignificantBits();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.skill_task_item_card, parent, false);
        return new SkillTasksRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (getData() != null) {
            holder.bindHolder(getData().get(position));
        }
    }

    public Observable<Task> getTaskSelectionEvents() {
        return taskSelectionEvents.asObservable();
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

        void bindHolder(Task habitItem) {
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
            if (v.equals(itemView)) {
                taskSelectionEvents.onNext(task);
            }
        }
    }
}
