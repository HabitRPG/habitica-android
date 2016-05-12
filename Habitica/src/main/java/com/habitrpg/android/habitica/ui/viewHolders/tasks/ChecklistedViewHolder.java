package com.habitrpg.android.habitica.ui.viewHolders.tasks;

import com.github.data5tream.emojilib.EmojiTextView;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import org.greenrobot.eventbus.EventBus;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;

public abstract class ChecklistedViewHolder extends BaseTaskViewHolder implements CompoundButton.OnCheckedChangeListener {

    @BindView(R.id.checkBoxHolder)
    ViewGroup checkboxHolder;

    @BindView(R.id.checkBox)
    CheckBox checkbox;

    @BindView(R.id.checklistView)
    LinearLayout checklistView;
    @BindView(R.id.checklistSeparator)
    View checklistSeparator;
    @BindView(R.id.checklistBottomSpace)
    View checklistBottomSpace;

    @BindView(R.id.checklistIndicatorWrapper)
    ViewGroup checklistIndicatorWrapper;

    @BindView(R.id.checkListCompletedTextView)
    TextView checklistCompletedTextView;
    @BindView(R.id.checkListAllTextView)
    TextView checklistAllTextView;

    public Boolean displayChecklist;

    public ChecklistedViewHolder(View itemView) {
        super(itemView);
        checklistIndicatorWrapper.setClickable(true);
        checkbox.setOnCheckedChangeListener(this);
        expandCheckboxTouchArea(checkboxHolder, checkbox);
        this.displayChecklist = false;
    }

    @Override
    public void bindHolder(Task newTask, int position) {
        super.bindHolder(newTask, position);
        this.checkbox.setChecked(this.task.completed);
        if (this.shouldDisplayAsActive()) {
            this.checkboxHolder.setBackgroundResource(this.task.getLightTaskColor());
        } else {
            this.checkboxHolder.setBackgroundColor(this.taskGray);
        }
        this.checklistCompletedTextView.setText(String.valueOf(task.getCompletedChecklistCount()));
        this.checklistAllTextView.setText(String.valueOf(task.getChecklist().size()));

        this.checklistView.removeAllViews();
        this.setDisplayChecklist(this.displayChecklist);

        this.checklistIndicatorWrapper.setVisibility(task.checklist.size() == 0 ? View.GONE : View.VISIBLE);
        if (this.rightBorderView != null) {
            this.rightBorderView.setVisibility(task.checklist.size() == 0 ? View.VISIBLE : View.GONE);
            if (this.task.getCompleted()) {
                this.rightBorderView.setBackgroundResource(this.task.getLightTaskColor());
            } else {
                this.rightBorderView.setBackgroundColor(this.taskGray);
            }
        }
    }

    abstract public Boolean shouldDisplayAsActive();

    public void setDisplayChecklist(Boolean displayChecklist) {
        this.displayChecklist = displayChecklist;
        //This needs to be a LinearLayout, as ListViews can not be inside other ListViews.
        if (this.checklistView != null) {
            if (this.displayChecklist && this.task.checklist != null) {
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                for (ChecklistItem item : this.task.checklist) {
                    LinearLayout itemView = (LinearLayout) layoutInflater.inflate(R.layout.checklist_item_row, this.checklistView, false);
                    CheckBox checkbox = (CheckBox) itemView.findViewById(R.id.checkBox);
                    EmojiTextView textView = (EmojiTextView) itemView.findViewById(R.id.checkedTextView);
                    // Populate the data into the template view using the data object
                    textView.setText(item.getText());
                    checkbox.setChecked(item.getCompleted());
                    checkbox.setOnCheckedChangeListener(this);
                    RelativeLayout checkboxHolder = (RelativeLayout) itemView.findViewById(R.id.checkBoxHolder);
                    expandCheckboxTouchArea(checkboxHolder, checkbox);
                    this.checklistView.addView(itemView);
                }
                this.checklistSeparator.setVisibility(View.VISIBLE);
                this.checklistView.setVisibility(View.VISIBLE);
                this.checklistBottomSpace.setVisibility(View.VISIBLE);
            } else {
                this.checklistView.removeAllViewsInLayout();
                this.checklistSeparator.setVisibility(View.GONE);
                this.checklistView.setVisibility(View.GONE);
                this.checklistBottomSpace.setVisibility(View.GONE);
            }
        }
    }

    @OnClick(R.id.checklistIndicatorWrapper)
    public void onChecklistIndicatorClicked() {
        if (this.displayChecklist != null) {
            this.setDisplayChecklist(!this.displayChecklist);
        } else {
            this.setDisplayChecklist(true);
        }
        RecyclerView recyclerView = (RecyclerView)this.checklistView.getParent().getParent();
        LinearLayoutManager layoutManager = (LinearLayoutManager)recyclerView.getLayoutManager();
        layoutManager.scrollToPositionWithOffset(this.getAdapterPosition(), 15);
    }

    public void expandCheckboxTouchArea(final View expandedView, final View checkboxView){
        expandedView.post(() -> {
            Rect rect = new Rect();
            expandedView.getHitRect(rect);
            expandedView.setTouchDelegate(new TouchDelegate(rect, checkboxView));
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == checkbox) {
            if (isChecked != task.getCompleted()) {
                TaskCheckedCommand event = new TaskCheckedCommand();
                event.Task = task;
                event.completed =  !task.getCompleted();

                // it needs to be changed after the event is send -> to the server
                // maybe a refactor is needed here
                EventBus.getDefault().post(event);
                task.completed =event.completed;
                task.save();

            }
        } else {
            View v = (View) buttonView.getParent();
            while (v.getParent() != this.checklistView) {
                v = (View) v.getParent();
            }
            Integer position = ((ViewGroup) v.getParent()).indexOfChild(v);
            if (task.checklist.size() > position && isChecked != task.checklist.get(position).getCompleted()) {
                TaskSaveEvent event = new TaskSaveEvent();
                task.checklist.get(position).setCompleted(isChecked);
                event.task = task;
                EventBus.getDefault().post(event);
            }
        }
    }
}
