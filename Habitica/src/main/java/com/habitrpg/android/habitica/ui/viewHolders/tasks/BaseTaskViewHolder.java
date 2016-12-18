package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.TaskTappedEvent;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;



import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BaseTaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

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

	@BindView(R.id.tags_glyph)
	ImageView tagsGlyph;

    public BaseTaskViewHolder(View itemView) {
        super(itemView);

        itemView.setOnClickListener(this);
        itemView.setClickable(true);

        ButterKnife.bind(this, itemView);

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
                        });
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
	    if (task.getTags() != null && !task.tags.isEmpty())
		    this.tagsGlyph.setVisibility(View.VISIBLE);
    }

	@OnClick(R.id.tags_glyph)
	void displayTagsToast(){
		String tagString = "";
		for(TaskTag t:
		    task.getTags())
		{
			tagString = tagString + t.getTag().getName() + ", ";
		}
		Toast.makeText(context, tagString, Toast.LENGTH_LONG).show();
	}

    @Override
    public void onClick(View v) {
        if (v != itemView) {
            return;
        }

        TaskTappedEvent event = new TaskTappedEvent();
        event.Task = task;

        EventBus.getDefault().post(event);
    }

    public boolean canContainMarkdown() {
        return true;
    }
}
