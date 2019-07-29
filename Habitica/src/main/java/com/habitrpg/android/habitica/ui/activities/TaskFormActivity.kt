package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.core.widget.NestedScrollView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.tasks.HabitResetOption
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.tasks.form.*
import io.reactivex.functions.Consumer
import io.realm.RealmList
import java.util.*
import javax.inject.Inject


class TaskFormActivity : BaseActivity() {

    private var userScrolled: Boolean = false
    private var isSaving: Boolean = false
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var taskRepository: TaskRepository
    @Inject
    lateinit var tagRepository: TagRepository
    @Inject
    lateinit var taskAlarmManager: TaskAlarmManager

    private val toolbar: Toolbar by bindView(R.id.toolbar)
    private val scrollView: NestedScrollView by bindView(R.id.scroll_view)
    private val upperTextWrapper: LinearLayout by bindView(R.id.upper_text_wrapper)
    private val textEditText: EditText by bindView(R.id.text_edit_text)
    private val notesEditText: EditText by bindView(R.id.notes_edit_text)
    private val habitScoringButtons: HabitScoringButtonsView by bindView(R.id.habit_scoring_buttons)
    private val checklistTitleView: TextView by bindView(R.id.checklist_title)
    private val checklistContainer: ChecklistContainer by bindView(R.id.checklist_container)
    private val habitResetStreakTitleView: TextView by bindView(R.id.habit_reset_streak_title)
    private val habitResetStreakButtons: HabitResetStreakButtons by bindView(R.id.habit_reset_streak_buttons)
    private val taskSchedulingTitleView: TextView by bindView(R.id.scheduling_title)
    private val taskSchedulingControls: TaskSchedulingControls by bindView(R.id.scheduling_controls)
    private val adjustStreakWrapper: ViewGroup by bindView(R.id.adjust_streak_wrapper)
    private val adjustStreakTitleView: TextView by bindView(R.id.adjust_streak_title)
    private val habitAdjustPositiveStreakView: EditText by bindView(R.id.habit_adjust_positive_streak)
    private val habitAdjustNegativeStreakView: EditText by bindView(R.id.habit_adjust_negative_streak)
    private val remindersTitleView: TextView by bindView(R.id.reminders_title)
    private val remindersContainer: ReminderContainer by bindView(R.id.reminders_container)

    private val taskDifficultyTitleView: TextView by bindView(R.id.task_difficulty_title)
    private val taskDifficultyButtons: TaskDifficultyButtons by bindView(R.id.task_difficulty_buttons)

    private val statWrapper: ViewGroup by bindView(R.id.stat_wrapper)
    private val statStrengthButton: TextView by bindView(R.id.stat_strength_button)
    private val statIntelligenceButton: TextView by bindView(R.id.stat_intelligence_button)
    private val statConstitutionButton: TextView by bindView(R.id.stat_constitution_button)
    private val statPerceptionButton: TextView by bindView(R.id.stat_perception_button)

    private val rewardValueTitleView: TextView by bindView(R.id.reward_value_title)
    private val rewardValueFormView: RewardValueFormView by bindView(R.id.reward_value)

    private val tagsTitleView: TextView by bindView(R.id.tags_title)
    private val tagsWrapper: LinearLayout by bindView(R.id.tags_wrapper)

    private var isCreating = true
    private var isChallengeTask = false
    private var usesTaskAttributeStats = false
    private var task: Task? = null
    private var taskType: String = ""
    private var tags = listOf<Tag>()
    private var preselectedTags: ArrayList<String>? = null
    private var hasPreselectedTags = false
    private var selectedStat = Stats.STRENGTH
    set(value) {
        field = value
        setSelectedAttribute(value)
    }

    private var canSave: Boolean = false

    private var tintColor: Int = 0
    set(value) {
        field = value
        upperTextWrapper.setBackgroundColor(value)
        taskDifficultyButtons.tintColor = value
        habitScoringButtons.tintColor = value
        habitResetStreakButtons.tintColor = value
        supportActionBar?.setBackgroundDrawable(ColorDrawable(value))
        updateTagViewsColors()
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_task_form
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        tintColor = ContextCompat.getColor(this, R.color.brand_300)

        val bundle = intent.extras ?: return

        isChallengeTask = bundle.getBoolean(IS_CHALLENGE_TASK, false)

        taskType = bundle.getString(TASK_TYPE_KEY) ?: Task.TYPE_HABIT
        val taskId = bundle.getString(TASK_ID_KEY)
        preselectedTags = bundle.getStringArrayList(SELECTED_TAGS_KEY)

        compositeSubscription.add(tagRepository.getTags()
                .map { tagRepository.getUnmanagedCopy(it) }
                .subscribe(Consumer {
                    tags = it
                    setTagViews()
                }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(userRepository.getUser().subscribe(Consumer {
            usesTaskAttributeStats = it.preferences?.allocationMode == "taskbased"
            configureForm()
        }, RxErrorHandler.handleEmptyError()))


        textEditText.addTextChangedListener(OnChangeTextWatcher { _, _, _, _ ->
            checkCanSave()
        })
        statStrengthButton.setOnClickListener { selectedStat = Stats.STRENGTH }
        statIntelligenceButton.setOnClickListener { selectedStat = Stats.INTELLIGENCE }
        statConstitutionButton.setOnClickListener { selectedStat = Stats.CONSTITUTION }
        statPerceptionButton.setOnClickListener { selectedStat = Stats.PERCEPTION }
        scrollView.setOnTouchListener { view, event ->
            userScrolled = view == scrollView && (event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_MOVE)
            return@setOnTouchListener false
        }
        scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
            if (userScrolled) {
                dismissKeyboard()
            }
        }

        title = ""
        when {
            taskId != null -> {
                isCreating = false
                compositeSubscription.add(taskRepository.getUnmanagedTask(taskId).firstElement().subscribe(Consumer {
                    task = it
                    //tintColor = ContextCompat.getColor(this, it.mediumTaskColor)
                    fillForm(it)
                }, RxErrorHandler.handleEmptyError()))
            }
            bundle.containsKey(PARCELABLE_TASK) -> {
                isCreating = false
                task = bundle.getParcelable(PARCELABLE_TASK)
                task?.let { fillForm(it) }
            }
            else -> title = getString(R.string.create_task, getString(when (taskType) {
                Task.TYPE_DAILY -> R.string.daily
                Task.TYPE_TODO -> R.string.todo
                Task.TYPE_REWARD -> R.string.reward
                else -> R.string.habit
            }))
        }
        configureForm()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (isCreating) {
            menuInflater.inflate(R.menu.menu_task_create, menu)
        } else {
            menuInflater.inflate(R.menu.menu_task_edit, menu)
        }
        menu.findItem(R.id.action_save).isEnabled = canSave
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_save -> saveTask()
            R.id.action_delete -> deleteTask()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkCanSave() {
        val newCanSave = textEditText.text.isNotBlank()
        if (newCanSave != canSave) {
            invalidateOptionsMenu()
        }
        canSave = newCanSave
    }

    private fun configureForm() {
        val habitViewsVisibility = if (taskType == Task.TYPE_HABIT) View.VISIBLE else View.GONE
        habitScoringButtons.visibility = habitViewsVisibility
        habitResetStreakTitleView.visibility = habitViewsVisibility
        habitResetStreakButtons.visibility = habitViewsVisibility
        habitAdjustNegativeStreakView.visibility = habitViewsVisibility

        val habitDailyVisibility = if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_HABIT) View.VISIBLE else View.GONE
        adjustStreakTitleView.visibility = habitDailyVisibility
        adjustStreakWrapper.visibility = habitDailyVisibility
        if (taskType == Task.TYPE_HABIT) {
            habitAdjustPositiveStreakView.hint = getString(R.string.positive_habit_form)
        } else {
            habitAdjustPositiveStreakView.hint = getString(R.string.streak)
        }

        val todoDailyViewsVisibility = if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_TODO) View.VISIBLE else View.GONE

        checklistTitleView.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility
        checklistContainer.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility

        remindersTitleView.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility
        remindersContainer.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility
        remindersContainer.taskType = taskType

        taskSchedulingTitleView.visibility = todoDailyViewsVisibility
        taskSchedulingControls.visibility = todoDailyViewsVisibility
        taskSchedulingControls.taskType = taskType

        val rewardHideViews = if (taskType == Task.TYPE_REWARD) View.GONE else View.VISIBLE
        taskDifficultyTitleView.visibility = rewardHideViews
        taskDifficultyButtons.visibility = rewardHideViews

        val rewardViewsVisibility = if (taskType == Task.TYPE_REWARD) View.VISIBLE else View.GONE
        rewardValueTitleView.visibility = rewardViewsVisibility
        rewardValueFormView.visibility = rewardViewsVisibility

        tagsTitleView.visibility = if (isChallengeTask) View.GONE else View.VISIBLE
        tagsWrapper.visibility = if (isChallengeTask) View.GONE else View.VISIBLE

        statWrapper.visibility = if (usesTaskAttributeStats) View.VISIBLE else View.GONE
        if (isCreating) {
            adjustStreakTitleView.visibility = View.GONE
            adjustStreakWrapper.visibility = View.GONE
        }
    }

    private fun setTagViews() {
        tagsWrapper.removeAllViews()
        val padding = 20.dpToPx(this)
        for (tag in tags) {
            val view = CheckBox(this)
            view.setPadding(padding, view.paddingTop, view.paddingRight, view.paddingBottom)
            view.text = tag.name
            if (preselectedTags?.contains(tag.id) == true) {
                view.isChecked = true
            }
            tagsWrapper.addView(view)
        }
        setAllTagSelections()
        updateTagViewsColors()
    }

    private fun setAllTagSelections() {
        if (hasPreselectedTags) {
            tags.forEachIndexed { index, tag ->
                val view = tagsWrapper.getChildAt(index) as? CheckBox
                view?.isChecked = task?.tags?.find { it.id == tag.id } != null
            }
        } else {
            hasPreselectedTags = true
        }
    }

    private fun fillForm(task: Task) {
        if (!task.isValid) {
            return
        }
        canSave = true
        textEditText.setText(task.text)
        notesEditText.setText(task.notes)
        taskDifficultyButtons.selectedDifficulty = task.priority
        when (taskType) {
            Task.TYPE_HABIT -> {
                habitScoringButtons.isPositive = task.up ?: false
                habitScoringButtons.isNegative = task.down ?: false
                task.frequency?.let {
                    if (it.isNotBlank()) {
                        habitResetStreakButtons.selectedResetOption = HabitResetOption.valueOf(it.toUpperCase(Locale.US))
                    }
                }
                habitAdjustPositiveStreakView.setText((task.counterUp ?: 0).toString())
                habitAdjustNegativeStreakView.setText((task.counterDown ?: 0).toString())
                habitAdjustPositiveStreakView.visibility = if (task.up == true) View.VISIBLE else View.GONE
                habitAdjustNegativeStreakView.visibility = if (task.down == true) View.VISIBLE else View.GONE
                if (task.up != true && task.down != true) {
                    adjustStreakTitleView.visibility = View.GONE
                    adjustStreakWrapper.visibility = View.GONE
                }
            }
            Task.TYPE_DAILY -> {
                taskSchedulingControls.startDate = task.startDate ?: Date()
                taskSchedulingControls.frequency = task.frequency ?: Task.FREQUENCY_DAILY
                taskSchedulingControls.everyX = task.everyX ?: 1
                task.repeat?.let { taskSchedulingControls.weeklyRepeat = it }
                taskSchedulingControls.daysOfMonth = task.getDaysOfMonth()
                taskSchedulingControls.weeksOfMonth = task.getWeeksOfMonth()
                habitAdjustPositiveStreakView.setText((task.streak ?: 0).toString())
            }
            Task.TYPE_TODO -> taskSchedulingControls.dueDate = task.dueDate
            Task.TYPE_REWARD -> rewardValueFormView.value = task.value
        }
        if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_TODO) {
            task.checklist?.let { checklistContainer.checklistItems = it }
            remindersContainer.taskType = taskType
            task.reminders?.let { remindersContainer.reminders = it }
        }
        task.attribute?.let { setSelectedAttribute(it) }
        setAllTagSelections()
    }

    private fun setSelectedAttribute(attributeName: String) {
        if (!usesTaskAttributeStats) return
        configureStatsButton(statStrengthButton, attributeName == Stats.STRENGTH )
        configureStatsButton(statIntelligenceButton, attributeName == Stats.INTELLIGENCE )
        configureStatsButton(statConstitutionButton, attributeName == Stats.CONSTITUTION )
        configureStatsButton(statPerceptionButton, attributeName == Stats.PERCEPTION )
    }

    private fun configureStatsButton(button: TextView, isSelected: Boolean) {
        button.background.setTint(if (isSelected) tintColor else ContextCompat.getColor(this, R.color.taskform_gray))
        val textColorID = if (isSelected) R.color.white else R.color.gray_100
        button.setTextColor(ContextCompat.getColor(this, textColorID))
    }

    private fun updateTagViewsColors() {
        tagsWrapper.children.forEach { view ->
            val tagView = view as? AppCompatCheckBox
            val colorStateList = ColorStateList(
                    arrayOf(intArrayOf(-android.R.attr.state_checked), // unchecked
                            intArrayOf(android.R.attr.state_checked)  // checked
                    ),
                    intArrayOf(ContextCompat.getColor(this, R.color.gray_400), tintColor)
            )
            tagView?.buttonTintList = colorStateList
        }
    }

    private fun saveTask() {
        if (isSaving) {
            return
        }
        isSaving = true
        var thisTask = task
        if (thisTask == null) {
            thisTask = Task()
            thisTask.type = taskType
            thisTask.dateCreated = Date()
        }
        thisTask.text = textEditText.text.toString()
        thisTask.notes = notesEditText.text.toString()
        thisTask.priority = taskDifficultyButtons.selectedDifficulty
        if (usesTaskAttributeStats) {
            thisTask.attribute = selectedStat
        }
        if (taskType == Task.TYPE_HABIT) {
            thisTask.up = habitScoringButtons.isPositive
            thisTask.down = habitScoringButtons.isNegative
            thisTask.frequency = habitResetStreakButtons.selectedResetOption.value
            if (habitAdjustPositiveStreakView.text.isNotEmpty()) thisTask.counterUp = habitAdjustPositiveStreakView.text.toString().toInt()
            if (habitAdjustNegativeStreakView.text.isNotEmpty()) thisTask.counterDown = habitAdjustNegativeStreakView.text.toString().toInt()
        } else if (taskType == Task.TYPE_DAILY) {
            thisTask.startDate = taskSchedulingControls.startDate
            thisTask.everyX = taskSchedulingControls.everyX
            thisTask.frequency = taskSchedulingControls.frequency
            thisTask.repeat = taskSchedulingControls.weeklyRepeat
            thisTask.setDaysOfMonth(taskSchedulingControls.daysOfMonth)
            thisTask.setWeeksOfMonth(taskSchedulingControls.weeksOfMonth)
            if (habitAdjustPositiveStreakView.text.isNotEmpty()) thisTask.streak = habitAdjustPositiveStreakView.text.toString().toInt()
        } else if (taskType == Task.TYPE_TODO) {
            thisTask.dueDate = taskSchedulingControls.dueDate
        } else if (taskType == Task.TYPE_REWARD) {
            thisTask.value = rewardValueFormView.value
        }

        val resultIntent = Intent()
        resultIntent.putExtra(TASK_TYPE_KEY, taskType)
        if (!isChallengeTask) {
            if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_TODO) {
                thisTask.checklist = checklistContainer.checklistItems
                thisTask.reminders = remindersContainer.reminders
            }
            thisTask.tags = RealmList()
            tagsWrapper.forEachIndexed { index, view ->
                val tagView = view as? CheckBox
                if (tagView?.isChecked == true) {
                    thisTask.tags?.add(tags[index])
                }
            }

            if (isCreating) {
                taskRepository.createTaskInBackground(thisTask)
            } else {
                taskRepository.updateTaskInBackground(thisTask)
            }

            if (thisTask.type == Task.TYPE_DAILY || thisTask.type == Task.TYPE_TODO) {
                taskAlarmManager.scheduleAlarmsForTask(thisTask)
            }
        } else {
                resultIntent.putExtra(PARCELABLE_TASK, thisTask)
        }

        val mainHandler = Handler(this.mainLooper)
        mainHandler.postDelayed({
            setResult(Activity.RESULT_OK, resultIntent)
            dismissKeyboard()
            finish()
        }, 500)
    }

    private fun deleteTask() {
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(R.string.are_you_sure)
        alert.addButton(R.string.delete_task, true) { _, _ ->
            if (task?.isValid != true) return@addButton
            task?.id?.let { taskRepository.deleteTask(it).subscribe(Consumer {  }, RxErrorHandler.handleEmptyError()) }
            dismissKeyboard()
            finish()
        }
        alert.addCancelButton()
        alert.show()
    }

    private fun dismissKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val currentFocus = currentFocus
        if (currentFocus != null && !habitAdjustPositiveStreakView.isFocused && !habitAdjustNegativeStreakView.isFocused) {
            imm?.hideSoftInputFromWindow(currentFocus.windowToken, 0)
        }
    }

    companion object {
        val SELECTED_TAGS_KEY = "selectedTags"
        const val TASK_ID_KEY = "taskId"
        const val USER_ID_KEY = "userId"
        const val TASK_TYPE_KEY = "type"
        const val IS_CHALLENGE_TASK = "isChallengeTask"

        const val PARCELABLE_TASK = "parcelable_task"

        // in order to disable the event handler in MainActivity
        const val SET_IGNORE_FLAG = "ignoreFlag"
    }
}