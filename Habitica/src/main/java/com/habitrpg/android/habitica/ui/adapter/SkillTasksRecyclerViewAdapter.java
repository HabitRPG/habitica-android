package com.habitrpg.android.habitica.ui.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;
import rx.Observable;
import rx.subjects.PublishSubject;


public class SkillTasksRecyclerViewAdapter extends RealmRecyclerViewAdapter<Task, SkillTasksRecyclerViewAdapter.ViewHolder> {

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

        @BindView(R.id.titleTextView)
        TextView titleTextView;
        @BindView(R.id.notesTextView)
        TextView notesTextView;
        @BindView(R.id.rightBorderView)
        View rightBorderView;

        public ViewHolder(View itemView) {
            super(itemView);

            itemView.setOnClickListener(this);
            itemView.setClickable(true);

            ButterKnife.bind(this, itemView);

            resources = itemView.getResources();
        }

        void bindHolder(Task task) {
            this.task = task;
            titleTextView.setText(task.getText());
            if (task.getNotes() == null || task.getNotes().length() == 0) {
                notesTextView.setVisibility(View.GONE);
            } else {
                notesTextView.setVisibility(View.VISIBLE);
                notesTextView.setText(task.getNotes());
            }
            rightBorderView.setBackgroundResource(task.getLightTaskColor());
        }

        @Override
        public void onClick(View v) {
            if (v.equals(itemView)) {
                taskSelectionEvents.onNext(task);
            }
        }
    }
}
