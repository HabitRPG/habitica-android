package com.habitrpg.android.habitica.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.data5tream.emojilib.EmojiEditText;
import com.github.data5tream.emojilib.EmojiGridView;
import com.github.data5tream.emojilib.EmojiPopup;
import com.github.data5tream.emojilib.emoji.Emojicon;
import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.events.TaskSaveEvent;
import com.habitrpg.android.habitica.events.commands.DeleteTaskCommand;
import com.habitrpg.android.habitica.ui.WrapContentRecyclerViewLayoutManager;
import com.habitrpg.android.habitica.ui.adapter.tasks.CheckListAdapter;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.habitrpg.android.habitica.ui.helpers.SimpleItemTouchHelperCallback;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;
import com.magicmicky.habitrpgwrapper.lib.models.Tag;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.ChecklistItem;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Days;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.Task;
import com.magicmicky.habitrpgwrapper.lib.models.tasks.TaskTag;
import com.raizlabs.android.dbflow.runtime.transaction.BaseTransaction;
import com.raizlabs.android.dbflow.runtime.transaction.TransactionListener;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import butterknife.Bind;


public class TaskFormActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
    public static final String TASK_ID_KEY = "taskId";
    public static final String TASK_TYPE_KEY = "type";
    public static final String TAG_IDS_KEY = "tagsId";
    public static final String TAG_NAMES_KEY = "tagsName";
    public static final String ALLOCATION_MODE_KEY = "allocationModeKey";

    private String taskType;
    private String taskId;
    private Task task;

    private String allocationMode;
    private List<CheckBox> weekdayCheckboxes = new ArrayList<>();
    private NumberPicker frequencyPicker;
    private List<String> tags;
    private List<String> tagsName; //added this
    private CheckListAdapter checklistAdapter;
    private List<CharSequence> userSelectedTags;
    private List<CheckBox> allTags;
    private List<String> userSelectedTagIds;

    @Bind(R.id.task_value_edittext)
    EmojiEditText taskValue;

    @Bind(R.id.task_value_layout)
    TextInputLayout taskValueLayout;

    @Bind(R.id.task_checklist_wrapper)
    LinearLayout checklistWrapper;

    @Bind(R.id.task_startdate_picker)
    DatePicker startDatePicker;

    @Bind(R.id.task_difficulty_wrapper)
    LinearLayout difficultyWrapper;

    @Bind(R.id.task_attribute_wrapper)
    LinearLayout attributeWrapper;

    @Bind(R.id.task_main_wrapper)
    LinearLayout mainWrapper;

    @Bind(R.id.task_text_edittext)
    EmojiEditText taskText;

    @Bind(R.id.task_notes_edittext)
    EmojiEditText taskNotes;

    @Bind(R.id.task_difficulty_spinner)
    Spinner taskDifficultySpinner;

    @Bind(R.id.task_attribute_spinner)
    Spinner taskAttributeSpinner;

    @Bind(R.id.btn_delete_task)
    Button btnDelete;

    @Bind(R.id.task_startdate_layout)
    LinearLayout startDateLayout;

    @Bind(R.id.task_task_wrapper)
    LinearLayout taskWrapper;

    @Bind(R.id.task_positive_checkbox)
    CheckBox positiveCheckBox;

    @Bind(R.id.task_negative_checkbox)
    CheckBox negativeCheckBox;

    @Bind(R.id.task_actions_wrapper)
    LinearLayout actionsLayout;

    @Bind(R.id.task_weekdays_wrapper)
    LinearLayout weekdayWrapper;

    @Bind(R.id.task_frequency_spinner)
    Spinner dailyFrequencySpinner;

    @Bind(R.id.task_frequency_container)
    LinearLayout frequencyContainer;

    @Bind(R.id.checklist_recycler_view)
    RecyclerView recyclerView;

    @Bind(R.id.new_checklist)
    EmojiEditText newCheckListEditText;

    @Bind(R.id.add_checklist_button)
    Button button;

    @Bind(R.id.emoji_toggle_btn0)
    ImageButton emojiToggle0;

    @Bind(R.id.emoji_toggle_btn1)
    ImageButton emojiToggle1;


    ImageButton emojiToggle2;
    @Bind(R.id.task_duedate_layout)
    LinearLayout dueDateLayout;

    @Bind(R.id.duedate_checkbox)
    CheckBox dueDateCheckBox;

    @Bind(R.id.task_duedate_picker)
    DatePicker dueDatePicker;

    @Bind(R.id.task_tags_wrapper)
    LinearLayout tagsWrapper;

    @Bind(R.id.task_tags_checklist)
    LinearLayout tagsContainerLinearLayout;

    EmojiPopup popup;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_task_form;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        taskType = bundle.getString(TASK_TYPE_KEY);
        taskId = bundle.getString(TASK_ID_KEY);
        tags = bundle.getStringArrayList(TAG_IDS_KEY);
        tagsName = bundle.getStringArrayList(TAG_NAMES_KEY);
        allocationMode = bundle.getString(ALLOCATION_MODE_KEY);
        userSelectedTags = new ArrayList<CharSequence>();
        allTags = new ArrayList<CheckBox>();
        userSelectedTagIds = new ArrayList<String>();
        if (taskType == null) {
            return;
        }

        btnDelete.setEnabled(false);
        ViewHelper.SetBackgroundTint(btnDelete, ContextCompat.getColor(this, R.color.worse_10));
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(view.getContext())
                        .setTitle(getString(R.string.taskform_delete_title))
                        .setMessage(getString(R.string.taskform_delete_message)).setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (task != null) {
                            task.delete();
                        }

                        finish();
                        dismissKeyboard();

                        EventBus.getDefault().post(new DeleteTaskCommand(taskId));
                    }
                }).setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            }
        });

        ArrayAdapter<CharSequence> difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_difficulties, android.R.layout.simple_spinner_item);
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskDifficultySpinner.setAdapter(difficultyAdapter);
        taskDifficultySpinner.setSelection(1);

        ArrayAdapter<CharSequence> attributeAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_attributes, android.R.layout.simple_spinner_item);
        attributeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        taskAttributeSpinner.setAdapter(attributeAdapter);
        taskAttributeSpinner.setSelection(0);

        //Filling in the tags check boxes
        //If tags list is empty, we don't allow user to select a tag.
        //If they have tags, we allow them to add tags
        if(tags.isEmpty())
        {
            mainWrapper.removeView(tagsWrapper);
        }
        else {
            createTagsCheckBoxes();
        }

        if (TextUtils.isEmpty(allocationMode) || !allocationMode.equals("taskbased")){
            attributeWrapper.setVisibility(View.GONE);
        }

        if (taskType.equals("habit")) {
            taskWrapper.removeView(startDateLayout);

            mainWrapper.removeView(checklistWrapper);

            positiveCheckBox.setChecked(true);
            negativeCheckBox.setChecked(true);
        } else {
            mainWrapper.removeView(actionsLayout);
        }

        if (taskType.equals("daily")) {
            ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(this,
                    R.array.daily_frequencies, android.R.layout.simple_spinner_item);
            frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            this.dailyFrequencySpinner.setAdapter(frequencyAdapter);
            this.dailyFrequencySpinner.setOnItemSelectedListener(this);
        } else {
            mainWrapper.removeView(weekdayWrapper);
            mainWrapper.removeView(startDateLayout);
        }

        if (taskType.equals("todo")) {
            dueDateLayout.removeView(dueDatePicker);
            //Allows user to decide if they want to add a due date or not
            dueDateCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isChecked()) {
                        dueDateLayout.addView(dueDatePicker);
                    } else {
                        dueDateLayout.removeView(dueDatePicker);
                    }
                }
            });
        }else{
            mainWrapper.removeView(dueDateLayout);
        }

        if (!taskType.equals("reward")) {
            taskValueLayout.setVisibility(View.GONE);
        } else {

            mainWrapper.removeView(checklistWrapper);

            difficultyWrapper.setVisibility(View.GONE);
            attributeWrapper.setVisibility(View.GONE);
        }

        if (taskId != null) {
            Task task = new Select().from(Task.class).byIds(taskId).querySingle();
            this.task = task;
            if(task != null){
                populate(task);
            }

            setTitle(task);

            btnDelete.setEnabled(true);
        } else {
            setTitle((Task) null);
            taskText.requestFocus();
        }

        if (taskType.equals("todo") || taskType.equals("daily")) {
            createCheckListRecyclerView();
        }

        // Emoji keyboard stuff
        boolean isTodo = false;
        if (taskType.equals("todo")) {
            isTodo = true;
        }

        // If it's a to-do, change the emojiToggle2 to the actual emojiToggle2 (prevents NPEs when not a to-do task)
        if (isTodo) {
            emojiToggle2 = (ImageButton) findViewById(R.id.emoji_toggle_btn2);
        } else {
            emojiToggle2 = emojiToggle0;
        }

        popup = new EmojiPopup(emojiToggle0.getRootView(), this, ContextCompat.getColor(this, R.color.brand));

        popup.setSizeForSoftKeyboard();
        popup.setOnDismissListener(new PopupWindow.OnDismissListener() {

            @Override
            public void onDismiss() {
                changeEmojiKeyboardIcon(false);
            }
        });
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing())
                    popup.dismiss();
            }
        });

        popup.setOnEmojiconClickedListener(new EmojiGridView.OnEmojiconClickedListener() {

            @Override
            public void onEmojiconClicked(Emojicon emojicon) {
                EmojiEditText emojiEditText = null;
                if (getCurrentFocus() == null || !isEmojiEditText(getCurrentFocus()) || emojicon == null) {
                    return;
                } else {
                    emojiEditText = (EmojiEditText) getCurrentFocus();
                }
                int start = emojiEditText.getSelectionStart();
                int end = emojiEditText.getSelectionEnd();
                if (start < 0) {
                    emojiEditText.append(emojicon.getEmoji());
                } else {
                    emojiEditText.getText().replace(Math.min(start, end),
                            Math.max(start, end), emojicon.getEmoji(), 0,
                            emojicon.getEmoji().length());
                }
            }
        });

        popup.setOnEmojiconBackspaceClickedListener(new EmojiPopup.OnEmojiconBackspaceClickedListener() {

            @Override
            public void onEmojiconBackspaceClicked(View v) {
                if (isEmojiEditText(getCurrentFocus())) {
                    KeyEvent event = new KeyEvent(
                            0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                    getCurrentFocus().dispatchKeyEvent(event);
                }
            }
        });

        emojiToggle0.setOnClickListener(new emojiClickListener(taskText));
        emojiToggle1.setOnClickListener(new emojiClickListener(taskNotes));
        if (isTodo) {
            emojiToggle2.setOnClickListener(new emojiClickListener(newCheckListEditText));
        }
    }

    private boolean isEmojiEditText(View view) {
        return view instanceof EmojiEditText;
    }

    private void changeEmojiKeyboardIcon(Boolean keyboardOpened) {

        if (keyboardOpened) {
            emojiToggle0.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_keyboard_grey600_24dp));
            emojiToggle1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_keyboard_grey600_24dp));
            emojiToggle2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_keyboard_grey600_24dp));
        } else {
            emojiToggle0.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_emoticon_grey600_24dp));
            emojiToggle1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_emoticon_grey600_24dp));
            emojiToggle2.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_emoticon_grey600_24dp));
        }
    }

    private class emojiClickListener implements View.OnClickListener {

        EmojiEditText view;

        public emojiClickListener(EmojiEditText view) {
            this.view = view;
        }

        @Override
        public void onClick(View v) {
            if(!popup.isShowing()){

                if(popup.isKeyBoardOpen()){
                    popup.showAtBottom();
                    changeEmojiKeyboardIcon(true);
                }

                else{
                    view.setFocusableInTouchMode(true);
                    view.requestFocus();
                    popup.showAtBottomPending();
                    final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                    changeEmojiKeyboardIcon(true);
                }
            }

            else{
                popup.dismiss();
                changeEmojiKeyboardIcon(false);
            }
        }
    }

    private void createCheckListRecyclerView() {
        List<ChecklistItem> checklistItems = new ArrayList<>();
        if (task != null && task.getChecklist() != null) {
            checklistItems = task.getChecklist();
        }
        checklistAdapter = new CheckListAdapter(checklistItems);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(checklistAdapter);

        recyclerView.setLayoutManager(new WrapContentRecyclerViewLayoutManager(this));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(checklistAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String checklist = newCheckListEditText.getText().toString();
                ChecklistItem item = new ChecklistItem(checklist);
                checklistAdapter.addItem(item);
                newCheckListEditText.setText("");
            }
        });
    }


    private void createTagsCheckBoxes() {
        for(int i = 0; i < tagsName.size(); i++){
            TableRow row = new TableRow(tagsContainerLinearLayout.getContext());
            row.setId(i);
            CheckBox tagsCheckBox = new CheckBox(tagsContainerLinearLayout.getContext());
            tagsCheckBox.setText(tagsName.get(i)); // set text Name
            tagsCheckBox.setId(i);
            //This is to check if the tag was selected by the user. Similar to onClickListener
            tagsCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (buttonView.isChecked()) {
                        userSelectedTags.add(buttonView.getText());
                    } else {
                        userSelectedTags.remove(buttonView.getText());
                    }
                }
            });
            row.addView(tagsCheckBox);
            tagsContainerLinearLayout.addView(row);
            allTags.add(tagsCheckBox);
        }


    }


    private void setTitle(Task task) {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {

            String title = "";

            if (task != null) {
                title = getResources().getString(R.string.action_edit) + " " + task.getText();
            } else {
                switch (taskType) {
                    case "todo":
                        title = getResources().getString(R.string.new_todo);
                        break;
                    case "daily":
                        title = getResources().getString(R.string.new_daily);
                        break;
                    case "habit":
                        title = getResources().getString(R.string.new_habit);
                        break;
                    case "reward":
                        title = getResources().getString(R.string.new_reward);
                        break;
                }
            }

            actionBar.setTitle(title);
        }
    }

    private void setDailyFrequencyViews() {
        this.frequencyContainer.removeAllViews();
        if (this.dailyFrequencySpinner.getSelectedItemPosition() == 0) {
            String[] weekdays = getResources().getStringArray(R.array.weekdays);
            for (int i = 0; i < 7; i++) {
                View weekdayRow = getLayoutInflater().inflate(R.layout.row_checklist, null);
                TextView tv = (TextView) weekdayRow.findViewById(R.id.label);
                CheckBox checkbox = (CheckBox) weekdayRow.findViewById(R.id.checkbox);
                checkbox.setChecked(true);
                this.weekdayCheckboxes.add(checkbox);
                tv.setText(weekdays[i]);
                this.frequencyContainer.addView(weekdayRow);
            }
        } else {
            View dayRow = getLayoutInflater().inflate(R.layout.row_number_picker, null);
            this.frequencyPicker = (NumberPicker) dayRow.findViewById(R.id.numberPicker);
            this.frequencyPicker.setMinValue(1);
            this.frequencyPicker.setMaxValue(366);
            TextView tv = (TextView) dayRow.findViewById(R.id.label);
            tv.setText(getResources().getString(R.string.frequency_daily));
            this.frequencyContainer.addView(dayRow);
        }

        if (this.task != null) {

            if (this.dailyFrequencySpinner.getSelectedItemPosition() == 0) {
                this.weekdayCheckboxes.get(0).setChecked(this.task.getRepeat().getM());
                this.weekdayCheckboxes.get(1).setChecked(this.task.getRepeat().getT());
                this.weekdayCheckboxes.get(2).setChecked(this.task.getRepeat().getW());
                this.weekdayCheckboxes.get(3).setChecked(this.task.getRepeat().getTh());
                this.weekdayCheckboxes.get(4).setChecked(this.task.getRepeat().getF());
                this.weekdayCheckboxes.get(5).setChecked(this.task.getRepeat().getS());
                this.weekdayCheckboxes.get(6).setChecked(this.task.getRepeat().getSu());
            } else {
                this.frequencyPicker.setValue(this.task.getEveryX());
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_task_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_save_changes) {
            finishActivitySuccessfuly();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populate(Task task) {
        taskText.setText(task.text);
        taskNotes.setText(task.notes);
        taskValue.setText(String.format("%.0f", task.value));

        for(TaskTag tt : task.getTags()){
            for(CheckBox box : allTags){
                int tagNameLocation = tags.indexOf(tt.getTag().getId());
                if(tagsName.get(tagNameLocation) == box.getText()){
                    box.setChecked(true);
                }
            }
        }

        float priority = task.getPriority();
        if (Math.abs(priority - 0.1) < 0.000001) {
            this.taskDifficultySpinner.setSelection(0);
        } else if (Math.abs(priority - 1.0) < 0.000001) {
            this.taskDifficultySpinner.setSelection(1);
        } else if (Math.abs(priority - 1.5) < 0.000001) {
            this.taskDifficultySpinner.setSelection(2);
        } else if (Math.abs(priority - 2.0) < 0.000001) {
            this.taskDifficultySpinner.setSelection(3);
        }

        String attribute = task.getAttribute();
        if (attribute != null){
            switch (attribute){
                case Task.ATTRIBUTE_STRENGTH:
                    taskAttributeSpinner.setSelection(0);
                    break;
                case Task.ATTRIBUTE_INTELLIGENCE:
                    taskAttributeSpinner.setSelection(1);
                    break;
                case Task.ATTRIBUTE_CONSTITUTION:
                    taskAttributeSpinner.setSelection(2);
                    break;
                case Task.ATTRIBUTE_PERCEPTION:
                    taskAttributeSpinner.setSelection(3);
                    break;
            }
        }

        if (task.type.equals("habit")) {
            positiveCheckBox.setChecked(task.getUp());
            negativeCheckBox.setChecked(task.getDown());
        }

        if (task.type.equals("daily")) {

            if (task.getStartDate() != null) {
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(task.getStartDate());
            startDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            }

            if (task.getFrequency().equals("weekly")) {
                this.dailyFrequencySpinner.setSelection(0);
                if (weekdayCheckboxes.size() == 7) {
                    this.weekdayCheckboxes.get(0).setChecked(task.getRepeat().getM());
                    this.weekdayCheckboxes.get(1).setChecked(task.getRepeat().getT());
                    this.weekdayCheckboxes.get(2).setChecked(task.getRepeat().getW());
                    this.weekdayCheckboxes.get(3).setChecked(task.getRepeat().getTh());
                    this.weekdayCheckboxes.get(4).setChecked(task.getRepeat().getF());
                    this.weekdayCheckboxes.get(5).setChecked(task.getRepeat().getS());
                    this.weekdayCheckboxes.get(6).setChecked(task.getRepeat().getSu());
                }
            } else {
                this.dailyFrequencySpinner.setSelection(1);
                if (this.frequencyPicker != null) {
                    this.frequencyPicker.setValue(task.getEveryX());
                }
            }
        }

        if (task.type.equals("todo")) {
            if (task.getDueDate() != null) {
                dueDateCheckBox.setChecked(true);
                Calendar calendar = new GregorianCalendar();
                calendar.setTime(task.getDueDate());
                dueDatePicker.updateDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            }
        }

    }

    private boolean saveTask(Task task) {
        task.text = MarkdownParser.parseCompiled(taskText.getText());

        if (checklistAdapter != null) {
            if (checklistAdapter.getCheckListItems() != null) {
                task.setChecklist(checklistAdapter.getCheckListItems());
            }
        }

        if (task.text.isEmpty())
            return false;

        task.notes = MarkdownParser.parseCompiled(taskNotes.getText());


        //To add tags, I must first make a TaskTag, then grab information on which Tag they want to associate with the task
        //After that, I need to make a List<TaskTag> so I can do task.setTags(List<TaskTag>)
        if(!userSelectedTags.isEmpty()){
            List<TaskTag> selectedTags = new ArrayList<TaskTag>();
            for(CharSequence names : userSelectedTags){
                TaskTag addMeToList = new TaskTag();
                int tagIdLocation = tagsName.indexOf(names);

                //Creates a Tag, so we can associate it with the task.
                Tag pickedTag = new Tag(tags.get(tagIdLocation),tagsName.get(tagIdLocation));

                //Set the selected Tag and the new Task to the TaskTag Object
                addMeToList.setTag(pickedTag);
                addMeToList.setTask(task);

                selectedTags.add(addMeToList);
                userSelectedTagIds.add(pickedTag.getId()); //used for the SQL command
            }
            task.setTags(selectedTags);
        }

        if (this.taskDifficultySpinner.getSelectedItemPosition() == 0) {
            task.setPriority((float) 0.1);
        } else if (this.taskDifficultySpinner.getSelectedItemPosition() == 1) {
            task.setPriority((float) 1.0);
        } else if (this.taskDifficultySpinner.getSelectedItemPosition() == 2) {
            task.setPriority((float) 1.5);
        } else if (this.taskDifficultySpinner.getSelectedItemPosition() == 3) {
            task.setPriority((float) 2.0);
        }

        if (TextUtils.isEmpty(allocationMode) || !allocationMode.equals("taskbased")){
            task.setAttribute(Task.ATTRIBUTE_STRENGTH);
        }else {
            switch (this.taskAttributeSpinner.getSelectedItemPosition()){
                case 0:
                    task.setAttribute(Task.ATTRIBUTE_STRENGTH);
                    break;
                case 1:
                    task.setAttribute(Task.ATTRIBUTE_INTELLIGENCE);
                    break;
                case 2:
                    task.setAttribute(Task.ATTRIBUTE_CONSTITUTION);
                    break;
                case 3:
                    task.setAttribute(Task.ATTRIBUTE_PERCEPTION);
                    break;
            }
        }

        switch (task.type) {
            case "habit": {
                task.setUp(positiveCheckBox.isChecked());
                task.setDown(negativeCheckBox.isChecked());
            }
            break;

            case "daily": {
                GregorianCalendar calendar = new GregorianCalendar();
                calendar.set(startDatePicker.getYear(), startDatePicker.getMonth(), startDatePicker.getDayOfMonth());
                task.setStartDate(new Date(calendar.getTimeInMillis()));

                if (this.dailyFrequencySpinner.getSelectedItemPosition() == 0) {
                    task.setFrequency("weekly");
                    Days repeat = task.getRepeat();
                    if (repeat == null) {
                        repeat = new Days();
                        task.setRepeat(repeat);
                    }

                    repeat.setM(this.weekdayCheckboxes.get(0).isChecked());
                    repeat.setT(this.weekdayCheckboxes.get(1).isChecked());
                    repeat.setW(this.weekdayCheckboxes.get(2).isChecked());
                    repeat.setTh(this.weekdayCheckboxes.get(3).isChecked());
                    repeat.setF(this.weekdayCheckboxes.get(4).isChecked());
                    repeat.setS(this.weekdayCheckboxes.get(5).isChecked());
                    repeat.setSu(this.weekdayCheckboxes.get(6).isChecked());
                } else {
                    task.setFrequency("daily");
                    task.setEveryX(this.frequencyPicker.getValue());
                }
            }
            break;

            case "todo":{
                if(dueDateCheckBox.isChecked()) {
                    Calendar calendar = new GregorianCalendar();
                    calendar.set(dueDatePicker.getYear(), dueDatePicker.getMonth(), dueDatePicker.getDayOfMonth());
                    task.setDueDate(new Date(calendar.getTimeInMillis()));
                }else{
                    task.setDueDate(null);
                }
            }
            break;

            case "reward": {
                String value = taskValue.getText().toString();
                if(!value.isEmpty()){
                    task.setValue(Double.parseDouble(value));
                }else{
                    task.setValue(0.0d);
                }

            }
            break;
        }

        return true;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        this.setDailyFrequencyViews();
    }

    public void onNothingSelected(AdapterView<?> parent) {
        this.setDailyFrequencyViews();
    }

    private void prepareSave() {
        if (this.task == null) {
            this.task = new Task();
            this.task.setType(taskType);
        }
        if (this.saveTask(this.task)) {
            this.task.save();
            new Select()
                    .from(Tag.class)
                    .where(Condition.column("id").in("", userSelectedTagIds.toArray())).async().queryList(tagsSearchingListener);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        dismissKeyboard();
        return true;
    }

    @Override
    public void onBackPressed() {
        finish();
        dismissKeyboard();
    }

    private void finishActivitySuccessfuly() {
        this.prepareSave();
        finish();
        dismissKeyboard();
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    private TransactionListener<List<Tag>> tagsSearchingListener = new TransactionListener<List<Tag>>() {
        @Override
        public void onResultReceived(List<Tag> tags) {
            //UI thread.
            List<TaskTag> taskTags = new ArrayList<>();
            for (Tag tag : tags) {
                TaskTag tt = new TaskTag();
                tt.setTag(tag);
                tt.setTask(task);
                taskTags.add(tt);
            }
            //save
            TaskFormActivity.this.task.setTags(taskTags);
            TaskFormActivity.this.task.update();
            //send back to other elements.
            TaskSaveEvent event = new TaskSaveEvent();
            if (TaskFormActivity.this.task.getId() == null) {
                event.created = true;
            }

            event.task = TaskFormActivity.this.task;
            EventBus.getDefault().post(event);

        }

        @Override
        public boolean onReady(BaseTransaction<List<Tag>> baseTransaction) {
            return true;
        }

        @Override
        public boolean hasResult(BaseTransaction<List<Tag>> baseTransaction, List<Tag> tags) {
            return true;
        }
    };
}
