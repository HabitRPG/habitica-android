package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.habitrpg.android.habitica.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public abstract class BaseTaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {


    public Task task;
    protected Context context;
    @BindView(R.id.checkedTextView)
    TextView titleTextView;

    @BindView(R.id.notesTextView)
    TextView notesTextView;

    @Nullable
    @BindView(R.id.rightBorderView)
    View rightBorderView;

    @BindColor(R.color.task_gray)
    int taskGray;

    @Nullable
    @BindView(R.id.specialTaskText)
    TextView specialTaskTextView;

    @Nullable
    @BindView(R.id.iconviewChallenge)
    ImageView iconViewChallenge;

    @Nullable
    @BindView(R.id.iconviewReminder)
    ImageView iconViewReminder;

    @Nullable
    @BindView(R.id.iconviewTag)
    ImageView iconViewTag;

    @Nullable
    @BindView(R.id.taskIconWrapper)
    LinearLayout taskIconWrapper;

    @BindView(R.id.approvalRequiredTextField)
    TextView approvalRequiredTextView;

    boolean disabled;

    public BaseTaskViewHolder(View itemView) {
        this(itemView, true);
    }

    public BaseTaskViewHolder(View itemView, boolean useButterKnife) {
        super(itemView);

        itemView.setOnClickListener(this);
        itemView.setClickable(true);

        if (useButterKnife) {
            ButterKnife.bind(this, itemView);
        }

        //Re enable when we find a way to only react when a link is tapped.
        //this.notesTextView.setMovementMethod(LinkMovementMethod.getInstance());
        //this.titleTextView.setMovementMethod(LinkMovementMethod.getInstance());

        context = itemView.getContext();
    }

    public void bindHolder(Task newTask, int position) {
        this.task = newTask;
        if (this.canContainMarkdown()) {
            if (this.task.parsedText != null) {
                this.titleTextView.setText(this.task.parsedText);
                this.notesTextView.setText(this.task.parsedNotes);
            } else {
                this.titleTextView.setText(this.task.getText());
                this.notesTextView.setText(this.task.getNotes());
                Observable.just(this.task)
                        .map(task1 -> {
                            task.parsedText = MarkdownParser.parseMarkdown(task.getText());
                            task.parsedNotes = MarkdownParser.parseMarkdown(task.getNotes());
                            return task;
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(task2 -> {
                            this.titleTextView.setText(this.task.parsedText);
                            this.notesTextView.setText(this.task.parsedNotes);
                        }, Throwable::printStackTrace);
            }
        } else {
            this.titleTextView.setText(this.task.getText());
            this.notesTextView.setText(this.task.getNotes());
        }
        if (this.task.getNotes() != null && this.task.getNotes().length() > 0) {
            this.notesTextView.setVisibility(View.VISIBLE);
        } else {
            this.notesTextView.setVisibility(View.GONE);
        }

        if (this.rightBorderView != null) {
            this.rightBorderView.setBackgroundResource(this.task.getLightTaskColor());
        }
        if (this.iconViewReminder != null) {
            this.iconViewReminder.setVisibility(this.task.getReminders().size() > 0 ? View.VISIBLE : View.GONE);
        }
        if (this.iconViewTag != null) {
            this.iconViewTag.setVisibility(this.task.getTags().size() > 0 ? View.VISIBLE : View.GONE);
        }

        if (this.iconViewChallenge != null) {
            this.iconViewChallenge.setVisibility(View.GONE);
        }

        this.configureSpecialTaskTextView(task);

        if (this.taskIconWrapper != null) {
            this.taskIconWrapper.setVisibility(getTaskIconWrapperIsVisible() ? View.VISIBLE : View.GONE);
        }

        if (task.isPendingApproval()) {
            approvalRequiredTextView.setVisibility(View.VISIBLE);
        } else {
            approvalRequiredTextView.setVisibility(View.GONE);
        }

    }



    protected void configureSpecialTaskTextView(Task task) {
        if (this.specialTaskTextView != null) {
            this.specialTaskTextView.setVisibility(View.INVISIBLE);
        }
    }

    protected Boolean getTaskIconWrapperIsVisible() {
        Boolean isVisible = false;

        if (this.iconViewReminder != null) {
            if (this.iconViewReminder.getVisibility() == View.VISIBLE) {
                isVisible = true;
            }
        }
        if (this.iconViewTag != null) {
            if (this.iconViewTag.getVisibility() == View.VISIBLE) {
                isVisible = true;
            }
        }
        if (this.iconViewChallenge != null) {
            if (this.iconViewChallenge.getVisibility() == View.VISIBLE) {
                isVisible = true;
            }
        }
        if (this.iconViewReminder != null) {
            if (this.iconViewReminder.getVisibility() == View.VISIBLE) {
                isVisible = true;
            }
        }
        if (this.specialTaskTextView != null) {
            if (this.specialTaskTextView.getVisibility() == View.VISIBLE) {
                isVisible = true;
            }
        }
        return isVisible;
    }

    @Override
    public void onClick(View v) {
        if (v != itemView || isDisabled()) {
            return;
        }

        TaskTappedEvent event = new TaskTappedEvent();
        event.Task = task;

        EventBus.getDefault().post(event);
    }

    public boolean canContainMarkdown() {
        return true;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;

        itemView.setEnabled(!disabled);
    }
}
