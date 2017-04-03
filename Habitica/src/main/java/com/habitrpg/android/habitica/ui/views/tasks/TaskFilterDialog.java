package com.habitrpg.android.habitica.ui.views.tasks;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.IdRes;
import android.support.annotation.StringDef;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TaskFilterDialog extends AlertDialog implements RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.task_type_title)
    TextView taskTypeTitle;

    @BindView(R.id.task_filter_wrapper)
    RadioGroup taskFilters;
    @BindView(R.id.all_task_filter)
    RadioButton allTaskFilter;
    @BindView(R.id.second_task_filter)
    RadioButton secondTaskFilter;
    @BindView(R.id.third_task_filter)
    RadioButton thirdTaskFilter;

    @BindView(R.id.tags_list)
    LinearLayout tagsList;
    private String taskType;
    private OnFilterCompletedListener listener;

    private String filterType;
    private List<String> tagIds;
    private List<String> activeTags;

    public TaskFilterDialog(Context context) {
        super(context);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_task_filter, null);

        ButterKnife.bind(this, view);

        taskFilters.setOnCheckedChangeListener(this);

        setTitle(R.string.filters);
        setView(view);
        this.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.close), (dialog, which) -> {
            if (listener != null) {
                listener.onFilterCompleted(filterType, activeTags);
            }
            this.dismiss();
        });
    }

    public void setTags(List<Tag> tags) {
        tagIds = new ArrayList<>();
        for (Tag tag : tags) {
            tagIds.add(tag.getId());
            CheckBox tagCheckbox = new CheckBox(getContext());
            tagCheckbox.setText(tag.getName());
            tagCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    if (!activeTags.contains(tag.getId())) {
                        activeTags.add(tag.getId());
                    }
                } else {
                    if (activeTags.contains(tag.getId())) {
                        activeTags.remove(tag.getId());
                    }
                }
            });
            tagsList.addView(tagCheckbox);
        }
    }

    public void setActiveTags(List<String> tagIds) {
        this.activeTags = tagIds;
        for (String tagId : tagIds) {
            ((CheckBox)tagsList.getChildAt(this.tagIds.indexOf(tagId))).setChecked(true);
        }
    }

    public void setTaskType(String taskType, String activeFilter) {
        this.taskType = taskType;
        switch (taskType) {
            case Task.TYPE_HABIT: {
                taskTypeTitle.setText(R.string.habits);
                allTaskFilter.setText(R.string.all);
                secondTaskFilter.setText(R.string.weak);
                thirdTaskFilter.setText(R.string.strong);
                break;
            }
            case Task.TYPE_DAILY: {
                taskTypeTitle.setText(R.string.dailies);
                allTaskFilter.setText(R.string.all);
                secondTaskFilter.setText(R.string.active);
                thirdTaskFilter.setText(R.string.gray);
                break;
            }
            case Task.TYPE_TODO: {
                taskTypeTitle.setText(R.string.todos);
                allTaskFilter.setText(R.string.active);
                secondTaskFilter.setText(R.string.dated);
                thirdTaskFilter.setText(R.string.completed);
                break;
            }
        }
        setActiveFilter(activeFilter);
    }

    private void setActiveFilter(String activeFilter) {
        filterType = activeFilter;
        int checkedId = -1;
        if (activeFilter == null) {
            checkedId = R.id.all_task_filter;
        } else {
            switch (activeFilter) {
                case Task.FILTER_ALL:
                    checkedId = R.id.all_task_filter;
                    break;
                case Task.FILTER_WEAK:
                case Task.FILTER_DATED:
                    checkedId = R.id.second_task_filter;
                    break;
                case Task.FILTER_STRONG:
                case Task.FILTER_GRAY:
                case Task.FILTER_COMPLETED:
                    checkedId = R.id.third_task_filter;
                    break;
                case Task.FILTER_ACTIVE:
                    if (taskType.equals(Task.TYPE_DAILY)) {
                        checkedId = R.id.second_task_filter;
                    } else {
                        checkedId = R.id.all_task_filter;
                    }
                    break;
            }
        }
        taskFilters.check(checkedId);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.all_task_filter) {
            if (!taskType.equals(Task.TYPE_TODO)) {
                filterType = Task.FILTER_ALL;
            } else {
                filterType = Task.FILTER_ACTIVE;
            }
        } else if (checkedId == R.id.second_task_filter) {
            switch (taskType) {
                case Task.TYPE_HABIT:
                    filterType = Task.FILTER_WEAK;
                    break;
                case Task.FREQUENCY_DAILY:
                    filterType = Task.FILTER_ACTIVE;
                    break;
                case Task.TYPE_TODO:
                    filterType = Task.FILTER_DATED;
                    break;
            }
        } else if (checkedId == R.id.third_task_filter) {
            switch (taskType) {
                case Task.TYPE_HABIT:
                    filterType = Task.FILTER_STRONG;
                    break;
                case Task.FREQUENCY_DAILY:
                    filterType = Task.FILTER_GRAY;
                    break;
                case Task.TYPE_TODO:
                    filterType = Task.FILTER_COMPLETED;
                    break;
            }
        }
    }

    public void setListener(OnFilterCompletedListener listener) {
        this.listener = listener;
    }

    public interface OnFilterCompletedListener {

        void onFilterCompleted(String activeTaskFilter, List<String> activeTags);
    }
}
