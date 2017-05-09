package com.habitrpg.android.habitica.ui.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.habitrpg.android.habitica.R;
import com.habitrpg.android.habitica.components.AppComponent;
import com.habitrpg.android.habitica.data.TagRepository;
import com.habitrpg.android.habitica.data.TaskRepository;
import com.habitrpg.android.habitica.helpers.FirstDayOfTheWeekHelper;
import com.habitrpg.android.habitica.helpers.RemoteConfigManager;
import com.habitrpg.android.habitica.helpers.RxErrorHandler;
import com.habitrpg.android.habitica.helpers.RemindersManager;
import com.habitrpg.android.habitica.helpers.TaskFilterHelper;
import com.habitrpg.android.habitica.models.Tag;
import com.habitrpg.android.habitica.models.tasks.ChecklistItem;
import com.habitrpg.android.habitica.models.tasks.Days;
import com.habitrpg.android.habitica.models.tasks.RemindersItem;
import com.habitrpg.android.habitica.models.tasks.Task;
import com.habitrpg.android.habitica.modules.AppModule;
import com.habitrpg.android.habitica.ui.WrapContentRecyclerViewLayoutManager;
import com.habitrpg.android.habitica.ui.adapter.tasks.CheckListAdapter;
import com.habitrpg.android.habitica.ui.adapter.tasks.RemindersAdapter;
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser;
import com.habitrpg.android.habitica.ui.helpers.SimpleItemTouchHelperCallback;
import com.habitrpg.android.habitica.ui.helpers.ViewHelper;

import net.pherth.android.emoji_library.EmojiEditText;
import net.pherth.android.emoji_library.EmojiPopup;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.OnClick;
import io.realm.RealmList;
import rx.Observable;

public class TaskFormActivity extends BaseActivity implements AdapterView.OnItemSelectedListener {
    public static final String TASK_ID_KEY = "taskId";
    public static final String USER_ID_KEY = "userId";
    public static final String TASK_TYPE_KEY = "type";
    public static final String SHOW_TAG_SELECTION = "show_tag_selection";
    public static final String ALLOCATION_MODE_KEY = "allocationModeKey";
    public static final String SHOW_CHECKLIST = "show_checklist";

    public static final String PARCELABLE_TASK = "parcelable_task";
    public static final String SAVE_TO_DB = "saveToDb";

    // in order to disable the event handler in MainActivity
    public static final String SET_IGNORE_FLAG = "ignoreFlag";

    @BindView(R.id.task_value_edittext)
    EditText taskValue;
    @BindView(R.id.task_value_layout)
    TextInputLayout taskValueLayout;

    @BindView(R.id.task_checklist_wrapper)
    LinearLayout checklistWrapper;

    @BindView(R.id.task_difficulty_wrapper)
    LinearLayout difficultyWrapper;

    @BindView(R.id.task_attribute_wrapper)
    LinearLayout attributeWrapper;

    @BindView(R.id.task_main_wrapper)
    LinearLayout mainWrapper;

    @BindView(R.id.task_text_edittext)
    EmojiEditText taskText;

    @BindView(R.id.task_notes_edittext)
    EmojiEditText taskNotes;

    @BindView(R.id.task_difficulty_spinner)
    Spinner taskDifficultySpinner;

    @BindView(R.id.task_attribute_spinner)
    Spinner taskAttributeSpinner;

    @BindView(R.id.btn_delete_task)
    Button btnDelete;

    @BindView(R.id.task_startdate_layout)
    LinearLayout startDateLayout;

    @BindView(R.id.task_task_wrapper)
    LinearLayout taskWrapper;

    @BindView(R.id.task_positive_checkbox)
    CheckBox positiveCheckBox;

    @BindView(R.id.task_negative_checkbox)
    CheckBox negativeCheckBox;

    @BindView(R.id.task_actions_wrapper)
    LinearLayout actionsLayout;

    @BindView(R.id.task_weekdays_wrapper)
    LinearLayout weekdayWrapper;

    @BindView(R.id.frequency_title)
    TextView frequencyTitleTextView;

    @BindView(R.id.task_frequency_spinner)
    Spinner dailyFrequencySpinner;

    @BindView(R.id.task_frequency_container)
    LinearLayout frequencyContainer;

    @BindView(R.id.checklist_recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.new_checklist)
    EmojiEditText newCheckListEditText;

    @BindView(R.id.add_checklist_button)
    Button addChecklistItemButton;

    @BindView(R.id.task_reminders_wrapper)
    LinearLayout remindersWrapper;

    @BindView(R.id.new_reminder_edittext)
    EditText newRemindersEditText;

    @BindView(R.id.reminders_recycler_view)
    RecyclerView remindersRecyclerView;

    @BindView(R.id.emoji_toggle_btn0)
    ImageButton emojiToggle0;

    @BindView(R.id.emoji_toggle_btn1)
    ImageButton emojiToggle1;
    ImageButton emojiToggle2;

    @BindView(R.id.task_duedate_layout)
    LinearLayout dueDateLayout;

    @BindView(R.id.task_duedate_picker_layout)
    LinearLayout dueDatePickerLayout;

    @BindView(R.id.duedate_checkbox)
    CheckBox dueDateCheckBox;

    @BindView(R.id.startdate_text_title)
    TextView startDateTitleTextView;
    @BindView(R.id.startdate_text_edittext)
    EditText startDatePickerText;
    @BindView (R.id.repeatables_startdate_text_edittext)
    EditText repeatablesStartDatePickerText;
    DateEditTextListener startDateListener;

    @BindView(R.id.repeatables)
    LinearLayout repeatablesLayout;

    @BindView(R.id.repeatables_on_title)
    TextView reapeatablesOnTextView;
    @BindView(R.id.task_repeatables_on_spinner)
    Spinner repeatablesOnSpinner;

    @BindView(R.id.task_repeatables_every_x_spinner)
    NumberPicker repeatablesEveryXSpinner;

    @BindView(R.id.task_repeatables_frequency_container)
    LinearLayout repeatablesFrequencyContainer;

    @BindView(R.id.summary)
    TextView summaryTextView;

    @BindView(R.id.duedate_text_edittext)
    EditText dueDatePickerText;
    DateEditTextListener dueDateListener;

    @BindView(R.id.task_tags_wrapper)
    LinearLayout tagsWrapper;

    @BindView(R.id.task_tags_checklist)
    LinearLayout tagsContainerLinearLayout;
    @BindView(R.id.task_repeatables_frequency_spinner)
    Spinner repeatablesFrequencySpinner;


    @Inject
    TaskFilterHelper taskFilterHelper;
    @Inject

    TaskRepository taskRepository;
    @Inject
    TagRepository tagRepository;
    @Inject
    @Named(AppModule.NAMED_USER_ID)
    String userId;
    @Inject
    RemoteConfigManager remoteConfigManager;

    private boolean showTagSelection;

    private boolean showChecklist;
    private boolean setIgnoreFlag;
    private Task task;
    private String allocationMode;
    private List<CheckBox> weekdayCheckboxes = new ArrayList<>();
    private List<CheckBox> repeatablesWeekDayCheckboxes = new ArrayList<>();
    private NumberPicker frequencyPicker;
    private List<Tag> tags;
    private CheckListAdapter checklistAdapter;
    private RemindersAdapter remindersAdapter;
    private List<CheckBox> tagCheckBoxList;
    private List<Tag> selectedTags;

    private RemindersManager remindersManager;
    private FirstDayOfTheWeekHelper firstDayOfTheWeekHelper;

    private boolean saveToDb;
    private String taskType;
    private String taskId;
    private EmojiPopup popup;

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
        allocationMode = bundle.getString(ALLOCATION_MODE_KEY);
        showTagSelection = bundle.getBoolean(SHOW_TAG_SELECTION, true);
        showChecklist = bundle.getBoolean(SHOW_CHECKLIST, true);
        allocationMode = bundle.getString(ALLOCATION_MODE_KEY);
        saveToDb = bundle.getBoolean(SAVE_TO_DB, true);
        setIgnoreFlag = bundle.getBoolean(SET_IGNORE_FLAG, false);
        tagCheckBoxList = new ArrayList<>();

        tagsWrapper.setVisibility(showTagSelection ? View.VISIBLE : View.GONE);

        if(bundle.containsKey(PARCELABLE_TASK)){
            task = bundle.getParcelable(PARCELABLE_TASK);
            if (task != null) {
                taskType = task.type;
            }
        }

        tagCheckBoxList = new ArrayList<>();
        selectedTags = new ArrayList<>();
        if (taskType == null) {
            return;
        }

        remindersManager = new RemindersManager(taskType);

        dueDateListener = new DateEditTextListener(dueDatePickerText);
        startDateListener = new DateEditTextListener(startDatePickerText);

        btnDelete.setEnabled(false);
        ViewHelper.SetBackgroundTint(btnDelete, ContextCompat.getColor(this, R.color.worse_10));
        btnDelete.setOnClickListener(view -> new AlertDialog.Builder(view.getContext())
                .setTitle(getString(R.string.taskform_delete_title))
                .setMessage(getString(R.string.taskform_delete_message)).setPositiveButton(getString(R.string.yes), (dialog, which) -> {
                    if (task != null) {
                        taskRepository.deleteTask(task.getId());
                    }

                    finish();
                    dismissKeyboard();

                    taskRepository.deleteTask(taskId).subscribe(aVoid -> {}, RxErrorHandler.handleEmptyError());
                }).setNegativeButton(getString(R.string.no), (dialog, which) -> dialog.dismiss()).show());

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

        if (TextUtils.isEmpty(allocationMode) || !allocationMode.equals("taskbased")) {
            attributeWrapper.setVisibility(View.GONE);
        }

        if (taskType.equals("habit")) {
            taskWrapper.removeView(startDateLayout);

            mainWrapper.removeView(checklistWrapper);
            mainWrapper.removeView(remindersWrapper);

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
            dueDatePickerLayout.removeView(dueDatePickerText);
            //Allows user to decide if they want to add a due date or not
            dueDateCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isChecked()) {
                    dueDatePickerLayout.addView(dueDatePickerText);
                } else {
                    dueDatePickerLayout.removeView(dueDatePickerText);
                }
            });
        } else {
            mainWrapper.removeView(dueDateLayout);
        }

        if (!taskType.equals("reward")) {
            taskValueLayout.setVisibility(View.GONE);
        } else {

            mainWrapper.removeView(checklistWrapper);
            mainWrapper.removeView(remindersWrapper);

            difficultyWrapper.setVisibility(View.GONE);
            attributeWrapper.setVisibility(View.GONE);
        }

        if (taskId != null) {
            taskRepository.getTask(taskId)
                    .first()
                    .subscribe(task -> {
                        this.task = task;
                        if (task != null) {
                            populate(task);
                            populateChecklistRecyclerView();

                            setTitle(task);

                            populateRemindersRecyclerView();
                        }

                        setTitle(task);
                    }, RxErrorHandler.handleEmptyError());

            btnDelete.setEnabled(true);
        } else {
            setTitle((Task) null);
            taskText.requestFocus();
        }

        if (taskType.equals("todo") || taskType.equals("daily")) {
            createCheckListRecyclerView();
            createRemindersRecyclerView();
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

        // if showChecklist is inactive the wrapper is wrapper, so the reference can't be found
        if(emojiToggle2 == null) {
            emojiToggle2 = emojiToggle0;
        }

        popup = new EmojiPopup(emojiToggle0.getRootView(), this, ContextCompat.getColor(this, R.color.brand));

        popup.setSizeForSoftKeyboard();
        popup.setOnDismissListener(() -> changeEmojiKeyboardIcon(false));
        popup.setOnSoftKeyboardOpenCloseListener(new EmojiPopup.OnSoftKeyboardOpenCloseListener() {

            @Override
            public void onKeyboardOpen(int keyBoardHeight) {

            }

            @Override
            public void onKeyboardClose() {
                if (popup.isShowing()) {
                    popup.dismiss();
                }
            }
        });

        popup.setOnEmojiconClickedListener(emojicon -> {
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
        });

        popup.setOnEmojiconBackspaceClickedListener(v -> {
            if (isEmojiEditText(getCurrentFocus())) {
                KeyEvent event = new KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                getCurrentFocus().dispatchKeyEvent(event);
            }
        });

        emojiToggle0.setOnClickListener(new emojiClickListener(taskText));
        emojiToggle1.setOnClickListener(new emojiClickListener(taskNotes));
        if (isTodo) {
            emojiToggle2.setOnClickListener(new emojiClickListener(newCheckListEditText));
        }

        enableRepeatables();

        tagRepository.getTags(userId)
                .subscribe(loadedTags -> {
                            tags = loadedTags;
                            createTagsCheckBoxes();
                        }, throwable -> {}
                );
    }

    @Override
    protected void onDestroy() {
        taskRepository.close();
        tagRepository.close();
        super.onDestroy();
    }

    @Override
    protected void injectActivity(AppComponent component) {
        component.inject(this);
    }

    // @TODO: abstract business logic to Presenter and only modify view?
    private void enableRepeatables()
    {
        if (!remoteConfigManager.repeatablesAreEnabled()){
            return;
        }

        if (!taskType.equals("daily")) {
            repeatablesLayout.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams repeatablesLayoutParams = repeatablesLayout.getLayoutParams();
            repeatablesLayoutParams.height = 0;
            repeatablesLayout.setLayoutParams(repeatablesLayoutParams);
            return;
        };

        startDateLayout.setVisibility(View.INVISIBLE);

        ViewGroup.LayoutParams startDateLayoutParams = startDateLayout.getLayoutParams();
        startDateLayoutParams.height = 0;
        startDateLayout.setLayoutParams(startDateLayoutParams);

        ViewGroup.LayoutParams startDatePickerTextParams = startDatePickerText.getLayoutParams();
        startDatePickerTextParams.height = 0;
        startDatePickerText.setLayoutParams(startDatePickerTextParams);

        ViewGroup.LayoutParams startDateTitleTextViewParams = startDateTitleTextView.getLayoutParams();
        startDateTitleTextViewParams.height = 0;
        startDateTitleTextView.setLayoutParams(startDateTitleTextViewParams);

        weekdayWrapper.setVisibility(View.INVISIBLE);
        ViewGroup.LayoutParams weekdayWrapperParams = weekdayWrapper.getLayoutParams();
        weekdayWrapperParams.height = 0;
        weekdayWrapper.setLayoutParams(weekdayWrapperParams);

        ViewGroup.LayoutParams frequencyTitleTextViewParams = frequencyTitleTextView.getLayoutParams();
        frequencyTitleTextViewParams.height = 0;
        frequencyTitleTextView.setLayoutParams(frequencyTitleTextViewParams);

        ViewGroup.LayoutParams dailyFrequencySpinnerParams = dailyFrequencySpinner.getLayoutParams();
        dailyFrequencySpinnerParams.height = 0;
        dailyFrequencySpinner.setLayoutParams(dailyFrequencySpinnerParams);

        startDateListener = new DateEditTextListener(repeatablesStartDatePickerText);

        ArrayAdapter<CharSequence> frequencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.repeatables_frequencies, android.R.layout.simple_spinner_item);
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.repeatablesFrequencySpinner.setAdapter(frequencyAdapter);
        this.repeatablesFrequencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                generateSummary();
                Resources r = getResources();

                // @TODO: remove magic numbers

                if (position == 2) {
                    ViewGroup.LayoutParams repeatablesOnSpinnerParams = repeatablesOnSpinner.getLayoutParams();
                    repeatablesOnSpinnerParams.height =  (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72, r.getDisplayMetrics());
                    repeatablesOnSpinner.setLayoutParams(repeatablesOnSpinnerParams);

                    ViewGroup.LayoutParams repeatablesOnTitleParams = reapeatablesOnTextView.getLayoutParams();
                    repeatablesOnTitleParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30, r.getDisplayMetrics());
                    reapeatablesOnTextView.setLayoutParams(repeatablesOnTitleParams);
                }else if (position == 1) {
                    ViewGroup.LayoutParams repeatablesFrequencyContainerParams = repeatablesFrequencyContainer.getLayoutParams();
                    repeatablesFrequencyContainerParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 220, r.getDisplayMetrics());
                    repeatablesFrequencyContainer.setLayoutParams(repeatablesFrequencyContainerParams);
                    return;
                } else {
                    ViewGroup.LayoutParams repeatablesOnSpinnerParams = repeatablesOnSpinner.getLayoutParams();
                    repeatablesOnSpinnerParams.height = 0;
                    repeatablesOnSpinner.setLayoutParams(repeatablesOnSpinnerParams);

                    ViewGroup.LayoutParams repeatablesOnTitleParams = reapeatablesOnTextView.getLayoutParams();
                    repeatablesOnTitleParams.height =  0;
                    reapeatablesOnTextView.setLayoutParams(repeatablesOnTitleParams);

                    ViewGroup.LayoutParams repeatablesFrequencyContainerParams = repeatablesFrequencyContainer.getLayoutParams();
                    repeatablesFrequencyContainerParams.height = 0;
                    repeatablesFrequencyContainer.setLayoutParams(repeatablesFrequencyContainerParams);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> repeatablesOnAdapter = ArrayAdapter.createFromResource(this,
                R.array.repeatables_on, android.R.layout.simple_spinner_item);
        repeatablesOnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        this.repeatablesOnSpinner.setAdapter(repeatablesOnAdapter);
        this.repeatablesOnSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                generateSummary();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        setEveryXSpinner(repeatablesEveryXSpinner);
        repeatablesEveryXSpinner.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                generateSummary();
            }
        });

        this.repeatablesFrequencyContainer.removeAllViews();
        String[] weekdays = getResources().getStringArray(R.array.weekdays);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String dayOfTheWeek = sharedPreferences.getString("FirstDayOfTheWeek",
                Integer.toString(Calendar.getInstance().getFirstDayOfWeek()));
        firstDayOfTheWeekHelper =
                FirstDayOfTheWeekHelper.newInstance(Integer.parseInt(dayOfTheWeek));
        ArrayList<String> weekdaysTemp = new ArrayList<>(Arrays.asList(weekdays));
        Collections.rotate(weekdaysTemp, firstDayOfTheWeekHelper.getDailyTaskFormOffset());
        weekdays = weekdaysTemp.toArray(new String[1]);

        for (int i = 0; i < 7; i++) {
            View weekdayRow = getLayoutInflater().inflate(R.layout.row_checklist, this.repeatablesFrequencyContainer, false);
            CheckBox checkbox = (CheckBox) weekdayRow.findViewById(R.id.checkbox);
            checkbox.setText(weekdays[i]);
            checkbox.setChecked(true);
            checkbox.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    generateSummary();
                }
            });
            repeatablesWeekDayCheckboxes.add(checkbox);
            repeatablesFrequencyContainer.addView(weekdayRow);
        }

        generateSummary();
    }

    private void generateSummary () {
        String frequency = repeatablesFrequencySpinner.getSelectedItem().toString();
        String everyX = String.valueOf(repeatablesEveryXSpinner.getValue());
        String frequencyQualifier = "";

        switch (frequency) {
            case "Daily":
                frequencyQualifier = "day(s)";
                break;
            case "Weekly":
                frequencyQualifier = "week(s)";
                break;
            case "Monthly":
                frequencyQualifier = "month(s)";
                break;
            case "Yearly":
                frequencyQualifier = "year(s)";
                break;
        }

        String weekdays = "";
        List<String> weekdayStrings = new ArrayList<>();
        int offset = firstDayOfTheWeekHelper.getDailyTaskFormOffset();
        if (this.repeatablesWeekDayCheckboxes.get(offset).isChecked()) {
            weekdayStrings.add("Monday");
        }
        if (this.repeatablesWeekDayCheckboxes.get((offset + 1) % 7).isChecked()) {
            weekdayStrings.add("Tuesday");
        }
        if (this.repeatablesWeekDayCheckboxes.get((offset + 2) % 7).isChecked()) {
            weekdayStrings.add("Wednesday");
        }
        if (this.repeatablesWeekDayCheckboxes.get((offset + 3) % 7).isChecked()) {
            weekdayStrings.add("Thursday");
        }
        if (this.repeatablesWeekDayCheckboxes.get((offset + 4) % 7).isChecked()) {
            weekdayStrings.add("Friday");
        }
        if (this.repeatablesWeekDayCheckboxes.get((offset + 5) % 7).isChecked()) {
            weekdayStrings.add("Saturday");
        }
        if (this.repeatablesWeekDayCheckboxes.get((offset + 6) % 7).isChecked()) {
            weekdayStrings.add("Sunday");
        }
        weekdays = " on " + TextUtils.join(", ", weekdayStrings);
        if (!frequency.equals("Weekly")) {
            weekdays = "";
        }

        if (frequency.equals("Monthly")) {
            weekdays = "";
            Calendar calendar = startDateListener.getCalendar();
            String monthlyFreq = repeatablesOnSpinner.getSelectedItem().toString();
            if (monthlyFreq.equals("Day of Month")) {
                Integer date = calendar.get(Calendar.DATE);
                weekdays = " on the " + date.toString();
            } else {
                Integer week = calendar.get(Calendar.WEEK_OF_MONTH);
                String dayLongName = calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault());
                weekdays = " on the " + week.toString() + " week on " + dayLongName;
            }
        }

        String summary = getResources().getString(R.string.repeat_summary, frequency, everyX, frequencyQualifier, weekdays);
        summaryTextView.setText(summary);
    }

    private void setEveryXSpinner(NumberPicker frequencyPicker) {
//        View dayRow = getLayoutInflater().inflate(R.layout.row_number_picker, this.frequencyContainer, false);
//        frequencyPicker = (NumberPicker) dayRow.findViewById(R.id.numberPicker);
        frequencyPicker.setMinValue(1);
        frequencyPicker.setMaxValue(366);
//        TextView tv = (TextView) dayRow.findViewById(R.id.label);
//        tv.setText(getResources().getString(R.string.frequency_daily));
//        this.frequencyContainer.addView(dayRow);
    }

    private boolean isEmojiEditText(@Nullable View view) {
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

    private void createCheckListRecyclerView() {

        checklistAdapter = new CheckListAdapter();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(checklistAdapter);

        recyclerView.setLayoutManager(new WrapContentRecyclerViewLayoutManager(this));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(checklistAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void populateChecklistRecyclerView() {
        List<ChecklistItem> checklistItems = new ArrayList<>();
        if (task != null && task.getChecklist() != null) {
            checklistItems = task.getChecklist();
        }
        checklistAdapter.setItems(checklistItems);
    }

    @OnClick(R.id.add_checklist_button)
    public void addChecklistItem() {
        String checklist = newCheckListEditText.getText().toString();
        ChecklistItem item = new ChecklistItem(checklist);
        checklistAdapter.addItem(item);
        newCheckListEditText.setText("");
    }

    private void createRemindersRecyclerView() {
        List<RemindersItem> reminders = new ArrayList<>();
        if (task != null && task.getReminders() != null) {
            reminders = task.getReminders();
        }

        remindersAdapter = new RemindersAdapter(taskType);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        remindersRecyclerView.setLayoutManager(llm);
        remindersRecyclerView.setAdapter(remindersAdapter);

        remindersRecyclerView.setLayoutManager(new WrapContentRecyclerViewLayoutManager(this));

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(remindersAdapter);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(remindersRecyclerView);
    }

    private void populateRemindersRecyclerView() {
        List<RemindersItem> reminders = new ArrayList<>();
        if (task != null && task.getReminders() != null) {
            reminders = task.getReminders();
        }

        remindersAdapter.setReminders(reminders);
    }

    private void addNewReminder(RemindersItem remindersItem) {
        remindersAdapter.addItem(remindersItem);
    }

    @OnClick(R.id.new_reminder_edittext)
    public void selectNewReminderTime() {
        remindersManager.createReminderTimeDialog(this::addNewReminder, taskType, this, null);
    }

    private void createTagsCheckBoxes() {
        int position = 0;
        for (Tag tag : tags) {
            TableRow row = (TableRow) getLayoutInflater().inflate(R.layout.row_checklist, this.tagsContainerLinearLayout, false);
            CheckBox checkbox = (CheckBox) row.findViewById(R.id.checkbox);
            row.setId(position);
            checkbox.setText(tag.getName()); // set text Name
            checkbox.setId(position);
            //This is to check if the tag was selected by the user. Similar to onClickListener
            checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (buttonView.isChecked()) {
                    if (!selectedTags.contains(tag)) {
                        selectedTags.add(tag);
                    }
                } else {
                    if (selectedTags.contains(tag)) {
                        selectedTags.remove(tag);

                    }
                }
            });
            checkbox.setChecked(taskFilterHelper.isTagChecked(tag.getId()));
            tagsContainerLinearLayout.addView(row);
            tagCheckBoxList.add(checkbox);
            position++;
        }

        if (task != null) {
            fillTagCheckboxes();
        }
    }

    private void setTitle(@Nullable Task task) {
        ActionBar actionBar = getSupportActionBar();

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
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String dayOfTheWeek = sharedPreferences.getString("FirstDayOfTheWeek",
                    Integer.toString(Calendar.getInstance().getFirstDayOfWeek()));
            firstDayOfTheWeekHelper =
                    FirstDayOfTheWeekHelper.newInstance(Integer.parseInt(dayOfTheWeek));
            ArrayList<String> weekdaysTemp = new ArrayList<>(Arrays.asList(weekdays));
            Collections.rotate(weekdaysTemp, firstDayOfTheWeekHelper.getDailyTaskFormOffset());
            weekdays = weekdaysTemp.toArray(new String[1]);

            for (int i = 0; i < 7; i++) {
                View weekdayRow = getLayoutInflater().inflate(R.layout.row_checklist, this.frequencyContainer, false);
                CheckBox checkbox = (CheckBox) weekdayRow.findViewById(R.id.checkbox);
                checkbox.setText(weekdays[i]);
                checkbox.setChecked(true);
                this.weekdayCheckboxes.add(checkbox);
                this.frequencyContainer.addView(weekdayRow);
            }
        } else {
            View dayRow = getLayoutInflater().inflate(R.layout.row_number_picker, this.frequencyContainer, false);
            this.frequencyPicker = (NumberPicker) dayRow.findViewById(R.id.numberPicker);
            this.frequencyPicker.setMinValue(1);
            this.frequencyPicker.setMaxValue(366);
            TextView tv = (TextView) dayRow.findViewById(R.id.label);
            tv.setText(getResources().getString(R.string.frequency_daily));
            this.frequencyContainer.addView(dayRow);
        }

        if (this.task != null) {

            if (this.dailyFrequencySpinner.getSelectedItemPosition() == 0) {
                int offset = firstDayOfTheWeekHelper.getDailyTaskFormOffset();
                this.weekdayCheckboxes.get(offset).setChecked(this.task.getRepeat().getM());
                this.weekdayCheckboxes.get((offset + 1) % 7).setChecked(this.task.getRepeat().getT());
                this.weekdayCheckboxes.get((offset + 2) % 7).setChecked(this.task.getRepeat().getW());
                this.weekdayCheckboxes.get((offset + 3) % 7).setChecked(this.task.getRepeat().getTh());
                this.weekdayCheckboxes.get((offset + 4) % 7).setChecked(this.task.getRepeat().getF());
                this.weekdayCheckboxes.get((offset + 5) % 7).setChecked(this.task.getRepeat().getS());
                this.weekdayCheckboxes.get((offset + 6) % 7).setChecked(this.task.getRepeat().getSu());
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
            finishActivitySuccessfully();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populate(Task task) {
        taskText.setText(task.text);
        taskNotes.setText(task.notes);
        taskValue.setText(String.format(Locale.getDefault(), "%.2f", task.value));

        if (tags != null) {
            fillTagCheckboxes();
        }

        for (Tag tag : task.getTags()) {
            selectedTags.add(tag);
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
        if (attribute != null) {
            switch (attribute) {
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
                startDateListener.setCalendar(task.getStartDate());
            }

            if (task.getFrequency().equals("weekly")) {
                this.dailyFrequencySpinner.setSelection(0);
                if (weekdayCheckboxes.size() == 7) {
                    int offset = firstDayOfTheWeekHelper.getDailyTaskFormOffset();
                    this.weekdayCheckboxes.get(offset).setChecked(this.task.getRepeat().getM());
                    this.weekdayCheckboxes.get((offset + 1) % 7).setChecked(this.task.getRepeat().getT());
                    this.weekdayCheckboxes.get((offset + 2) % 7).setChecked(this.task.getRepeat().getW());
                    this.weekdayCheckboxes.get((offset + 3) % 7).setChecked(this.task.getRepeat().getTh());
                    this.weekdayCheckboxes.get((offset + 4) % 7).setChecked(this.task.getRepeat().getF());
                    this.weekdayCheckboxes.get((offset + 5) % 7).setChecked(this.task.getRepeat().getS());
                    this.weekdayCheckboxes.get((offset + 6) % 7).setChecked(this.task.getRepeat().getSu());
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
                dueDateListener.setCalendar(task.getDueDate());
            }
        }

        if (task.isGroupTask()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.group_tasks_edit_title)
                    .setMessage(R.string.group_tasks_edit_description)
                    .setPositiveButton(android.R.string.ok, (dialog, which) -> finish())
                    .show();
        }
    }

    private void fillTagCheckboxes() {
        for (Tag tag : task.getTags()) {
            int position = tags.indexOf(tag);
            if (tagCheckBoxList.size() > position && position >= 0) {
                tagCheckBoxList.get(position).setChecked(true);
            }
        }
    }

    private boolean saveTask(Task task) {

        String text = MarkdownParser.parseCompiled(taskText.getText());
        if (text == null || text.isEmpty()) {
            return false;
        }

        taskRepository.executeTransaction(realm -> {
            task.text = text;

            if (checklistAdapter != null) {
                if (checklistAdapter.getCheckListItems() != null) {
                    List<ChecklistItem> newItems = checklistAdapter.getCheckListItems();
                    if (task.getChecklist() == null) {
                        task.setChecklist(new RealmList<>());
                    }
                    List<ChecklistItem> itemsToRemove = new ArrayList<>();
                    for (ChecklistItem item : task.getChecklist()) {
                        if (item.getId() == null) {
                            itemsToRemove.add(item);
                            break;
                        }
                        ChecklistItem newItem = null;
                        for (ChecklistItem checkedItem : newItems) {
                            if (item.getId().equals(checkedItem.getId())) {
                                newItem = checkedItem;
                                break;
                            }
                        }
                        if (newItem == null) {
                            itemsToRemove.add(item);
                        } else {
                            item.setText(newItem.getText());
                            newItems.remove(newItem);
                        }
                    }
                    task.getChecklist().removeAll(itemsToRemove);
                    task.getChecklist().addAll(newItems);
                }
            }

            if (remindersAdapter != null) {
                if (remindersAdapter.getRemindersItems() != null) {
                    List<RemindersItem> newItems = remindersAdapter.getRemindersItems();
                    if (task.getReminders() == null) {
                        task.setReminders(new RealmList<>());
                    }
                    List<RemindersItem> itemsToRemove = new ArrayList<>();
                    for (RemindersItem item : task.getReminders()) {
                        if (item.getId() == null) {
                            itemsToRemove.add(item);
                            break;
                        }
                        RemindersItem newItem = null;
                        for (RemindersItem checkedItem : newItems) {
                            if (item.getId().equals(checkedItem.getId())) {
                                newItem = checkedItem;
                                break;
                            }
                        }
                        if (newItem == null) {
                            itemsToRemove.add(item);
                        } else {
                            item.setTime(newItem.getTime());
                            newItems.remove(newItem);
                        }
                    }
                    task.getReminders().removeAll(itemsToRemove);
                    task.getReminders().addAll(newItems);                }
            }


            RealmList<Tag> taskTags = new RealmList<>();
            taskTags.addAll(selectedTags);
            task.setTags(taskTags);

            task.notes = MarkdownParser.parseCompiled(taskNotes.getText());

            if (taskDifficultySpinner.getSelectedItemPosition() == 0) {
                task.setPriority((float) 0.1);
            } else if (taskDifficultySpinner.getSelectedItemPosition() == 1) {
                task.setPriority((float) 1.0);
            } else if (taskDifficultySpinner.getSelectedItemPosition() == 2) {
                task.setPriority((float) 1.5);
            } else if (taskDifficultySpinner.getSelectedItemPosition() == 3) {
                task.setPriority((float) 2.0);
            }

            if (TextUtils.isEmpty(allocationMode) || !allocationMode.equals("taskbased")) {
                task.setAttribute(Task.ATTRIBUTE_STRENGTH);
            } else {
                switch (taskAttributeSpinner.getSelectedItemPosition()) {
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
                    task.setStartDate(new Date(startDateListener.getCalendar().getTimeInMillis()));

                    if (dailyFrequencySpinner.getSelectedItemPosition() == 0) {
                        task.setFrequency("weekly");
                        String frequency = this.repeatablesFrequencySpinner.getSelectedItem().toString();
                        if (frequency != null && remoteConfigManager.repeatablesAreEnabled()) {
                            task.setFrequency(frequency.toLowerCase());
                        }

                        Days repeat = task.getRepeat();
                        if (repeat == null) {
                            repeat = new Days();
                            task.setRepeat(repeat);
                        }

                        int offset = firstDayOfTheWeekHelper.getDailyTaskFormOffset();
                        repeat.setM(weekdayCheckboxes.get(offset).isChecked());
                        repeat.setT(weekdayCheckboxes.get((offset + 1) % 7).isChecked());
                        repeat.setW(weekdayCheckboxes.get((offset + 2) % 7).isChecked());
                        repeat.setTh(weekdayCheckboxes.get((offset + 3) % 7).isChecked());
                        repeat.setF(weekdayCheckboxes.get((offset + 4) % 7).isChecked());
                        repeat.setS(weekdayCheckboxes.get((offset + 5) % 7).isChecked());
                        repeat.setSu(weekdayCheckboxes.get((offset + 6) % 7).isChecked());

                        if (remoteConfigManager.repeatablesAreEnabled()) {
                            repeat.setM(this.repeatablesWeekDayCheckboxes.get(offset).isChecked());
                            repeat.setT(this.repeatablesWeekDayCheckboxes.get((offset + 1) % 7).isChecked());
                            repeat.setW(this.repeatablesWeekDayCheckboxes.get((offset + 2) % 7).isChecked());
                            repeat.setTh(this.repeatablesWeekDayCheckboxes.get((offset + 3) % 7).isChecked());
                            repeat.setF(this.repeatablesWeekDayCheckboxes.get((offset + 4) % 7).isChecked());
                            repeat.setS(this.repeatablesWeekDayCheckboxes.get((offset + 5) % 7).isChecked());
                            repeat.setSu(this.repeatablesWeekDayCheckboxes.get((offset + 6) % 7).isChecked());
                        }

                        if ("monthly".equals(frequency)) {
                            Calendar calendar = startDateListener.getCalendar();
                            String monthlyFreq = repeatablesOnSpinner.getSelectedItem().toString();
                            if (monthlyFreq.equals("Day of Month")) {
                                Integer date = calendar.get(Calendar.DATE);
                                task.daysOfMonth = new ArrayList<>();
                                task.daysOfMonth.add(date);
                                task.weeksOfMonth = new ArrayList<>();
                            } else {
                                Integer week = calendar.get(Calendar.WEEK_OF_MONTH);
                                task.weeksOfMonth = new ArrayList<>();
                                task.weeksOfMonth.add(week);
                                task.daysOfMonth = new ArrayList<>();
                            }
                        }
                    } else {
                        task.setFrequency("daily");
                        task.setEveryX(frequencyPicker.getValue());
                    }
                }
                break;

                case "todo": {
                    if (dueDateCheckBox.isChecked()) {
                        task.setDueDate(new Date(dueDateListener.getCalendar().getTimeInMillis()));
                    } else {
                        task.setDueDate(null);
                    }
                }
                break;

                case "reward": {
                    String value = taskValue.getText().toString();
                    if (!value.isEmpty()) {
                        NumberFormat localFormat = DecimalFormat.getInstance(Locale.getDefault());
                        try {
                            task.setValue(localFormat.parse(value).doubleValue());
                        } catch (ParseException e) {
                        }
                    } else {
                        task.setValue(0.0d);
                    }

                }
                break;
            }
        });
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
            //send back to other elements.
            Observable<Task> observable;
            if (TaskFormActivity.this.task.getId() == null) {
                observable = taskRepository.createTask(task);
            } else {
                observable = taskRepository.updateTask(task);
            }

            observable.subscribe(task1 -> {}, RxErrorHandler.handleEmptyError());
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

    private void finishActivitySuccessfully() {
        this.prepareSave();
        finishWithSuccess();
        dismissKeyboard();
    }

    private void finishWithSuccess() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra(TaskFormActivity.TASK_TYPE_KEY, taskType);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void dismissKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    private class DateEditTextListener implements View.OnClickListener, DatePickerDialog.OnDateSetListener {
        Calendar calendar;
        DatePickerDialog datePickerDialog;
        EditText datePickerText;
        DateFormat dateFormatter;

        DateEditTextListener(EditText dateText) {
            calendar = Calendar.getInstance();

            this.datePickerText = dateText;
            this.datePickerText.setOnClickListener(this);
            this.dateFormatter = DateFormat.getDateInstance();
            this.datePickerDialog = new DatePickerDialog(datePickerText.getContext(), this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));

            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            String dayOfTheWeek = sharedPreferences.getString("FirstDayOfTheWeek",
                    Integer.toString(Calendar.getInstance().getFirstDayOfWeek()));
            FirstDayOfTheWeekHelper firstDayOfTheWeekHelper =
                    FirstDayOfTheWeekHelper.newInstance(Integer.parseInt(dayOfTheWeek));
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
                datePickerDialog.getDatePicker().getCalendarView().setFirstDayOfWeek(
                        firstDayOfTheWeekHelper.getFirstDayOfTheWeek());
            } else {
                datePickerDialog.getDatePicker().setFirstDayOfWeek(firstDayOfTheWeekHelper
                        .getFirstDayOfTheWeek());
            }

            this.datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, getResources().getString(R.string.today), (dialog, which) -> {
                setCalendar(Calendar.getInstance().getTime());
            });
            updateDateText();
        }

        public void onClick(View view) {
            datePickerDialog.show();
        }

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            calendar.set(year, monthOfYear, dayOfMonth);
            updateDateText();
        }

        public Calendar getCalendar() {
            return (Calendar) calendar.clone();
        }

        public void setCalendar(Date date) {
            calendar.setTime(date);
            datePickerDialog.updateDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            updateDateText();
        }

        private void updateDateText() {
            datePickerText.setText(dateFormatter.format(calendar.getTime()));
        }
    }

    private class emojiClickListener implements View.OnClickListener {

        EmojiEditText view;

        emojiClickListener(EmojiEditText view) {
            this.view = view;
        }

        @Override
        public void onClick(View v) {
            if (!popup.isShowing()) {

                if (popup.isKeyBoardOpen()) {
                    popup.showAtBottom();
                    changeEmojiKeyboardIcon(true);
                } else {
                    view.setFocusableInTouchMode(true);
                    view.requestFocus();
                    popup.showAtBottomPending();
                    final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
                    changeEmojiKeyboardIcon(true);
                }
            } else {
                popup.dismiss();
                changeEmojiKeyboardIcon(false);
            }
        }
    }
}
