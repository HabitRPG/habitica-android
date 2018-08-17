package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.TextInputLayout
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.preference.PreferenceManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils
import android.util.TypedValue
import android.view.KeyEvent
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.*
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.Days
import com.habitrpg.android.habitica.models.tasks.RemindersItem
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.WrapContentRecyclerViewLayoutManager
import com.habitrpg.android.habitica.ui.adapter.tasks.CheckListAdapter
import com.habitrpg.android.habitica.ui.adapter.tasks.RemindersAdapter
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.SimpleItemTouchHelperCallback
import com.habitrpg.android.habitica.ui.helpers.ViewHelper
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.functions.Consumer
import io.realm.Realm
import io.realm.RealmList
import net.pherth.android.emoji_library.EmojiEditText
import net.pherth.android.emoji_library.EmojiPopup
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.ParseException
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class TaskFormActivity : BaseActivity(), AdapterView.OnItemSelectedListener {
    private val taskValue: EditText by bindView(R.id.task_value_edittext)
    private val taskValueLayout: TextInputLayout by bindView(R.id.task_value_layout)
    private val checklistWrapper: LinearLayout by bindView(R.id.task_checklist_wrapper)
    private val difficultyWrapper: LinearLayout by bindView(R.id.task_difficulty_wrapper)
    private val attributeWrapper: LinearLayout by bindView(R.id.task_attribute_wrapper)
    private val mainWrapper: LinearLayout by bindView(R.id.task_main_wrapper)
    private val taskText: EmojiEditText by bindView(R.id.task_text_edittext)
    private val taskNotes: EmojiEditText by bindView(R.id.task_notes_edittext)
    private val taskDifficultySpinner: Spinner by bindView(R.id.task_difficulty_spinner)
    private val taskAttributeSpinner: Spinner by bindView(R.id.task_attribute_spinner)
    private val btnDelete: Button by bindView(R.id.btn_delete_task)
    private val startDateLayout: LinearLayout by bindView(R.id.task_startdate_layout)
    private val taskWrapper: LinearLayout by bindView(R.id.task_task_wrapper)
    private val positiveCheckBox: CheckBox by bindView(R.id.task_positive_checkbox)
    private val negativeCheckBox: CheckBox by bindView(R.id.task_negative_checkbox)
    private val actionsLayout: LinearLayout by bindView(R.id.task_actions_wrapper)
    private val weekdayWrapper: LinearLayout by bindView(R.id.task_weekdays_wrapper)
    private val frequencyTitleTextView: TextView by bindView(R.id.frequency_title)
    private val dailyFrequencySpinner: Spinner by bindView(R.id.task_frequency_spinner)
    private val frequencyContainer: LinearLayout by bindView(R.id.task_frequency_container)
    private val recyclerView: RecyclerView by bindView(R.id.checklist_recycler_view)
    private val newCheckListEditText: EmojiEditText by bindView(R.id.new_checklist)
    private val addChecklistItemButton: Button by bindView(R.id.add_checklist_button)
    private val remindersWrapper: LinearLayout by bindView(R.id.task_reminders_wrapper)
    private val newRemindersEditText: EditText by bindView(R.id.new_reminder_edittext)
    private val remindersRecyclerView: RecyclerView by bindView(R.id.reminders_recycler_view)
    private val emojiToggle0: ImageButton by bindView(R.id.emoji_toggle_btn0)
    private val emojiToggle1: ImageButton by bindView(R.id.emoji_toggle_btn1)
    private var emojiToggle2: ImageButton? = null
    private val dueDateLayout: LinearLayout by bindView(R.id.task_duedate_layout)
    private val dueDatePickerLayout: LinearLayout by bindView(R.id.task_duedate_picker_layout)
    private val dueDateCheckBox: CheckBox by bindView(R.id.duedate_checkbox)
    private val startDateTitleTextView: TextView by bindView(R.id.startdate_text_title)
    private val startDatePickerText: EditText by bindView(R.id.startdate_text_edittext)
    private val repeatablesStartDatePickerText: EditText by bindView(R.id.repeatables_startdate_text_edittext)
    private var startDateListener: DateEditTextListener? = null
    private val repeatablesLayout: LinearLayout by bindView(R.id.repeatables)
    private val reapeatablesOnTextView: TextView by bindView(R.id.repeatables_on_title)
    private val repeatablesOnSpinner: Spinner by bindView(R.id.task_repeatables_on_spinner)
    private val repeatablesEveryXSpinner: NumberPicker by bindView(R.id.task_repeatables_every_x_spinner)
    private val repeatablesFrequencyContainer: LinearLayout by bindView(R.id.task_repeatables_frequency_container)
    private val summaryTextView: TextView by bindView(R.id.summary)
    private val dueDatePickerText: EditText by bindView(R.id.duedate_text_edittext)
    private var dueDateListener: DateEditTextListener? = null
    private val tagsWrapper: LinearLayout by bindView(R.id.task_tags_wrapper)
    private val tagsContainerLinearLayout: LinearLayout by bindView(R.id.task_tags_checklist)
    private val repeatablesFrequencySpinner: Spinner by bindView(R.id.task_repeatables_frequency_spinner)


    @Inject
    internal lateinit var taskFilterHelper: TaskFilterHelper
    @Inject
    internal lateinit var taskRepository: TaskRepository
    @Inject
    internal lateinit var tagRepository: TagRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    internal lateinit var userId: String
    @Inject
    internal lateinit var remoteConfigManager: RemoteConfigManager
    @Inject
    internal lateinit var taskAlarmManager: TaskAlarmManager

    private var task: Task? = null
    private var taskBasedAllocation: Boolean = false
    private val weekdayCheckboxes = ArrayList<CheckBox>()
    private val repeatablesWeekDayCheckboxes = ArrayList<CheckBox>()
    private var frequencyPicker: NumberPicker? = null
    private var tags: List<Tag>? = null
    private var checklistAdapter: CheckListAdapter? = null
    private var remindersAdapter: RemindersAdapter? = null
    private var tagCheckBoxList: MutableList<CheckBox>? = null
    private var selectedTags: MutableList<Tag>? = null

    private lateinit var remindersManager: RemindersManager
    private var firstDayOfTheWeekHelper: FirstDayOfTheWeekHelper? = null

    private var taskType: String? = null
    private var taskId: String? = null
    private var popup: EmojiPopup? = null

    private var shouldSaveTask = true

    override fun getLayoutResId(): Int {
        return R.layout.activity_task_form
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        popup = EmojiPopup(emojiToggle0.rootView, this, ContextCompat.getColor(this, R.color.brand))

        val bundle = intent.extras

        taskType = bundle.getString(TASK_TYPE_KEY)
        task = bundle.getParcelable(PARCELABLE_TASK) as? Task
        taskId = bundle.getString(TASK_ID_KEY)
        taskBasedAllocation = bundle.getBoolean(ALLOCATION_MODE_KEY)
        shouldSaveTask = bundle.getBoolean(SAVE_TO_DB)
        val showTagSelection = bundle.getBoolean(SHOW_TAG_SELECTION, true)
        tagCheckBoxList = ArrayList()

        tagsWrapper.visibility = if (showTagSelection) View.VISIBLE else View.GONE

        if (bundle.containsKey(PARCELABLE_TASK)) {
            task = bundle.getParcelable(PARCELABLE_TASK)
            taskType = task?.type
        }

        tagCheckBoxList = ArrayList()
        selectedTags = ArrayList()
        if (taskType == null) {
            return
        }

        remindersManager = RemindersManager(taskType)

        dueDateListener = DateEditTextListener(dueDatePickerText)
        startDateListener = DateEditTextListener(startDatePickerText)

        btnDelete.isEnabled = false
        ViewHelper.SetBackgroundTint(btnDelete, ContextCompat.getColor(this, R.color.red_10))
        btnDelete.setOnClickListener { view ->
            AlertDialog.Builder(view.context)
                    .setTitle(getString(R.string.taskform_delete_title))
                    .setMessage(getString(R.string.taskform_delete_message)).setPositiveButton(getString(R.string.yes)) { _, _ ->

                        finish()
                        dismissKeyboard()

                        taskId.notNull { taskRepository.deleteTask(it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()) }
                    }.setNegativeButton(getString(R.string.no)) { dialog, _ -> dialog.dismiss() }.show()
        }

        val difficultyAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_difficulties, android.R.layout.simple_spinner_item)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        taskDifficultySpinner.adapter = difficultyAdapter
        taskDifficultySpinner.setSelection(1)

        val attributeAdapter = ArrayAdapter.createFromResource(this,
                R.array.task_attributes, android.R.layout.simple_spinner_item)
        attributeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        taskAttributeSpinner.adapter = attributeAdapter
        taskAttributeSpinner.setSelection(0)

        if (!taskBasedAllocation) {
            attributeWrapper.visibility = View.GONE
        }

        if (taskType == Task.TYPE_HABIT) {
            taskWrapper.removeView(startDateLayout)

            mainWrapper.removeView(checklistWrapper)
            mainWrapper.removeView(remindersWrapper)

            positiveCheckBox.isChecked = true
            negativeCheckBox.isChecked = true
        } else {
            mainWrapper.removeView(actionsLayout)
        }

        if (taskType == Task.TYPE_DAILY) {
            val frequencyAdapter = ArrayAdapter.createFromResource(this,
                    R.array.daily_frequencies, android.R.layout.simple_spinner_item)
            frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.dailyFrequencySpinner.adapter = frequencyAdapter
            this.dailyFrequencySpinner.onItemSelectedListener = this
        } else {
            mainWrapper.removeView(weekdayWrapper)
            mainWrapper.removeView(startDateLayout)
        }

        if (taskType == Task.TYPE_TODO) {
            dueDatePickerLayout.removeView(dueDatePickerText)
            //Allows user to decide if they want to add a due date or not
            dueDateCheckBox.setOnCheckedChangeListener { buttonView, _ ->
                if (buttonView.isChecked) {
                    dueDatePickerLayout.addView(dueDatePickerText)
                } else {
                    dueDatePickerLayout.removeView(dueDatePickerText)
                }
            }
        } else {
            mainWrapper.removeView(dueDateLayout)
        }

        if (taskType != Task.TYPE_REWARD) {
            taskValueLayout.visibility = View.GONE
        } else {

            mainWrapper.removeView(checklistWrapper)
            mainWrapper.removeView(remindersWrapper)

            difficultyWrapper.visibility = View.GONE
            attributeWrapper.visibility = View.GONE
        }

        if (taskType == Task.TYPE_TODO || taskType == Task.TYPE_DAILY) {
            createCheckListRecyclerView()
            createRemindersRecyclerView()
        }

        // Emoji keyboard stuff
        var isTodo = false
        if (taskType == Task.TYPE_TODO) {
            isTodo = true
        }

        // If it's a to-do, change the emojiToggle2 to the actual emojiToggle2 (prevents NPEs when not a to-do task)
        emojiToggle2 = if (isTodo) {
            findViewById<View>(R.id.emoji_toggle_btn2) as? ImageButton
        } else {
            emojiToggle0
        }

        // if showChecklist is inactive the wrapper is wrapper, so the reference can't be found
        if (emojiToggle2 == null) {
            emojiToggle2 = emojiToggle0
        }

        popup?.setSizeForSoftKeyboard()
        popup?.setOnDismissListener { changeEmojiKeyboardIcon(false) }
        popup?.setOnSoftKeyboardOpenCloseListener(object : EmojiPopup.OnSoftKeyboardOpenCloseListener {

            override fun onKeyboardOpen(keyBoardHeight: Int) {

            }

            override fun onKeyboardClose() {
                if (popup?.isShowing == true) {
                    popup?.dismiss()
                }
            }
        })

        popup?.setOnEmojiconClickedListener { emojicon ->
            if (currentFocus == null || !isEmojiEditText(currentFocus) || emojicon == null) {
                return@setOnEmojiconClickedListener
            }
            val emojiEditText = currentFocus as? EmojiEditText
            val start = emojiEditText?.selectionStart ?: 0
            val end = emojiEditText?.selectionEnd ?: 0
            if (start < 0) {
                emojiEditText?.append(emojicon.emoji)
            } else {
                emojiEditText?.text?.replace(Math.min(start, end),
                        Math.max(start, end), emojicon.emoji, 0,
                        emojicon.emoji.length)
            }
        }

        popup?.setOnEmojiconBackspaceClickedListener {
            if (isEmojiEditText(currentFocus)) {
                val event = KeyEvent(
                        0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL)
                currentFocus.dispatchKeyEvent(event)
            }
        }

        emojiToggle0.setOnClickListener(EmojiClickListener(taskText))
        emojiToggle1.setOnClickListener(EmojiClickListener(taskNotes))
        if (isTodo) {
            emojiToggle2?.setOnClickListener(EmojiClickListener(newCheckListEditText))
        }

        enableRepeatables()

        tagRepository.getTags(userId)
                .firstElement()
                .subscribe(Consumer { loadedTags ->
                    tags = loadedTags
                    createTagsCheckBoxes()
                }, RxErrorHandler.handleEmptyError()
                )

        if (taskId != null) {
            taskRepository.getTask(taskId ?: "")
                    .firstElement()
                    .subscribe(Consumer { task ->
                        this.task = task
                        if (task != null) {
                            populate(task)

                            setTitle(task)
                            if (taskType == Task.TYPE_TODO || taskType == Task.TYPE_DAILY) {
                                populateChecklistRecyclerView()
                                populateRemindersRecyclerView()
                            }
                        }

                        setTitle(task)
                    }, RxErrorHandler.handleEmptyError())

            btnDelete.isEnabled = true
        } else if (task != null) {
            val thisTask = task
            if (thisTask != null) {
                populate(thisTask)

                setTitle(thisTask)
                if (taskType == Task.TYPE_TODO || taskType == Task.TYPE_DAILY) {
                    populateChecklistRecyclerView()
                    populateRemindersRecyclerView()
                }
            }
        } else {
            //setTitle(null as? Task)
            taskText.requestFocus()
        }

    }

    override fun onDestroy() {
        tagRepository.close()
        super.onDestroy()
    }

    override fun injectActivity(component: AppComponent?) {
        component?.inject(this)
    }

    fun hideMonthOptions() {
        val repeatablesOnSpinnerParams = repeatablesOnSpinner.layoutParams
        repeatablesOnSpinnerParams.height = 0
        repeatablesOnSpinner.layoutParams = repeatablesOnSpinnerParams

        val repeatablesOnTitleParams = reapeatablesOnTextView.layoutParams
        repeatablesOnTitleParams.height = 0
        reapeatablesOnTextView.layoutParams = repeatablesOnTitleParams
    }

    fun hideWeekOptions() {
        val repeatablesFrequencyContainerParams = repeatablesFrequencyContainer.layoutParams
        repeatablesFrequencyContainerParams.height = 0
        repeatablesFrequencyContainer.layoutParams = repeatablesFrequencyContainerParams
    }

    private fun enableRepeatables() {
        if (!remoteConfigManager.repeatablesAreEnabled() || taskType != Task.TYPE_DAILY) {
            repeatablesLayout.visibility = View.INVISIBLE
            val repeatablesLayoutParams = repeatablesLayout.layoutParams
            repeatablesLayoutParams.height = 0
            repeatablesLayout.layoutParams = repeatablesLayoutParams
            return
        }

        startDateLayout.visibility = View.INVISIBLE

        // Hide old stuff
        val startDateLayoutParams = startDateLayout.layoutParams
        startDateLayoutParams.height = 0
        startDateLayout.layoutParams = startDateLayoutParams

        val startDatePickerTextParams = startDatePickerText.layoutParams
        startDatePickerTextParams.height = 0
        startDatePickerText.layoutParams = startDatePickerTextParams

        val startDateTitleTextViewParams = startDateTitleTextView.layoutParams
        startDateTitleTextViewParams.height = 0
        startDateTitleTextView.layoutParams = startDateTitleTextViewParams

        weekdayWrapper.visibility = View.INVISIBLE
        val weekdayWrapperParams = weekdayWrapper.layoutParams
        weekdayWrapperParams.height = 0
        weekdayWrapper.layoutParams = weekdayWrapperParams

        val frequencyTitleTextViewParams = frequencyTitleTextView.layoutParams
        frequencyTitleTextViewParams.height = 0
        frequencyTitleTextView.layoutParams = frequencyTitleTextViewParams

        val dailyFrequencySpinnerParams = dailyFrequencySpinner.layoutParams
        dailyFrequencySpinnerParams.height = 0
        dailyFrequencySpinner.layoutParams = dailyFrequencySpinnerParams

        // Start Date
        startDateListener = DateEditTextListener(repeatablesStartDatePickerText)

        // Frequency
        val frequencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.repeatables_frequencies, android.R.layout.simple_spinner_item)
        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        this.repeatablesFrequencySpinner.adapter = frequencyAdapter
        this.repeatablesFrequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                generateSummary()
                val r = resources

                when (position) {
                    2 -> {
                        hideWeekOptions()

                        val repeatablesOnSpinnerParams = repeatablesOnSpinner.layoutParams
                        repeatablesOnSpinnerParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 72f, r.displayMetrics).toInt()
                        repeatablesOnSpinner.layoutParams = repeatablesOnSpinnerParams

                        val repeatablesOnTitleParams = reapeatablesOnTextView.layoutParams
                        repeatablesOnTitleParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 30f, r.displayMetrics).toInt()
                        reapeatablesOnTextView.layoutParams = repeatablesOnTitleParams
                    }
                    1 -> {
                        hideMonthOptions()

                        val repeatablesFrequencyContainerParams = repeatablesFrequencyContainer.layoutParams
                        repeatablesFrequencyContainerParams.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 220f, r.displayMetrics).toInt()
                        repeatablesFrequencyContainer.layoutParams = repeatablesFrequencyContainerParams
                    }
                    else -> {
                        hideWeekOptions()
                        hideMonthOptions()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        // Repeat On
        val repeatablesOnAdapter = ArrayAdapter.createFromResource(this,
                R.array.repeatables_on, android.R.layout.simple_spinner_item)
        repeatablesOnAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        this.repeatablesOnSpinner.adapter = repeatablesOnAdapter
        this.repeatablesOnSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                generateSummary()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        // Every X
        setupEveryXSpinner(repeatablesEveryXSpinner)
        repeatablesEveryXSpinner.setOnValueChangedListener { _, _, _ -> generateSummary() }

        // WeekDays
        this.repeatablesFrequencyContainer.removeAllViews()
        var weekdays = resources.getStringArray(R.array.weekdays)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val dayOfTheWeek = sharedPreferences.getString("FirstDayOfTheWeek",
                Integer.toString(Calendar.getInstance().firstDayOfWeek))
        firstDayOfTheWeekHelper = FirstDayOfTheWeekHelper.newInstance(Integer.parseInt(dayOfTheWeek))
        val weekdaysTemp = weekdays.asList()
        Collections.rotate(weekdaysTemp, firstDayOfTheWeekHelper?.dailyTaskFormOffset ?: 0)
        weekdays = weekdaysTemp.toTypedArray()

        for (i in 0..6) {
            val weekdayRow = layoutInflater.inflate(R.layout.row_checklist, this.repeatablesFrequencyContainer, false)
            val checkbox = weekdayRow.findViewById<View>(R.id.checkbox) as? CheckBox
            checkbox?.text = weekdays[i]
            checkbox?.isChecked = true
            checkbox?.setOnClickListener { generateSummary() }
            checkbox.notNull { repeatablesWeekDayCheckboxes.add(it) }
            repeatablesFrequencyContainer.addView(weekdayRow)
        }

        generateSummary()
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {}

    private fun generateSummary() {
        val frequency = repeatablesFrequencySpinner.selectedItem.toString()
        val everyX = repeatablesEveryXSpinner.value.toString()
        var frequencyQualifier = ""

        when (frequency) {
            "Daily" -> frequencyQualifier = "day(s)"
            "Weekly" -> frequencyQualifier = "week(s)"
            "Monthly" -> frequencyQualifier = "month(s)"
            "Yearly" -> frequencyQualifier = "year(s)"
        }

        var weekdays: String
        val weekdayStrings = ArrayList<String>()
        val offset = firstDayOfTheWeekHelper?.dailyTaskFormOffset ?: 0
        if (this.repeatablesWeekDayCheckboxes[offset].isChecked) {
            weekdayStrings.add("Monday")
        }
        if (this.repeatablesWeekDayCheckboxes[(offset + 1) % 7].isChecked) {
            weekdayStrings.add("Tuesday")
        }
        if (this.repeatablesWeekDayCheckboxes[(offset + 2) % 7].isChecked) {
            weekdayStrings.add("Wednesday")
        }
        if (this.repeatablesWeekDayCheckboxes[(offset + 3) % 7].isChecked) {
            weekdayStrings.add("Thursday")
        }
        if (this.repeatablesWeekDayCheckboxes[(offset + 4) % 7].isChecked) {
            weekdayStrings.add("Friday")
        }
        if (this.repeatablesWeekDayCheckboxes[(offset + 5) % 7].isChecked) {
            weekdayStrings.add("Saturday")
        }
        if (this.repeatablesWeekDayCheckboxes[(offset + 6) % 7].isChecked) {
            weekdayStrings.add("Sunday")
        }
        weekdays = " on " + TextUtils.join(", ", weekdayStrings)
        if (frequency != "Weekly") {
            weekdays = ""
        }

        if (frequency == "Monthly") {
            val calendar = startDateListener?.getCalendar()
            val monthlyFreq = repeatablesOnSpinner.selectedItem.toString()
            weekdays = if (monthlyFreq == "Day of Month") {
                val date = calendar?.get(Calendar.DATE)
                " on the " + date.toString()
            } else {
                val week = calendar?.get(Calendar.WEEK_OF_MONTH)
                val dayLongName = calendar?.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault())
                " on the " + week.toString() + " week on " + dayLongName
            }
        }

        val summary = resources.getString(R.string.repeat_summary, frequency, everyX, frequencyQualifier, weekdays)
        summaryTextView.text = summary
    }

    private fun populateRepeatables(task: Task) {
        // Frequency
        var frequencySelection = 0
        when {
            task.frequency == "weekly" -> frequencySelection = 1
            task.frequency == "monthly" -> frequencySelection = 2
            task.frequency == "yearly" -> frequencySelection = 3
        }
        this.repeatablesFrequencySpinner.setSelection(frequencySelection)
        this.repeatablesEveryXSpinner.value = task.everyX ?: 0

        if (task.frequency == "weekly") {
            if (repeatablesWeekDayCheckboxes.size == 7) {
                val offset = firstDayOfTheWeekHelper?.dailyTaskFormOffset ?: 0
                this.repeatablesWeekDayCheckboxes[offset].isChecked = this.task?.repeat?.m ?: false
                this.repeatablesWeekDayCheckboxes[(offset + 1) % 7].isChecked = this.task?.repeat?.t ?: false
                this.repeatablesWeekDayCheckboxes[(offset + 2) % 7].isChecked = this.task?.repeat?.w ?: false
                this.repeatablesWeekDayCheckboxes[(offset + 3) % 7].isChecked = this.task?.repeat?.th ?: false
                this.repeatablesWeekDayCheckboxes[(offset + 4) % 7].isChecked = this.task?.repeat?.f ?: false
                this.repeatablesWeekDayCheckboxes[(offset + 5) % 7].isChecked = this.task?.repeat?.s ?: false
                this.repeatablesWeekDayCheckboxes[(offset + 6) % 7].isChecked = this.task?.repeat?.su ?: false
            }
        }

        setUpRepeatsOn(task)
    }

    private fun setUpRepeatsOn(task: Task) {
        this.repeatablesOnSpinner.setSelection(0)
        if (task.getWeeksOfMonth()?.isNotEmpty() == true) {
            this.repeatablesOnSpinner.setSelection(1)
        }
    }

    private fun setupEveryXSpinner(frequencyPicker: NumberPicker) {
        frequencyPicker.minValue = 0
        frequencyPicker.maxValue = 366
        frequencyPicker.value = 1
    }

    private fun isEmojiEditText(view: View?): Boolean {
        return view is EmojiEditText
    }

    private fun changeEmojiKeyboardIcon(keyboardOpened: Boolean) {
        if (keyboardOpened) {
            emojiToggle0.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_keyboard_grey600_24dp))
            emojiToggle1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_keyboard_grey600_24dp))
            emojiToggle2?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_keyboard_grey600_24dp))
        } else {
            emojiToggle0.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_emoticon_grey600_24dp))
            emojiToggle1.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_emoticon_grey600_24dp))
            emojiToggle2?.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_emoticon_grey600_24dp))
        }
    }

    private fun createCheckListRecyclerView() {
        checklistAdapter = CheckListAdapter()

        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL

        recyclerView.layoutManager = llm
        recyclerView.adapter = checklistAdapter

        recyclerView.layoutManager = WrapContentRecyclerViewLayoutManager(this)

        val callback = SimpleItemTouchHelperCallback(checklistAdapter)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(recyclerView)
        addChecklistItemButton.setOnClickListener { addChecklistItem() }
    }

    private fun populateChecklistRecyclerView() {
        var checklistItems: List<ChecklistItem> = ArrayList()
        if (task?.isManaged == true) {
            task?.checklist.notNull {
                checklistItems = taskRepository.getUnmanagedCopy(it)
            }
        }
        checklistAdapter?.setItems(checklistItems)
    }

    private fun addChecklistItem() {
        val text = newCheckListEditText.text.toString()
        val item = ChecklistItem(null, text)
        checklistAdapter?.addItem(item)
        newCheckListEditText.setText("")
    }

    private fun createRemindersRecyclerView() {
        taskType.notNull { remindersAdapter = RemindersAdapter(it) }

        val llm = LinearLayoutManager(this)
        llm.orientation = LinearLayoutManager.VERTICAL

        remindersRecyclerView.layoutManager = llm
        remindersRecyclerView.adapter = remindersAdapter

        remindersRecyclerView.layoutManager = WrapContentRecyclerViewLayoutManager(this)


        val callback = SimpleItemTouchHelperCallback(remindersAdapter)
        val mItemTouchHelper = ItemTouchHelper(callback)
        mItemTouchHelper.attachToRecyclerView(remindersRecyclerView)
        newRemindersEditText.setOnClickListener { selectNewReminderTime() }
    }

    private fun populateRemindersRecyclerView() {
        var reminders: List<RemindersItem> = ArrayList()
        task?.reminders.notNull {
            reminders = taskRepository.getUnmanagedCopy(it)
        }

        remindersAdapter?.setReminders(reminders)
    }

    private fun addNewReminder(remindersItem: RemindersItem) {
        remindersAdapter?.addItem(remindersItem)
    }

    private fun selectNewReminderTime() {
        remindersManager.createReminderTimeDialog({ it.notNull { this.addNewReminder(it) } }, taskType, this, null)
    }

    private fun createTagsCheckBoxes() {
        this.tagsContainerLinearLayout.removeAllViews()
        for ((position, tag) in (tags ?: emptyList()).withIndex()) {
            val row = layoutInflater.inflate(R.layout.row_checklist, this.tagsContainerLinearLayout, false) as? TableRow
            val checkbox = row?.findViewById<View>(R.id.checkbox) as? CheckBox
            row?.id = position
            checkbox?.text = tag.name // set text Name
            checkbox?.id = position
            //This is to check if the tag was selected by the user. Similar to onClickListener
            checkbox?.setOnCheckedChangeListener { buttonView, _ ->
                if (buttonView.isChecked) {
                    if (selectedTags?.contains(tag) == false) {
                        selectedTags?.add(tag)
                    }
                } else {
                    if (selectedTags?.contains(tag) == true) {
                        selectedTags?.remove(tag)
                    }
                }
            }
            checkbox?.isChecked = taskFilterHelper.isTagChecked(tag.getId())
            tagsContainerLinearLayout.addView(row)
            checkbox.notNull { tagCheckBoxList?.add(it) }
        }

        if (task != null) {
            fillTagCheckboxes()
        }
    }

    private fun setTitle(task: Task?) {
        val actionBar = supportActionBar

        if (actionBar != null) {

            var title = ""

            if (task != null && task.isValid) {
                title = resources.getString(R.string.action_edit) + " " + task.text
            } else {
                when (taskType) {
                    Task.TYPE_TODO -> title = resources.getString(R.string.new_todo)
                    Task.TYPE_DAILY -> title = resources.getString(R.string.new_daily)
                    Task.TYPE_HABIT -> title = resources.getString(R.string.new_habit)
                    Task.TYPE_REWARD -> title = resources.getString(R.string.new_reward)
                }
            }

            actionBar.title = title
        }
    }

    private fun setDailyFrequencyViews() {
        this.frequencyContainer.removeAllViews()
        if (this.dailyFrequencySpinner.selectedItemPosition == 0) {
            var weekdays = resources.getStringArray(R.array.weekdays)
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val dayOfTheWeek = sharedPreferences.getString("FirstDayOfTheWeek",
                    Integer.toString(Calendar.getInstance().firstDayOfWeek))
            firstDayOfTheWeekHelper = FirstDayOfTheWeekHelper.newInstance(Integer.parseInt(dayOfTheWeek))
            val weekdaysTemp = weekdays.asList()
            Collections.rotate(weekdaysTemp, firstDayOfTheWeekHelper?.dailyTaskFormOffset ?: 0)
            weekdays = weekdaysTemp.toTypedArray()

            for (i in 0..6) {
                val weekdayRow = layoutInflater.inflate(R.layout.row_checklist, this.frequencyContainer, false)
                val checkbox = weekdayRow.findViewById<View>(R.id.checkbox) as? CheckBox
                checkbox?.text = weekdays[i]
                checkbox?.isChecked = true
                checkbox.notNull {
                    this.weekdayCheckboxes.add(it)
                }
                this.frequencyContainer.addView(weekdayRow)
            }
        } else {
            val dayRow = layoutInflater.inflate(R.layout.row_number_picker, this.frequencyContainer, false)
            this.frequencyPicker = dayRow.findViewById<View>(R.id.numberPicker) as? NumberPicker
            this.frequencyPicker?.minValue = 1
            this.frequencyPicker?.maxValue = 366
            val tv = dayRow.findViewById<View>(R.id.label) as? TextView
            tv?.text = resources.getString(R.string.frequency_daily)
            this.frequencyContainer.addView(dayRow)
        }

        if (this.task?.isValid == true) {
            if (this.dailyFrequencySpinner.selectedItemPosition == 0) {
                val offset = firstDayOfTheWeekHelper?.dailyTaskFormOffset ?: 0
                this.weekdayCheckboxes[offset].isChecked = this.task?.repeat?.m ?: false
                this.weekdayCheckboxes[(offset + 1) % 7].isChecked = this.task?.repeat?.t ?: false
                this.weekdayCheckboxes[(offset + 2) % 7].isChecked = this.task?.repeat?.w ?: false
                this.weekdayCheckboxes[(offset + 3) % 7].isChecked = this.task?.repeat?.th ?: false
                this.weekdayCheckboxes[(offset + 4) % 7].isChecked = this.task?.repeat?.f ?: false
                this.weekdayCheckboxes[(offset + 5) % 7].isChecked = this.task?.repeat?.s ?: false
                this.weekdayCheckboxes[(offset + 6) % 7].isChecked = this.task?.repeat?.su ?: false
            } else {
                this.frequencyPicker?.value = this.task?.everyX ?: 0
            }
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_save, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_save_changes) {
            finishActivitySuccessfully()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun populate(task: Task) {
        if (!task.isValid) {
            return
        }
        taskText.setText(task.text)
        taskNotes.setText(task.notes)
        taskValue.setText(String.format(Locale.getDefault(), "%.2f", task.value))

        for (tag in task.tags ?: emptyList<Tag>()) {
            selectedTags?.add(tag)
        }

        if (tags != null) {
            fillTagCheckboxes()
        }

        val priority = task.priority
        when {
            Math.abs(priority - 0.1) < 0.000001 -> this.taskDifficultySpinner.setSelection(0)
            Math.abs(priority - 1.0) < 0.000001 -> this.taskDifficultySpinner.setSelection(1)
            Math.abs(priority - 1.5) < 0.000001 -> this.taskDifficultySpinner.setSelection(2)
            Math.abs(priority - 2.0) < 0.000001 -> this.taskDifficultySpinner.setSelection(3)
        }

        val attribute = task.attribute
        if (attribute != null) {
            when (attribute) {
                Stats.STRENGTH -> taskAttributeSpinner.setSelection(0)
                Stats.INTELLIGENCE -> taskAttributeSpinner.setSelection(1)
                Stats.CONSTITUTION -> taskAttributeSpinner.setSelection(2)
                Stats.PERCEPTION -> taskAttributeSpinner.setSelection(3)
            }
        }

        if (task.type == Task.TYPE_HABIT) {
            positiveCheckBox.isChecked = task.up ?: false
            negativeCheckBox.isChecked = task.down ?: false
        }

        if (task.type == Task.TYPE_DAILY) {

            if (task.startDate != null) {
                startDateListener?.setCalendar(task.startDate)
            }

            if (task.frequency == "weekly") {
                this.dailyFrequencySpinner.setSelection(0)
                if (weekdayCheckboxes.size == 7) {
                    val offset = firstDayOfTheWeekHelper?.dailyTaskFormOffset ?: 0
                    this.weekdayCheckboxes[offset].isChecked = this.task?.repeat?.m ?: false
                    this.weekdayCheckboxes[(offset + 1) % 7].isChecked = this.task?.repeat?.t ?: false
                    this.weekdayCheckboxes[(offset + 2) % 7].isChecked = this.task?.repeat?.w ?: false
                    this.weekdayCheckboxes[(offset + 3) % 7].isChecked = this.task?.repeat?.th ?: false
                    this.weekdayCheckboxes[(offset + 4) % 7].isChecked = this.task?.repeat?.f ?: false
                    this.weekdayCheckboxes[(offset + 5) % 7].isChecked = this.task?.repeat?.s ?: false
                    this.weekdayCheckboxes[(offset + 6) % 7].isChecked = this.task?.repeat?.su ?: false
                }
            } else {
                this.dailyFrequencySpinner.setSelection(1)
                if (this.frequencyPicker != null) {
                    this.frequencyPicker?.value = task.everyX ?: 0
                }
            }

            populateRepeatables(task)
        }

        if (task.type == Task.TYPE_TODO) {
            if (task.dueDate != null) {
                dueDateCheckBox.isChecked = true
                dueDateListener?.setCalendar(task.dueDate)
            }
        }

        if (task.isGroupTask) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.group_tasks_edit_title)
                    .setMessage(R.string.group_tasks_edit_description)
                    .setPositiveButton(android.R.string.ok) { _, _ -> finish() }
                    .show()
        }
    }

    private fun fillTagCheckboxes() {
        for (tag in task?.tags ?: emptyList<Tag>()) {
            val position = tags?.indexOf(tag) ?: 0
            if (tagCheckBoxList?.size ?: 0 > position && position >= 0) {
                tagCheckBoxList?.get(position)?.isChecked = true
            }
        }
    }

    @Suppress("ReturnCount")
    private fun saveTask(task: Task): Boolean {

        val text = MarkdownParser.parseCompiled(taskText.text)
        if (text == null || text.isEmpty()) {
            return false
        }

        if (!task.isValid) {
            return true
        }

        taskRepository.executeTransaction(Realm.Transaction { realm ->
            try {
                task.text = text
                task.notes = MarkdownParser.parseCompiled(taskNotes.text)
            } catch (ignored: IllegalArgumentException) {

            }

            if (checklistAdapter != null) {
                val newChecklist = RealmList<ChecklistItem>()
                checklistAdapter?.checkListItems.notNull { newChecklist.addAll(realm.copyToRealmOrUpdate(it)) }
                task.checklist = newChecklist
            }
            if (remindersAdapter != null) {
                val newReminders = RealmList<RemindersItem>()
                remindersAdapter?.remindersItems.notNull { newReminders.addAll(realm.copyToRealmOrUpdate(it)) }
                task.reminders = newReminders
            }


            val taskTags = RealmList<Tag>()
            selectedTags.notNull { taskTags.addAll(it) }
            task.tags = taskTags


            task.priority = when {
                taskDifficultySpinner.selectedItemPosition == 0 -> 0.1
                taskDifficultySpinner.selectedItemPosition == 1 -> 1.0
                taskDifficultySpinner.selectedItemPosition == 2 -> 1.5
                taskDifficultySpinner.selectedItemPosition == 3 -> 2.0
                else -> { 1.0 }
            }.toFloat()

            if (!taskBasedAllocation) {
                task.attribute = Stats.STRENGTH
            } else {
                when (taskAttributeSpinner.selectedItemPosition) {
                    0 -> task.attribute = Stats.STRENGTH
                    1 -> task.attribute = Stats.INTELLIGENCE
                    2 -> task.attribute = Stats.CONSTITUTION
                    3 -> task.attribute = Stats.PERCEPTION
                }
            }

            when (task.type) {
                Task.TYPE_HABIT -> {
                    task.up = positiveCheckBox.isChecked
                    task.down = negativeCheckBox.isChecked
                }

                Task.TYPE_DAILY -> {
                    task.startDate = Date(startDateListener?.getCalendar()?.timeInMillis ?: Date().time)

                    if (this.dailyFrequencySpinner.selectedItemPosition == 0) {
                        task.frequency = "weekly"

                        var repeat = task.repeat
                        if (repeat == null) {
                            repeat = Days()
                            task.repeat = repeat
                        }

                        val offset = firstDayOfTheWeekHelper?.dailyTaskFormOffset ?: 0
                        repeat.m = this.weekdayCheckboxes[offset].isChecked
                        repeat.t = this.weekdayCheckboxes[(offset + 1) % 7].isChecked
                        repeat.w = this.weekdayCheckboxes[(offset + 2) % 7].isChecked
                        repeat.th = this.weekdayCheckboxes[(offset + 3) % 7].isChecked
                        repeat.f = this.weekdayCheckboxes[(offset + 4) % 7].isChecked
                        repeat.s = this.weekdayCheckboxes[(offset + 5) % 7].isChecked
                        repeat.su = this.weekdayCheckboxes[(offset + 6) % 7].isChecked
                    } else {
                        task.frequency = "daily"
                        task.everyX = this.frequencyPicker?.value
                    }

                    if (remoteConfigManager.repeatablesAreEnabled()) {
                        val frequency = this.repeatablesFrequencySpinner.selectedItemPosition
                        var frequencyString = ""
                        when (frequency) {
                            0 -> frequencyString = "daily"
                            1 -> frequencyString = "weekly"
                            2 -> frequencyString = "monthly"
                            3 -> frequencyString = "yearly"
                        }
                        task.frequency = frequencyString

                        task.everyX = this.repeatablesEveryXSpinner.value

                        var repeat = task.repeat
                        if (repeat == null) {
                            repeat = Days()
                            task.repeat = repeat
                        }

                        if ("weekly" == frequencyString) {
                            val offset = firstDayOfTheWeekHelper?.dailyTaskFormOffset ?: 0
                            repeat.m = this.repeatablesWeekDayCheckboxes[offset].isChecked
                            repeat.t = this.repeatablesWeekDayCheckboxes[(offset + 1) % 7].isChecked
                            repeat.w = this.repeatablesWeekDayCheckboxes[(offset + 2) % 7].isChecked
                            repeat.th = this.repeatablesWeekDayCheckboxes[(offset + 3) % 7].isChecked
                            repeat.f = this.repeatablesWeekDayCheckboxes[(offset + 4) % 7].isChecked
                            repeat.s = this.repeatablesWeekDayCheckboxes[(offset + 5) % 7].isChecked
                            repeat.su = this.repeatablesWeekDayCheckboxes[(offset + 6) % 7].isChecked
                        }

                        if ("monthly" == frequencyString) {
                            val calendar = startDateListener?.getCalendar()
                            val monthlyFreq = repeatablesOnSpinner.selectedItem.toString()

                            if (monthlyFreq == "Day of Month") {
                                val date = calendar?.get(Calendar.DATE)
                                val daysOfMonth = ArrayList<Int>()
                                date.notNull { daysOfMonth.add(it) }
                                task.setDaysOfMonth(daysOfMonth)
                                task.setWeeksOfMonth(ArrayList())
                            } else {
                                val week = calendar?.get(Calendar.WEEK_OF_MONTH)
                                val weeksOfMonth = ArrayList<Int>()
                                week.notNull { weeksOfMonth.add(it) }
                                task.setWeeksOfMonth(weeksOfMonth)
                                task.setDaysOfMonth(ArrayList())
                            }
                        }
                    }
                }

                Task.TYPE_TODO -> {
                    if (dueDateCheckBox.isChecked) {
                        task.dueDate = Date(dueDateListener?.getCalendar()?.timeInMillis ?: Date().time)
                    } else {
                        task.dueDate = null
                    }
                }

                Task.TYPE_REWARD -> {
                    val value = taskValue.text.toString()
                    if (!value.isEmpty()) {
                        val localFormat = DecimalFormat.getInstance(Locale.getDefault())
                        try {
                            task.value = localFormat.parse(value).toDouble()
                        } catch (ignored: ParseException) {
                        }

                    } else {
                        task.value = 0.0
                    }

                }
            }
        })
        return true
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View,
                                pos: Int, id: Long) {
        this.setDailyFrequencyViews()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        this.setDailyFrequencyViews()
    }

    private fun prepareSave() {
        var thisTask = task
        if (thisTask == null) {
            thisTask = Task()
            thisTask.type = taskType ?: "habit"
        }

        if (this.saveTask(thisTask) && thisTask.isValid) {
            if (!shouldSaveTask) {
                task = thisTask
                return
            }
            //send back to other elements.
            if (thisTask.id == null || thisTask.id?.isEmpty() == true) {
                taskRepository.createTaskInBackground(thisTask)
            } else {
                taskRepository.updateTaskInBackground(thisTask)
            }
            val unmanagedTask = taskRepository.getUnmanagedCopy(thisTask)
            taskAlarmManager.scheduleAlarmsForTask(unmanagedTask)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        dismissKeyboard()
        return true
    }

    override fun onBackPressed() {
        finish()
        dismissKeyboard()
    }

    private fun finishActivitySuccessfully() {
        this.prepareSave()
        finishWithSuccess()
        dismissKeyboard()
    }

    private fun finishWithSuccess() {
        val mainHandler = Handler(this.mainLooper)
        mainHandler.postDelayed({
            val resultIntent = Intent()
            resultIntent.putExtra(TaskFormActivity.TASK_TYPE_KEY, taskType)
            if (!shouldSaveTask) {
                resultIntent.putExtra(TaskFormActivity.PARCELABLE_TASK, task)
            }
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }, 500)
    }

    private fun dismissKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val currentFocus = currentFocus
        if (currentFocus != null) {
            imm?.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
        popup?.dismiss()
        popup = null
    }

    private inner class DateEditTextListener internal constructor(internal var datePickerText: EditText) : View.OnClickListener, DatePickerDialog.OnDateSetListener {
        internal var calendar: Calendar = Calendar.getInstance()
        internal var datePickerDialog: DatePickerDialog
        internal var dateFormatter: DateFormat

        init {
            this.datePickerText.setOnClickListener(this)
            this.dateFormatter = DateFormat.getDateInstance()
            this.datePickerDialog = DatePickerDialog(datePickerText.context, this,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))

            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
            val dayOfTheWeek = sharedPreferences.getString("FirstDayOfTheWeek",
                    Integer.toString(Calendar.getInstance().firstDayOfWeek))
            val firstDayOfTheWeekHelper = FirstDayOfTheWeekHelper.newInstance(Integer.parseInt(dayOfTheWeek))
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT_WATCH) {
                @Suppress("DEPRECATION")
                datePickerDialog.datePicker.calendarView.firstDayOfWeek = firstDayOfTheWeekHelper.firstDayOfTheWeek
            } else {
                datePickerDialog.datePicker.firstDayOfWeek = firstDayOfTheWeekHelper
                        .firstDayOfTheWeek
            }

            this.datePickerDialog.setButton(DialogInterface.BUTTON_NEUTRAL, resources.getString(R.string.today)) { _, _ -> setCalendar(Calendar.getInstance().time) }
            updateDateText()
        }

        override fun onClick(view: View) {
            datePickerDialog.show()
        }

        override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
            calendar.set(year, monthOfYear, dayOfMonth)
            updateDateText()
        }

        @Suppress("UnsafeCast")
        fun getCalendar(): Calendar {
            return calendar.clone() as Calendar
        }

        fun setCalendar(date: Date?) {
            calendar.time = date
            datePickerDialog.updateDate(calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH))
            updateDateText()
        }

        private fun updateDateText() {
            datePickerText.setText(dateFormatter.format(calendar.time))
        }
    }

    private inner class EmojiClickListener internal constructor(internal var view: EmojiEditText) : View.OnClickListener {

        override fun onClick(v: View) {
            if (popup?.isShowing == false) {

                if (popup?.isKeyBoardOpen == true) {
                    popup?.showAtBottom()
                    changeEmojiKeyboardIcon(true)
                } else {
                    view.isFocusableInTouchMode = true
                    view.requestFocus()
                    popup?.showAtBottomPending()
                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    inputMethodManager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
                    changeEmojiKeyboardIcon(true)
                }
            } else {
                popup?.dismiss()
                changeEmojiKeyboardIcon(false)
            }
        }
    }

    companion object {
        const val TASK_ID_KEY = "taskId"
        const val USER_ID_KEY = "userId"
        const val TASK_TYPE_KEY = "type"
        const val SHOW_TAG_SELECTION = "show_tag_selection"
        const val ALLOCATION_MODE_KEY = "allocationModeKey"
        const val SHOW_CHECKLIST = "show_checklist"

        const val PARCELABLE_TASK = "parcelable_task"
        const val SAVE_TO_DB = "saveToDb"

        // in order to disable the event handler in MainActivity
        const val SET_IGNORE_FLAG = "ignoreFlag"
    }
}
