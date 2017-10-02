package com.habitrpg.android.habitica.ui.views.tasks;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.TagRepository;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TaskFilterDialog extends AlertDialog implements RadioGroup.OnCheckedChangeListener {

    @Inject
    TagRepository repository;

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

    @BindView(R.id.tags_title)
    TextView tagsTitleView;
    @BindView(R.id.tag_edit_button)
    Button tagsEditButton;
    @BindView(R.id.tags_list)
    LinearLayout tagsList;
    private String taskType;
    private OnFilterCompletedListener listener;

    private String filterType;
    private List<Tag> tags;
    private List<String> activeTags;
    private Map<String, Tag> editedTags = new HashMap<>();
    private Map<String, Tag> createdTags = new HashMap<>();
    private List<String> deletedTags = new ArrayList<>();

    private Drawable addIcon;
    private boolean isEditing;

    public TaskFilterDialog(Context context, AppComponent component) {
        super(context);
        component.inject(this);
        addIcon = ContextCompat.getDrawable(context, R.drawable.ic_add_purple_300_36dp);

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.dialog_task_filter, null);

        ButterKnife.bind(this, view);

        taskFilters.setOnCheckedChangeListener(this);

        setTitle(R.string.filters);
        setView(view);
        this.setButton(AlertDialog.BUTTON_POSITIVE, context.getString(R.string.done), (dialog, which) -> {
            if (isEditing) {
                stopEditing();
            }
            if (listener != null) {
                listener.onFilterCompleted(filterType, activeTags);
            }
            this.dismiss();
        });

        setButton(AlertDialog.BUTTON_NEUTRAL, getContext().getString(R.string.clear), (dialog, which) -> {
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Button clearButton = getButton(AlertDialog.BUTTON_NEUTRAL);
        if (clearButton != null) {
            clearButton.setOnClickListener(view1 -> {
                if (isEditing) {
                    stopEditing();
                }
                setActiveFilter(null);
                setActiveTags(null);
            });
            clearButton.setEnabled(hasActiveFilters());
        }
    }

    @Override
    public void show() {
        super.show();
        if (this.getWindow() != null) {
            this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
    }

    public void setTags(List<Tag> tags) {
        this.tags = repository.getUnmanagedCopy(tags);
        createTagViews();
    }

    private void createTagViews() {
        ColorStateList colorStateList = new ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, //disabled
                        new int[]{android.R.attr.state_checked} //enabled
                },
                new int[] {
                        Color.LTGRAY, //disabled
                        ContextCompat.getColor(getContext(), R.color.brand_400) //enabled
                }
        );
        int leftPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getContext().getResources().getDisplayMetrics());
        int verticalPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics());
        for (Tag tag : tags) {
            AppCompatCheckBox tagCheckbox = new AppCompatCheckBox(getContext());
            tagCheckbox.setText(tag.getName());
            tagCheckbox.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
            tagCheckbox.setPadding(tagCheckbox.getPaddingLeft()+ leftPadding,
                    verticalPadding,
                    tagCheckbox.getPaddingRight(),
                    verticalPadding);
            tagCheckbox.setTextColor(ContextCompat.getColor(getContext(), R.color.textColorLight));
            CompoundButtonCompat.setButtonTintList(tagCheckbox, colorStateList);
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
                filtersChanged();
            });
            tagsList.addView(tagCheckbox);
        }
        createAddTagButton();
    }

    private void createAddTagButton() {
        Button button = new Button(getContext());
        button.setText(R.string.add_tag);
        button.setOnClickListener(v -> createTag());
        button.setCompoundDrawablesWithIntrinsicBounds(addIcon, null, null, null);
        button.setBackgroundResource(R.drawable.layout_rounded_bg_lighter_gray);
        button.setTextColor(ContextCompat.getColor(getContext(), R.color.text_light));
        tagsList.addView(button);
    }

    private void createTag() {
        Tag tag = new Tag();
        tag.id = UUID.randomUUID().toString();
        tags.add(tag);
        createdTags.put(tag.getId(), tag);
        startEditing();
    }

    private void startEditing() {
        isEditing = true;
        tagsList.removeAllViews();
        createTagEditViews();
        tagsEditButton.setText(R.string.done);
        if (this.getWindow() != null) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }
    }

    private void stopEditing() {
        isEditing = false;
        tagsList.removeAllViews();
        createTagViews();
        tagsEditButton.setText(R.string.edit_tag_btn_edit);
        if (this.getWindow() != null) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        repository.updateTags(editedTags.values()).subscribe(tag -> editedTags.remove(tag.getId()), RxErrorHandler.handleEmptyError());
        repository.createTags(createdTags.values()).subscribe(tag -> createdTags.remove(tag.getId()), RxErrorHandler.handleEmptyError());
        repository.deleteTags(deletedTags).subscribe(tags1 -> deletedTags.clear(), RxErrorHandler.handleEmptyError());
    }

    private void createTagEditViews() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        if (tags != null) {
            for (int index = 0; index < tags.size(); index++) {
                Tag tag = tags.get(index);
                createTagEditView(inflater, index, tag);
            }
        }
        createAddTagButton();
    }

    private void createTagEditView(LayoutInflater inflater, int index, Tag tag) {
        LinearLayout wrapper = (LinearLayout) inflater.inflate(R.layout.edit_tag_item, tagsList, false);
        EditText tagEditText = (EditText) wrapper.findViewById(R.id.edit_text);
        tagEditText.setText(tag.getName());
        tagEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (index >= tags.size()) {
                    return;
                }
                Tag tag = tags.get(index);
                tag.setName(s.toString());
                if (createdTags.containsKey(tag.getId())) {
                    createdTags.put(tag.getId(), tag);
                } else {
                    editedTags.put(tag.getId(), tag);
                }
                tags.set(index, tag);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        Button deleteButton = (Button) wrapper.findViewById(R.id.delete_button);
        deleteButton.setOnClickListener(v -> {
            deletedTags.add(tag.getId());
            if (createdTags.containsKey(tag.getId())) {
                createdTags.remove(tag.getId());
            }
            if (editedTags.containsKey(tag.getId())) {
                editedTags.remove(tag.getId());
            }
            tags.remove(tag);
            tagsList.removeView(wrapper);
        });
        tagsList.addView(wrapper);
    }

    public void setActiveTags(@Nullable List<String> tagIds) {
        if (tagIds == null) {
            this.activeTags.clear();
        } else {
            this.activeTags = tagIds;
        }
        for (int index = 0; index < tagsList.getChildCount()-1; index++) {
            ((AppCompatCheckBox)tagsList.getChildAt(index)).setChecked(false);
        }
        for (String tagId : this.activeTags) {
            int index = indexForId(tagId);
            if (index >= 0) {
                ((CheckBox)tagsList.getChildAt(index)).setChecked(true);
            }
        }
        filtersChanged();
    }

    private int indexForId(String tagId) {
        for (int index = 0; index < tags.size(); index++) {
            if (tagId.equals(tags.get(index).id)) {
                return index;
            }
        }
        return -1;
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

    private void setActiveFilter(@Nullable String activeFilter) {
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
        filtersChanged();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (taskType == null) {
            return;
        }
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
        filtersChanged();
    }

    @OnClick(R.id.tag_edit_button)
    void editButtonClicked() {
        isEditing = !isEditing;
        if (isEditing) {
            startEditing();
        } else {
            stopEditing();
        }
    }

    private void filtersChanged() {
        Button clearButton = getButton(AlertDialog.BUTTON_NEUTRAL);
        if (clearButton != null) {
            clearButton.setEnabled(hasActiveFilters());
        }
     }

    private boolean hasActiveFilters() {
        return taskFilters.getCheckedRadioButtonId() != R.id.all_task_filter || activeTags.size() > 0;
    }

    public void setListener(OnFilterCompletedListener listener) {
        this.listener = listener;
    }

    public interface OnFilterCompletedListener {

        void onFilterCompleted(String activeTaskFilter, List<String> activeTags);
    }
}
