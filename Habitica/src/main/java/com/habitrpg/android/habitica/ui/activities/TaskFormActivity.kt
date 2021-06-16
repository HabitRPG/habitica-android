package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.core.widget.NestedScrollView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.ActivityTaskFormBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.ChecklistItem
import com.habitrpg.android.habitica.models.tasks.HabitResetOption
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.realm.RealmList
import java.util.*
import javax.inject.Inject


class TaskFormActivity : BaseActivity() {

    private lateinit var binding: ActivityTaskFormBinding
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
    @Inject
    lateinit var challengeRepository: ChallengeRepository
    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private var challenge: Challenge? = null

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
        binding.taskDifficultyButtons.tintColor = value
        binding.habitScoringButtons.tintColor = value
        binding.habitResetStreakButtons.tintColor = value
        binding.taskSchedulingControls.tintColor = value
        updateTagViewsColors()
    }

    override fun getLayoutResId(): Int {
        return R.layout.activity_task_form
    }

    override fun getContentView(): View {
        binding = ActivityTaskFormBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        overrideModernHeader = false
        val bundle = intent.extras ?: return

        val taskId = bundle.getString(TASK_ID_KEY)
        forcedIsNight = false
        forcedTheme = if (taskId != null) {
            val taskValue = bundle.getDouble(TASK_VALUE_KEY)
            when {
                taskValue < -20 -> "maroon"
                taskValue < -10 -> "red"
                taskValue < -1 -> "orange"
                taskValue < 1 -> "yellow"
                taskValue < 5 -> "green"
                taskValue < 10 -> "teal"
                else -> "blue"
            }
        } else {
            "taskform"
        }
        super.onCreate(savedInstanceState)

        if (forcedTheme == "yellow") {
            binding.taskDifficultyButtons.textTintColor = ContextCompat.getColor(this, R.color.text_yellow)
            binding.habitScoringButtons.textTintColor = ContextCompat.getColor(this, R.color.text_yellow)
        } else if (forcedTheme == "taskform") {
            binding.taskDifficultyButtons.textTintColor = ContextCompat.getColor(this, R.color.text_brand_neon)
            binding.habitScoringButtons.textTintColor = ContextCompat.getColor(this, R.color.text_brand_neon)
        }

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        tintColor = getThemeColor(R.attr.taskFormTint)
        val upperTintColor = if (forcedTheme == "taskform") getThemeColor(R.attr.taskFormTint) else getThemeColor(R.attr.colorAccent)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(upperTintColor))
        binding.upperTextWrapper.setBackgroundColor(upperTintColor)


        isChallengeTask = bundle.getBoolean(IS_CHALLENGE_TASK, false)

        taskType = bundle.getString(TASK_TYPE_KEY) ?: Task.TYPE_HABIT
        preselectedTags = bundle.getStringArrayList(SELECTED_TAGS_KEY)

        compositeSubscription.add(tagRepository.getTags()
                .map { tagRepository.getUnmanagedCopy(it) }
                .subscribe({
                    tags = it
                    setTagViews()
                }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(userRepository.getUser().subscribe({
            usesTaskAttributeStats = it.preferences?.allocationMode == "taskbased" && it.preferences?.automaticAllocation == true
            configureForm()
        }, RxErrorHandler.handleEmptyError()))


        binding.textEditText.addTextChangedListener(OnChangeTextWatcher { _, _, _, _ ->
            checkCanSave()
        })
        binding.textEditText.onFocusChangeListener = View.OnFocusChangeListener { _, isFocused ->
            binding.textInputLayout.alpha = if (isFocused) 0.8f else 0.6f
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        binding.notesEditText.onFocusChangeListener = View.OnFocusChangeListener { _, isFocused ->
            binding.notesInputLayout.alpha = if (isFocused) 0.8f else 0.6f
        }
        binding.statStrengthButton.setOnClickListener { selectedStat = Stats.STRENGTH }
        binding.statIntelligenceButton.setOnClickListener { selectedStat = Stats.INTELLIGENCE }
        binding.statConstitutionButton.setOnClickListener { selectedStat = Stats.CONSTITUTION }
        binding.statPerceptionButton.setOnClickListener { selectedStat = Stats.PERCEPTION }
        binding.scrollView.setOnTouchListener { view, event ->
            userScrolled = view == binding.scrollView && (event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_MOVE)
            return@setOnTouchListener false
        }
        binding.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
            if (userScrolled) {
                dismissKeyboard()
            }
        }

        title = ""
        when {
            taskId != null -> {
                isCreating = false
                compositeSubscription.add(taskRepository.getUnmanagedTask(taskId).firstElement().subscribe({
                    if (!it.isValid) return@subscribe
                    task = it
                    //tintColor = ContextCompat.getColor(this, it.mediumTaskColor)
                    fillForm(it)
                    it.challengeID?.let { challengeID ->
                        compositeSubscription.add(challengeRepository.retrieveChallenge(challengeID)
                                .subscribe({ challenge ->
                            this.challenge = challenge
                                    binding.challengeNameView.text = getString(R.string.challenge_task_name, challenge.name)
                                    binding.challengeNameView.visibility = View.VISIBLE
                        }, RxErrorHandler.handleEmptyError()))
                    }
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

    override fun loadTheme(sharedPreferences: SharedPreferences, forced: Boolean) {
        super.loadTheme(sharedPreferences, forced)
        val upperTintColor = if (forcedTheme == "taskform") getThemeColor(R.attr.taskFormTint) else getThemeColor(R.attr.colorAccent)
        window.statusBarColor = upperTintColor
    }

    override fun onStart() {
        super.onStart()
        if (isCreating) {
            binding.textEditText.requestFocus()
        }
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_save -> saveTask()
            R.id.action_delete -> deleteTask()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkCanSave() {
        val newCanSave = binding.textEditText.text?.isNotBlank() == true
        if (newCanSave != canSave) {
            invalidateOptionsMenu()
        }
        canSave = newCanSave
    }

    private fun configureForm() {
        val firstDayOfWeek = sharedPreferences.getString("FirstDayOfTheWeek", "-1")?.toInt() ?: -1
        val habitViewsVisibility = if (taskType == Task.TYPE_HABIT) View.VISIBLE else View.GONE
        binding.habitScoringButtons.visibility = habitViewsVisibility
        binding.habitResetStreakTitleView.visibility = habitViewsVisibility
        binding.habitResetStreakButtons.visibility = habitViewsVisibility
        binding.habitAdjustNegativeStreakView.visibility = habitViewsVisibility
        if (taskType == Task.TYPE_HABIT) {
            binding.habitScoringButtons.isPositive = true
            binding.habitScoringButtons.isNegative = false
        }

        val habitDailyVisibility = if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_HABIT) View.VISIBLE else View.GONE
        binding.adjustStreakTitleView.visibility = habitDailyVisibility
        binding.adjustStreakWrapper.visibility = habitDailyVisibility
        if (taskType == Task.TYPE_HABIT) {
            binding.habitAdjustPositiveStreakView.hint = getString(R.string.positive_habit_form)
        } else {
            binding.habitAdjustPositiveStreakView.hint = getString(R.string.streak)
        }

        val todoDailyViewsVisibility = if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_TODO) View.VISIBLE else View.GONE

        binding.checklistTitleView.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility
        binding.checklistContainer.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility

        binding.remindersTitleView.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility
        binding.remindersContainer.visibility = if (isChallengeTask) View.GONE else todoDailyViewsVisibility
        binding.remindersContainer.taskType = taskType
        binding.remindersContainer.firstDayOfWeek = firstDayOfWeek

        binding.schedulingTitleView.visibility = todoDailyViewsVisibility
        binding.taskSchedulingControls.visibility = todoDailyViewsVisibility
        binding.taskSchedulingControls.taskType = taskType
        binding.taskSchedulingControls.firstDayOfWeek = firstDayOfWeek

        val rewardHideViews = if (taskType == Task.TYPE_REWARD) View.GONE else View.VISIBLE
        binding.taskDifficultyTitleView.visibility = rewardHideViews
        binding.taskDifficultyButtons.visibility = rewardHideViews

        val rewardViewsVisibility = if (taskType == Task.TYPE_REWARD) View.VISIBLE else View.GONE
        binding.rewardValueTitleView.visibility = rewardViewsVisibility
        binding.rewardValue.visibility = rewardViewsVisibility

        binding.tagsTitleView.visibility = if (isChallengeTask) View.GONE else View.VISIBLE
        binding.tagsWrapper.visibility = if (isChallengeTask) View.GONE else View.VISIBLE

        binding.statWrapper.visibility = if (usesTaskAttributeStats) View.VISIBLE else View.GONE
        if (isCreating) {
            binding.adjustStreakTitleView.visibility = View.GONE
            binding.adjustStreakWrapper.visibility = View.GONE
        }
    }

    private fun setTagViews() {
        binding.tagsWrapper.removeAllViews()
        val padding = 20.dpToPx(this)
        for (tag in tags) {
            val view = CheckBox(this)
            view.setPadding(padding, view.paddingTop, view.paddingRight, view.paddingBottom)
            view.text = tag.name
            if (preselectedTags?.contains(tag.id) == true) {
                view.isChecked = true
            }
            binding.tagsWrapper.addView(view)
        }
        setAllTagSelections()
        updateTagViewsColors()
    }

    private fun setAllTagSelections() {
        if (hasPreselectedTags) {
            tags.forEachIndexed { index, tag ->
                val view = binding.tagsWrapper.getChildAt(index) as? CheckBox
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
        binding.textEditText.setText(task.text)
        binding.notesEditText.setText(task.notes)
        binding.taskDifficultyButtons.selectedDifficulty = task.priority
        when (taskType) {
            Task.TYPE_HABIT -> {
                binding.habitScoringButtons.isPositive = task.up ?: false
                binding.habitScoringButtons.isNegative = task.down ?: false
                task.frequency?.let {
                    if (it.isNotBlank()) {
                        binding.habitResetStreakButtons.selectedResetOption = HabitResetOption.valueOf(it.toUpperCase(Locale.US))
                    }
                }
                binding.habitAdjustPositiveStreakView.setText((task.counterUp ?: 0).toString())
                binding.habitAdjustNegativeStreakView.setText((task.counterDown ?: 0).toString())
                binding.habitAdjustPositiveStreakView.visibility = if (task.up == true) View.VISIBLE else View.GONE
                binding.habitAdjustNegativeStreakView.visibility = if (task.down == true) View.VISIBLE else View.GONE
                if (task.up != true && task.down != true) {
                    binding.adjustStreakTitleView.visibility = View.GONE
                    binding.adjustStreakWrapper.visibility = View.GONE
                }
            }
            Task.TYPE_DAILY -> {
                binding.taskSchedulingControls.startDate = task.startDate ?: Date()
                binding.taskSchedulingControls.everyX = task.everyX ?: 1
                task.repeat?.let { binding.taskSchedulingControls.weeklyRepeat = it }
                binding.taskSchedulingControls.daysOfMonth = task.getDaysOfMonth()
                binding.taskSchedulingControls.weeksOfMonth = task.getWeeksOfMonth()
                binding.habitAdjustPositiveStreakView.setText((task.streak ?: 0).toString())
                binding.taskSchedulingControls.frequency = task.frequency ?: Task.FREQUENCY_DAILY
            }
            Task.TYPE_TODO -> binding.taskSchedulingControls.dueDate = task.dueDate
            Task.TYPE_REWARD -> binding.rewardValue.value = task.value
        }
        if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_TODO) {
            task.checklist?.let { binding.checklistContainer.checklistItems = it }
            binding.remindersContainer.taskType = taskType
            task.reminders?.let { binding.remindersContainer.reminders = it }
        }
        task.attribute?.let { selectedStat = it }
        setAllTagSelections()
    }

    private fun setSelectedAttribute(attributeName: String) {
        if (!usesTaskAttributeStats) return
        configureStatsButton(binding.statStrengthButton, attributeName == Stats.STRENGTH )
        configureStatsButton(binding.statIntelligenceButton, attributeName == Stats.INTELLIGENCE )
        configureStatsButton(binding.statConstitutionButton, attributeName == Stats.CONSTITUTION )
        configureStatsButton(binding.statPerceptionButton, attributeName == Stats.PERCEPTION )
    }

    private fun configureStatsButton(button: TextView, isSelected: Boolean) {
        button.background.setTint(if (isSelected) tintColor else ContextCompat.getColor(this, R.color.taskform_gray))
        val textColorID = if (isSelected) R.color.window_background else R.color.text_secondary
        button.setTextColor(ContextCompat.getColor(this, textColorID))
        if (isSelected) {
            button.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        } else {
            button.typeface = Typeface.create("sans-serif", Typeface.NORMAL)
        }
    }

    private fun updateTagViewsColors() {
        binding.tagsWrapper.children.forEach { view ->
            val tagView = view as? AppCompatCheckBox
            val colorStateList = ColorStateList(
                    arrayOf(intArrayOf(-android.R.attr.state_checked), // unchecked
                            intArrayOf(android.R.attr.state_checked)  // checked
                    ),
                    intArrayOf(ContextCompat.getColor(this, R.color.text_dimmed), tintColor)
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
        } else {
            if (!thisTask.isValid) return
        }
        thisTask.text = binding.textEditText.text.toString()
        thisTask.notes = binding.notesEditText.text.toString()
        thisTask.priority = binding.taskDifficultyButtons.selectedDifficulty
        if (usesTaskAttributeStats) {
            thisTask.attribute = selectedStat
        }
        if (taskType == Task.TYPE_HABIT) {
            thisTask.up = binding.habitScoringButtons.isPositive
            thisTask.down = binding.habitScoringButtons.isNegative
            thisTask.frequency = binding.habitResetStreakButtons.selectedResetOption.value
            if (binding.habitAdjustPositiveStreakView.text?.isNotEmpty() == true) thisTask.counterUp = binding.habitAdjustPositiveStreakView.text.toString().toIntCatchOverflow()
            if (binding.habitAdjustNegativeStreakView.text?.isNotEmpty() == true) thisTask.counterDown = binding.habitAdjustNegativeStreakView.text.toString().toIntCatchOverflow()
        } else if (taskType == Task.TYPE_DAILY) {
            thisTask.startDate = binding.taskSchedulingControls.startDate
            thisTask.everyX = binding.taskSchedulingControls.everyX
            thisTask.frequency = binding.taskSchedulingControls.frequency
            thisTask.repeat = binding.taskSchedulingControls.weeklyRepeat
            thisTask.setDaysOfMonth(binding.taskSchedulingControls.daysOfMonth)
            thisTask.setWeeksOfMonth(binding.taskSchedulingControls.weeksOfMonth)
            if (binding.habitAdjustPositiveStreakView.text?.isNotEmpty() == true) thisTask.streak = binding.habitAdjustPositiveStreakView.text.toString().toIntCatchOverflow()
        } else if (taskType == Task.TYPE_TODO) {
            thisTask.dueDate = binding.taskSchedulingControls.dueDate
        } else if (taskType == Task.TYPE_REWARD) {
            thisTask.value = binding.rewardValue.value
        }

        val resultIntent = Intent()
        resultIntent.putExtra(TASK_TYPE_KEY, taskType)
        if (!isChallengeTask) {
            if (taskType == Task.TYPE_DAILY || taskType == Task.TYPE_TODO) {
                thisTask.checklist = RealmList()
                thisTask.checklist?.addAll(binding.checklistContainer.checklistItems)
                thisTask.reminders = RealmList()
                thisTask.reminders?.addAll(binding.remindersContainer.reminders)
            }
            thisTask.tags = RealmList()
            binding.tagsWrapper.forEachIndexed { index, view ->
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
            finish()
        }, 500)
    }

    private fun deleteTask() {
        if (task?.challengeBroken?.isNotBlank() == true) {
            showBrokenChallengeDialog()
            return
        } else if (task?.challengeID?.isNotBlank() == true) {
            showChallengeDeleteTask()
            return
        }
        val alert = HabiticaAlertDialog(this)
        alert.setTitle(R.string.are_you_sure)
        alert.addButton(R.string.delete_task, true) { _, _ ->
            if (task?.isValid != true) return@addButton
            task?.id?.let { taskRepository.deleteTask(it).subscribe({  }, RxErrorHandler.handleEmptyError()) }
            finish()
        }
        alert.addCancelButton()
        alert.show()
    }

    private fun showChallengeDeleteTask() {
        compositeSubscription.add(taskRepository.getTasksForChallenge(task?.challengeID).firstElement().subscribe({ tasks ->
            val taskCount = tasks.size
            val alert = HabiticaAlertDialog(this)
            alert.setTitle(getString(R.string.delete_challenge_task_title))
            alert.setMessage(getString(R.string.delete_challenge_task_description, taskCount, challenge?.name ?: ""))
            alert.addButton(R.string.leave_delete_task, isPrimary = true, isDestructive = true) { _, _ ->
                challenge?.let {
                    compositeSubscription.add(challengeRepository.leaveChallenge(it, "keep-all")
                            .flatMap { taskRepository.deleteTask(task?.id ?: "") }
                            .flatMap { userRepository.retrieveUser(true) }
                            .subscribe({
                                finish()
                            }, RxErrorHandler.handleEmptyError()))
                }
            }
            alert.addButton(getString(R.string.leave_delete_x_tasks, taskCount), isPrimary = false, isDestructive = true) { _, _ ->
                challenge?.let {
                    compositeSubscription.add(challengeRepository.leaveChallenge(it, "remove-all")
                            .flatMap { userRepository.retrieveUser(true) }
                            .subscribe({
                        finish()
                    }, RxErrorHandler.handleEmptyError()))
                }
            }
            alert.setExtraCloseButtonVisibility(View.VISIBLE)
            alert.show()
        }, RxErrorHandler.handleEmptyError()))
    }

    private fun showBrokenChallengeDialog() {
        val task = task ?: return
        if (!task.isValid) {
            return
        }
        compositeSubscription.add(taskRepository.getTasksForChallenge(task.challengeID).subscribe({ tasks ->
            val taskCount = tasks.size
            val dialog = HabiticaAlertDialog(this)
            dialog.setTitle(R.string.broken_challenge)
            dialog.setMessage(this.getString(R.string.broken_challenge_description, taskCount))
            dialog.addButton(this.getString(R.string.keep_x_tasks, taskCount), true) { _, _ ->
                taskRepository.unlinkAllTasks(task.challengeID, "keep-all").subscribe({
                    finish()
                }, RxErrorHandler.handleEmptyError())
            }
            dialog.addButton(this.getString(R.string.delete_x_tasks, taskCount), false, true) { _, _ ->
                taskRepository.unlinkAllTasks(task.challengeID, "remove-all").subscribe({
                    finish()
                }, RxErrorHandler.handleEmptyError())
            }
            dialog.setExtraCloseButtonVisibility(View.VISIBLE)
            dialog.show()
        }, RxErrorHandler.handleEmptyError()))
    }


    override fun finish() {
        dismissKeyboard()
        super.finish()
    }

    companion object {
        const val SELECTED_TAGS_KEY = "selectedTags"
        const val TASK_ID_KEY = "taskId"
        const val TASK_VALUE_KEY = "taskValue"
        const val USER_ID_KEY = "userId"
        const val TASK_TYPE_KEY = "type"
        const val IS_CHALLENGE_TASK = "isChallengeTask"

        const val PARCELABLE_TASK = "parcelable_task"

        // in order to disable the event handler in MainActivity
        const val SET_IGNORE_FLAG = "ignoreFlag"
    }
}

private fun String.toIntCatchOverflow(): Int? {
    return try {
        toInt()
    } catch (e: NumberFormatException) {
        0
    }
}
