package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.databinding.ActivityTaskFormBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.HabitResetOption
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.realm.RealmList
import java.util.Date
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class TaskFormActivity : BaseActivity() {

    private lateinit var binding: ActivityTaskFormBinding
    private var userScrolled: Boolean = false
    private var isSaving: Boolean = false
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
    @Inject
    lateinit var userViewModel: MainUserViewModel

    private var challenge: Challenge? = null

    private var isCreating = true
    private var isChallengeTask = false
    private var usesTaskAttributeStats = false
    private var task: Task? = null
    private var initialTaskInstance: Task? = null
    private var taskType: TaskType = TaskType.HABIT
    private var tags = listOf<Tag>()
    private var preselectedTags: ArrayList<String>? = null
    private var hasPreselectedTags = false
    private var selectedStat = Attribute.STRENGTH
        set(value) {
            field = value
            setSelectedAttribute(value)
        }

    private var isDiscardCancelled: Boolean = false
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

        taskType = TaskType.from(bundle.getString(TASK_TYPE_KEY)) ?: TaskType.HABIT
        preselectedTags = bundle.getStringArrayList(SELECTED_TAGS_KEY)

        compositeSubscription.add(
            tagRepository.getTags()
                .map { tagRepository.getUnmanagedCopy(it) }
                .subscribe(
                    {
                        tags = it
                        setTagViews()
                    },
                    ExceptionHandler.rx()
                )
        )
        userViewModel.user.observe(this) {
            usesTaskAttributeStats = it?.preferences?.allocationMode == "taskbased" && it.preferences?.automaticAllocation == true
            configureForm()
        }

        binding.textEditText.addTextChangedListener(
            OnChangeTextWatcher { _, _, _, _ ->
                checkCanSave()
            }
        )
        binding.textEditText.onFocusChangeListener = View.OnFocusChangeListener { _, isFocused ->
            binding.textInputLayout.alpha = if (isFocused) 0.8f else 0.6f
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
        binding.notesEditText.onFocusChangeListener = View.OnFocusChangeListener { _, isFocused ->
            binding.notesInputLayout.alpha = if (isFocused) 0.8f else 0.6f
        }
        binding.statStrengthButton.setOnClickListener { selectedStat = Attribute.STRENGTH }
        binding.statIntelligenceButton.setOnClickListener { selectedStat = Attribute.INTELLIGENCE }
        binding.statConstitutionButton.setOnClickListener { selectedStat = Attribute.CONSTITUTION }
        binding.statPerceptionButton.setOnClickListener { selectedStat = Attribute.PERCEPTION }
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
                compositeSubscription.add(
                    taskRepository.getUnmanagedTask(taskId).firstElement().subscribe(
                        {
                            if (!it.isValid) return@subscribe
                            task = it
                            initialTaskInstance = it
                            // tintColor = ContextCompat.getColor(this, it.mediumTaskColor)
                            fillForm(it)
                            it.challengeID?.let { challengeID ->
                                compositeSubscription.add(
                                    challengeRepository.retrieveChallenge(challengeID)
                                        .subscribe(
                                            { challenge ->
                                                this.challenge = challenge
                                                binding.challengeNameView.text = getString(R.string.challenge_task_name, challenge.name)
                                                binding.challengeNameView.visibility = View.VISIBLE
                                                disableEditingForUneditableFieldsInChallengeTask()
                                            },
                                            ExceptionHandler.rx()
                                        )
                                )
                            }
                        },
                        ExceptionHandler.rx()
                    )
                )
            }
            bundle.containsKey(PARCELABLE_TASK) -> {
                isCreating = false
                task = bundle.getParcelable(PARCELABLE_TASK)
                task?.let { fillForm(it) }
            }
            else -> {
                title = getString(
                    R.string.create_task,
                    getString(
                        when (taskType) {
                            TaskType.DAILY -> R.string.daily
                            TaskType.TODO -> R.string.todo
                            TaskType.REWARD -> R.string.reward
                            else -> R.string.habit
                        }
                    )
                )
                initialTaskInstance = configureTask(Task())
            }
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

    override fun onBackPressed() {
        val currentTaskInstance = configureTask(Task())
        if (initialTaskInstance?.isBeingEdited(currentTaskInstance) == true) {
            val alert = HabiticaAlertDialog(this)
            alert.setTitle(R.string.unsaved_changes)
            alert.setMessage(R.string.discard_changes_to_task_message)
            alert.addButton(R.string.discard, true, true) { _, _ ->
                analyticsManager.logEvent("discard_task", bundleOf(Pair("is_creating", isCreating)))
                super.onBackPressed()
            }
            alert.addButton(R.string.cancel, false) { _, _ ->
                isDiscardCancelled = true
                alert.dismiss()
            }
            alert.setOnDismissListener {
                    isDiscardCancelled = true
                }
            alert.show()
        } else {
            super.onBackPressed()
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
        val habitViewsVisibility = if (taskType == TaskType.HABIT) View.VISIBLE else View.GONE
        binding.habitScoringButtons.visibility = habitViewsVisibility
        binding.habitResetStreakTitleView.visibility = habitViewsVisibility
        binding.habitResetStreakButtons.visibility = habitViewsVisibility
        (binding.habitAdjustNegativeStreakView.parent as ViewGroup).visibility = habitViewsVisibility
        if (taskType == TaskType.HABIT) {
            binding.habitScoringButtons.isPositive = true
            binding.habitScoringButtons.isNegative = false
        }

        val habitDailyVisibility = if (taskType == TaskType.DAILY || taskType == TaskType.HABIT) View.VISIBLE else View.GONE
        binding.adjustStreakTitleView.visibility = habitDailyVisibility
        binding.adjustStreakWrapper.visibility = habitDailyVisibility
        if (taskType == TaskType.HABIT) {
            binding.habitAdjustPositiveInputLayout.hint = getString(R.string.positive_habit_form)
            binding.adjustStreakTitleView.text = getString(R.string.adjust_counter)
        } else {
            binding.habitAdjustPositiveInputLayout.hint = getString(R.string.streak)
            binding.adjustStreakTitleView.text = getString(R.string.adjust_streak)
        }

        val todoDailyViewsVisibility = if (taskType == TaskType.DAILY || taskType == TaskType.TODO) View.VISIBLE else View.GONE

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

        val rewardHideViews = if (taskType == TaskType.REWARD) View.GONE else View.VISIBLE
        binding.taskDifficultyTitleView.visibility = rewardHideViews
        binding.taskDifficultyButtons.visibility = rewardHideViews

        val rewardViewsVisibility = if (taskType == TaskType.REWARD) View.VISIBLE else View.GONE
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
            TaskType.HABIT -> {
                binding.habitScoringButtons.isPositive = task.up ?: false
                binding.habitScoringButtons.isNegative = task.down ?: false
                task.frequency?.let {
                    binding.habitResetStreakButtons.selectedResetOption = HabitResetOption.from(it) ?: HabitResetOption.DAILY
                }
                binding.habitAdjustPositiveStreakView.setText((task.counterUp ?: 0).toString())
                binding.habitAdjustNegativeStreakView.setText((task.counterDown ?: 0).toString())
                (binding.habitAdjustPositiveStreakView.parent as ViewGroup).visibility = if (task.up == true) View.VISIBLE else View.GONE
                (binding.habitAdjustNegativeStreakView.parent as ViewGroup).visibility = if (task.down == true) View.VISIBLE else View.GONE
                if (task.up != true && task.down != true) {
                    binding.adjustStreakTitleView.visibility = View.GONE
                    binding.adjustStreakWrapper.visibility = View.GONE
                }
            }
            TaskType.DAILY -> {
                binding.taskSchedulingControls.startDate = task.startDate ?: Date()
                binding.taskSchedulingControls.everyX = task.everyX ?: 1
                task.repeat?.let { binding.taskSchedulingControls.weeklyRepeat = it }
                binding.taskSchedulingControls.daysOfMonth = task.getDaysOfMonth()
                binding.taskSchedulingControls.weeksOfMonth = task.getWeeksOfMonth()
                binding.habitAdjustPositiveStreakView.setText((task.streak ?: 0).toString())
                binding.taskSchedulingControls.frequency = task.frequency ?: Frequency.DAILY
            }
            TaskType.TODO -> binding.taskSchedulingControls.dueDate = task.dueDate
            TaskType.REWARD -> binding.rewardValue.value = task.value
        }
        if (taskType == TaskType.DAILY || taskType == TaskType.TODO) {
            task.checklist?.let { binding.checklistContainer.checklistItems = it }
            binding.remindersContainer.taskType = taskType
            task.reminders?.let { binding.remindersContainer.reminders = it }
        }
        task.attribute?.let { selectedStat = it }
        setAllTagSelections()
    }

    private fun setSelectedAttribute(attributeName: Attribute) {
        if (!usesTaskAttributeStats) return
        configureStatsButton(binding.statStrengthButton, attributeName == Attribute.STRENGTH)
        configureStatsButton(binding.statIntelligenceButton, attributeName == Attribute.INTELLIGENCE)
        configureStatsButton(binding.statConstitutionButton, attributeName == Attribute.CONSTITUTION)
        configureStatsButton(binding.statPerceptionButton, attributeName == Attribute.PERCEPTION)
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
                arrayOf(
                    intArrayOf(-android.R.attr.state_checked), // unchecked
                    intArrayOf(android.R.attr.state_checked) // checked
                ),
                intArrayOf(ContextCompat.getColor(this, R.color.text_dimmed), tintColor)
            )
            tagView?.buttonTintList = colorStateList
        }
    }

    private fun configureTask(thisTask: Task): Task {
        thisTask.type = taskType
        thisTask.dateCreated = Date()

        thisTask.text = binding.textEditText.text.toString()
        thisTask.notes = binding.notesEditText.text.toString()
        thisTask.priority = binding.taskDifficultyButtons.selectedDifficulty
        if (usesTaskAttributeStats) {
            thisTask.attribute = selectedStat
        }
        if (taskType == TaskType.HABIT) {
            thisTask.up = binding.habitScoringButtons.isPositive
            thisTask.down = binding.habitScoringButtons.isNegative
            thisTask.frequency = binding.habitResetStreakButtons.selectedResetOption.value
            if (binding.habitAdjustPositiveStreakView.text?.isNotEmpty() == true) thisTask.counterUp =
                binding.habitAdjustPositiveStreakView.text.toString().toIntCatchOverflow()
            if (binding.habitAdjustNegativeStreakView.text?.isNotEmpty() == true) thisTask.counterDown =
                binding.habitAdjustNegativeStreakView.text.toString().toIntCatchOverflow()
        } else if (taskType == TaskType.DAILY) {
            thisTask.startDate = binding.taskSchedulingControls.startDate
            thisTask.everyX = binding.taskSchedulingControls.everyX
            thisTask.frequency = binding.taskSchedulingControls.frequency
            thisTask.repeat = binding.taskSchedulingControls.weeklyRepeat
            thisTask.setDaysOfMonth(binding.taskSchedulingControls.daysOfMonth)
            thisTask.setWeeksOfMonth(binding.taskSchedulingControls.weeksOfMonth)
            if (binding.habitAdjustPositiveStreakView.text?.isNotEmpty() == true) thisTask.streak =
                binding.habitAdjustPositiveStreakView.text.toString().toIntCatchOverflow()
        } else if (taskType == TaskType.TODO) {
            thisTask.dueDate = binding.taskSchedulingControls.dueDate
        } else if (taskType == TaskType.REWARD) {
            thisTask.value = binding.rewardValue.value
        }
        if (!isChallengeTask) {
            if (taskType == TaskType.DAILY || taskType == TaskType.TODO) {
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
        }

        return thisTask
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

        thisTask = configureTask(thisTask)

        val resultIntent = Intent()
        resultIntent.putExtra(TASK_TYPE_KEY, taskType.value)
        if (!isChallengeTask) {
            if (isCreating) {
                if (isDiscardCancelled) {
                    analyticsManager.logEvent("back_to_task", bundleOf(Pair("is_creating", isCreating)))
                }
                taskRepository.createTaskInBackground(thisTask)
            } else {
                if (isDiscardCancelled) {
                    analyticsManager.logEvent("back_to_task", bundleOf(Pair("is_creating", isCreating)))
                }
                taskRepository.updateTaskInBackground(thisTask)
            }

            if (thisTask.type == TaskType.DAILY || thisTask.type == TaskType.TODO) {
                taskAlarmManager.scheduleAlarmsForTask(thisTask)
            }
        } else {
            resultIntent.putExtra(PARCELABLE_TASK, thisTask)
        }

        val mainHandler = Handler(this.mainLooper)
        mainHandler.postDelayed(
            {
                setResult(Activity.RESULT_OK, resultIntent)
                finish()
            },
            500
        )
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
            task?.id?.let { taskRepository.deleteTask(it).subscribe({ }, ExceptionHandler.rx()) }
            finish()
        }
        alert.addCancelButton()
        alert.show()
    }

    private fun showChallengeDeleteTask() {
        compositeSubscription.add(
            taskRepository.getTasksForChallenge(task?.challengeID).firstElement().subscribe(
                { tasks ->
                    val taskCount = tasks.size
                    val alert = HabiticaAlertDialog(this)
                    alert.setTitle(getString(R.string.delete_challenge_task_title))
                    alert.setMessage(getString(R.string.delete_challenge_task_description, taskCount, challenge?.name ?: ""))
                    alert.addButton(R.string.leave_delete_task, isPrimary = true, isDestructive = true) { _, _ ->
                        challenge?.let {
                            compositeSubscription.add(
                                challengeRepository.leaveChallenge(it, "keep-all")
                                    .flatMap { taskRepository.deleteTask(task?.id ?: "") }
                                    .subscribe(
                                        {
                                            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                                                userRepository.retrieveUser(true, true)
                                            }
                                            finish()
                                        },
                                        ExceptionHandler.rx()
                                    )
                            )
                        }
                    }
                    alert.addButton(getString(R.string.leave_delete_x_tasks, taskCount), isPrimary = false, isDestructive = true) { _, _ ->
                        challenge?.let {
                            compositeSubscription.add(
                                challengeRepository.leaveChallenge(it, "remove-all")
                                    .subscribe(
                                        {
                                            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                                                userRepository.retrieveUser(true, true)
                                            }
                                            finish()
                                        },
                                        ExceptionHandler.rx()
                                    )
                            )
                        }
                    }
                    alert.setExtraCloseButtonVisibility(View.VISIBLE)
                    alert.show()
                },
                ExceptionHandler.rx()
            )
        )
    }

    private fun showBrokenChallengeDialog() {
        val task = task ?: return
        if (!task.isValid) {
            return
        }
        compositeSubscription.add(
            taskRepository.getTasksForChallenge(task.challengeID).subscribe(
                { tasks ->
                    val taskCount = tasks.size
                    val dialog = HabiticaAlertDialog(this)
                    dialog.setTitle(R.string.broken_challenge)
                    dialog.setMessage(this.getString(R.string.broken_challenge_description, taskCount))
                    dialog.addButton(this.getString(R.string.keep_x_tasks, taskCount), true) { _, _ ->
                        taskRepository.unlinkAllTasks(task.challengeID, "keep-all")
                            .subscribe(
                            {
                                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                                    userRepository.retrieveUser(true, true)
                                }
                                finish()
                            },
                            ExceptionHandler.rx()
                        )
                    }
                    dialog.addButton(this.getString(R.string.delete_x_tasks, taskCount), false, true) { _, _ ->
                        taskRepository.unlinkAllTasks(task.challengeID, "remove-all")
                            .subscribe(
                            {
                                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                                    userRepository.retrieveUser(true, true)
                                }
                                finish()
                            },
                            ExceptionHandler.rx()
                        )
                    }
                    dialog.setExtraCloseButtonVisibility(View.VISIBLE)
                    dialog.show()
                },
                ExceptionHandler.rx()
            )
        )
    }

    private fun disableEditingForUneditableFieldsInChallengeTask() {
        binding.textEditText.isEnabled = false
        binding.taskDifficultyButtons.isEnabled = false
        binding.taskSchedulingControls.isEnabled = false
        binding.habitScoringButtons.isEnabled = false
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
