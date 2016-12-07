package com.habitrpg.android.habitica.ui.adapter.tasks;

import com.habitrpg.android.habitica.HabiticaBaseApplication;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.events.TaskCreatedEvent;
import com.habitrpg.android.habitica.events.TaskRemovedEvent;
import com.habitrpg.android.habitica.events.TaskUpdatedEvent;
import com.habitrpg.android.habitica.events.commands.FilterTasksByTagsCommand;
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand;
import com.habitrpg.android.habitica.helpers.TagsHelper;
import com.habitrpg.android.habitica.proxy.ifce.CrashlyticsProxy;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.habitrpg.android.habitica.ui.viewHolders.tasks.BaseTaskViewHolder;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.OrderBy;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.app.Activity;
import android.content.Context;
import android.databinding.ObservableArrayList;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class BaseTasksRecyclerViewAdapter<VH extends BaseTaskViewHolder>
        extends RecyclerView.Adapter<VH> {
    @Inject
    protected CrashlyticsProxy crashlyticsProxy;

    private final String userID;
    int layoutResource;
    String taskType;
    Context context;
    List<Task> content;
    List<Task> filteredContent;
    private TagsHelper tagsHelper;

    public BaseTasksRecyclerViewAdapter(String taskType, TagsHelper tagsHelper, int layoutResource,
                                        Context newContext, String userID) {
        this.setHasStableIds(true);
        this.taskType = taskType;
        this.context = newContext;
        this.tagsHelper = tagsHelper;
        this.userID = userID;
        this.filteredContent = new ArrayList<>();
        injectThis(HabiticaBaseApplication.getComponent());

        this.loadContent(true);

        this.layoutResource = layoutResource;
    }

    protected abstract void injectThis(AppComponent component);

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Task item = filteredContent.get(position);

        holder.bindHolder(item, position);

        /*if (this.displayedChecklist != null && ChecklistedViewHolder.class.isAssignableFrom(holder.getClass())) {
            ChecklistedViewHolder checklistedHolder = (ChecklistedViewHolder) holder;
            checklistedHolder.setDisplayChecklist(this.displayedChecklist == position);
        }*/
    }

    @Override
    public long getItemId(int position) {
        Task task = filteredContent.get(position);
        return task.getId().hashCode();
    }

    @Override
    public int getItemCount() {
        return filteredContent != null ? filteredContent.size() : 0;
    }

    public View getContentView(ViewGroup parent) {
        return LayoutInflater.from(parent.getContext()).inflate(layoutResource, parent, false);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        EventBus.getDefault().unregister(this);

    }

    @Subscribe
    public void onEvent(FilterTasksByTagsCommand cmd) {
        filter();
    }

    @Subscribe
    public void onEvent(TaskCheckedCommand evnt) {
        if (!taskType.equals(evnt.Task.getType()))
            return;

        if (evnt.completed && evnt.Task.getType().equals("todo")) {
            // remove from the list
            content.remove(evnt.Task);
        }
        this.updateTask(evnt.Task);
        filter();
    }

    @Subscribe
    public void onEvent(TaskUpdatedEvent evnt) {
        if (!taskType.equals(evnt.task.getType()))
            return;
        this.updateTask(evnt.task);
        filter();
    }

    @Subscribe
    public void onEvent(TaskCreatedEvent evnt) {
        if (!taskType.equals(evnt.task.getType()))
            return;

        content.add(0, evnt.task);
        filter();
    }

    @Subscribe
    public void onEvent(TaskRemovedEvent evnt) {
        Task taskToDelete = null;

        for (Task t : content) {
            if (t.getId().equals(evnt.deletedTaskId)) {
                taskToDelete = t;
                break;
            }
        }

        if (taskToDelete != null) {
            content.remove(taskToDelete);
            filter();
        }
    }

    private void updateTask(Task task) {
        int i;
        for (i = 0; i < this.content.size(); ++i) {
            if (content.get(i).getId().equals(task.getId())) {
                break;
            }
        }
        if (i < content.size()) {
            content.set(i, task);
        }
    }

    private void filter() {
        if (this.tagsHelper.howMany() == 0) {
            filteredContent = content;
        } else {
            filteredContent = new ObservableArrayList<>();
            filteredContent.addAll(this.tagsHelper.filter(content));
        }

        ((Activity) context).runOnUiThread(this::notifyDataSetChanged);
    }

    public void loadContent(boolean forced) {
        if (this.content == null || forced) {
            List<Task> tasks = new ArrayList<>();
            Observable.defer(() -> Observable.just(new Select().from(Task.class)
                    .where(Condition.column("type").eq(this.taskType))
                    .and(Condition.CombinedCondition
                            .begin(Condition.column("completed").eq(false))
                            .or(Condition.column("type").eq("daily"))
                    )
                    .and(Condition.column("user_id").eq(this.userID))
                    .orderBy(OrderBy.columns("position", "dateCreated").descending())
                    .queryList()))
                    .flatMap(Observable::from)
                    .map(task -> {
                        try {
                            task.parsedText = MarkdownParser.parseMarkdown(task.getText());
                        } catch (NullPointerException e) {
                            task.parsedText = task.getText();
                        }
                        try {
                            task.parsedNotes = MarkdownParser.parseMarkdown(task.getNotes());
                        } catch (NullPointerException e) {
                            task.parsedNotes = task.getNotes();
                        }
                        return task;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(tasks::add, crashlyticsProxy::logException, () -> setTasks(tasks));
        }
    }

    public void setTasks(List<Task> tasks) {
        this.content = new ObservableArrayList<>();
        this.content.addAll(tasks);
        filter();
    }

}
