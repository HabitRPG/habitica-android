package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.CheckBox
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.toMutableStateList
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.children
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
import androidx.core.view.iterator
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.TaskRepository
import com.habitrpg.android.habitica.databinding.ActivityTaskFormBinding
import com.habitrpg.android.habitica.extensions.OnChangeTextWatcher
import com.habitrpg.android.habitica.extensions.addCancelButton
import com.habitrpg.android.habitica.extensions.addZeroWidthSpace
import com.habitrpg.android.habitica.extensions.removeZeroWidthSpace
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.TaskAlarmManager
import com.habitrpg.android.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskGroupPlan
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.viewmodels.TaskFormViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.showAsBottomSheet
import com.habitrpg.android.habitica.ui.views.tasks.AssignSheet
import com.habitrpg.android.habitica.ui.views.tasks.AssignedView
import com.habitrpg.android.habitica.ui.views.tasks.form.HabitScoringSelector
import com.habitrpg.android.habitica.ui.views.tasks.form.LabeledValue
import com.habitrpg.android.habitica.ui.views.tasks.form.TaskDifficultySelector
import com.habitrpg.android.habitica.ui.views.tasks.form.TaskFormSelector
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.shared.habitica.models.tasks.Attribute
import com.habitrpg.shared.habitica.models.tasks.Frequency
import com.habitrpg.shared.habitica.models.tasks.HabitResetOption
import com.habitrpg.shared.habitica.models.tasks.TaskDifficulty
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.realm.RealmList
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

class TaskFormActivity : BaseActivity() {

    private val viewModel: TaskFormViewModel by viewModels()

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

    @Inject
    lateinit var socialRepository: SocialRepository

    private var challenge: Challenge? = null

    private var isCreating = true
    private var isChallengeTask = false
    private var usesTaskAttributeStats = false
    private var task: Task? = null
    private var initialTaskInstance: Task? = null
    private var taskType: TaskType = TaskType.HABIT
    private var tags = listOf<Tag>()
    private var groupID: String? = null
    private var groupMembers = listOf<Member>()
    private var assignedIDs = mutableStateListOf<String>()
    private var taskCompletedMap = mutableStateMapOf<String, Date>()
    private var preselectedTags: ArrayList<String>? = null
    private var hasPreselectedTags = false

    private var isDiscardCancelled: Boolean = false
    private var canSave: Boolean = false

    private var tintColor: Int = 0
        set(value) {
            field = value
            binding.taskSchedulingControls.tintColor = value
            updateTagViewsColors()
        }

    override fun getLayoutResId(): Int {
        return R.layout.activity_task_form
    }

    override fun getContentView(layoutResId: Int?): View {
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
        groupID = bundle.getString(GROUP_ID_KEY)
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

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        if (forcedTheme == "taskform" || forcedTheme == "maroon") {
            ToolbarColorHelper.colorizeToolbar(binding.toolbar, this, ContextCompat.getColor(this, R.color.white))
        }
        tintColor = getThemeColor(R.attr.taskFormTint)
        val upperTintColor =
            if (forcedTheme == "taskform") getThemeColor(R.attr.taskFormTint) else getThemeColor(R.attr.colorAccent)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(upperTintColor))
        binding.upperTextWrapper.setBackgroundColor(upperTintColor)

        isChallengeTask = bundle.getBoolean(IS_CHALLENGE_TASK, false)

        taskType = TaskType.from(bundle.getString(TASK_TYPE_KEY)) ?: TaskType.HABIT
        preselectedTags = bundle.getStringArrayList(SELECTED_TAGS_KEY)

        lifecycleScope.launchCatching {
            tagRepository.getTags()
                .map { tagRepository.getUnmanagedCopy(it) }
                .collect {
                    tags = it
                    setTagViews()
                }
        }
        userViewModel.user.observe(this) {
            usesTaskAttributeStats =
                it?.preferences?.allocationMode == "taskbased" && it.preferences?.automaticAllocation == true
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
        binding.notesEditText.movementMethod = LinkMovementMethod.getInstance()
        binding.scrollView.setOnTouchListener { view, event ->
            userScrolled =
                view == binding.scrollView && (event.action == MotionEvent.ACTION_SCROLL || event.action == MotionEvent.ACTION_MOVE)
            return@setOnTouchListener false
        }
        binding.scrollView.setOnScrollChangeListener { _: NestedScrollView?, _: Int, _: Int, _: Int, _: Int ->
            if (userScrolled) {
                dismissKeyboard()
            }
        }

        if (groupID != null) {
            binding.assignView.setContent {
                HabiticaTheme {
                    AssignedView(
                        groupMembers.filter { assignedIDs.contains(it.id) },
                        taskCompletedMap,
                        HabiticaTheme.colors.windowBackgroundFor(task),
                        HabiticaTheme.colors.textPrimaryFor(task),
                        {
                            showAssignDialog()
                        },
                        showEditButton = true
                    )
                }
            }

            lifecycleScope.launchCatching {
                socialRepository.getGroupMembers(groupID ?: "").collect {
                    groupMembers = it
                }
            }

            binding.tagsTitleView.isVisible = false
            binding.tagsWrapper.isVisible = false
        } else {
            binding.assignTitleView.visibility = View.GONE
            binding.assignView.visibility = View.GONE
        }

        title = ""
        when {
            taskId != null -> {
                isCreating = false
                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                    val task =
                        taskRepository.getUnmanagedTask(taskId).firstOrNull() ?: return@launch
                    if (!task.isValid) return@launch
                    this@TaskFormActivity.task = task
                    initialTaskInstance = task
                    fillForm(task)
                    task.challengeID?.let { challengeID ->
                        lifecycleScope.launchCatching {
                            val challenge = challengeRepository.retrieveChallenge(challengeID)
                                ?: return@launchCatching
                            this@TaskFormActivity.challenge = challenge
                            binding.challengeNameView.text =
                                getString(R.string.challenge_task_name, challenge.name)
                            binding.challengeNameView.visibility = View.VISIBLE
                            disableEditingForUneditableFieldsInChallengeTask()
                        }
                    }
                }
            }
            bundle.containsKey(PARCELABLE_TASK) -> {
                isCreating = false
                task = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable(PARCELABLE_TASK, Task::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    bundle.getParcelable(PARCELABLE_TASK)
                }
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

        binding.taskDifficultyButtons.setContent {
            HabiticaTheme {
                TaskDifficultySelector(
                    viewModel.taskDifficulty.value,
                    onSelect = { viewModel.taskDifficulty.value = it })
            }
        }

        if (taskType == TaskType.HABIT) {
            binding.habitScoringButtons.setContent {
                HabiticaTheme {
                    HabitScoringSelector(
                        viewModel.habitScoringPositive.value,
                        viewModel.habitScoringNegative.value,
                        { viewModel.habitScoringPositive.value = !viewModel.habitScoringPositive.value },
                        { viewModel.habitScoringNegative.value = !viewModel.habitScoringNegative.value })
                }
            }

            binding.habitResetStreakButtons.setContent {
                HabiticaTheme {
                    TaskFormSelector(
                        viewModel.habitResetOption.value, listOf(
                            LabeledValue(getString(R.string.repeat_daily), HabitResetOption.DAILY),
                            LabeledValue(getString(R.string.weekly), HabitResetOption.WEEKLY),
                            LabeledValue(getString(R.string.monthly), HabitResetOption.MONTHLY)
                        ), { viewModel.habitResetOption.value = it }, columnSize = 3
                    )
                }
            }
        }


        binding.statsSelector.setContent {
            HabiticaTheme {
                TaskFormSelector(viewModel.selectedAttribute.value, listOf(
                    LabeledValue(getString(R.string.strength), Attribute.STRENGTH),
                    LabeledValue(getString(R.string.constitution), Attribute.CONSTITUTION),
                    LabeledValue(getString(R.string.intelligence), Attribute.INTELLIGENCE),
                    LabeledValue(getString(R.string.perception), Attribute.PERCEPTION)
                ), { viewModel.selectedAttribute.value = it })
            }
        }

        configureForm()
    }

    override fun loadTheme(sharedPreferences: SharedPreferences, forced: Boolean) {
        super.loadTheme(sharedPreferences, forced)
        val upperTintColor =
            if (forcedTheme == "taskform") getThemeColor(R.attr.taskFormTint) else getThemeColor(R.attr.colorAccent)
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
        if (forcedTheme == "taskform" || forcedTheme == "maroon") {
            menu.iterator().forEach {
                val spannable = SpannableString(it.title)
                spannable.setSpan(ForegroundColorSpan(Color.WHITE), 0, spannable.length, 0)
                it.title = spannable
            }
        }
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
        (binding.habitAdjustNegativeStreakView.parent as ViewGroup).visibility =
            habitViewsVisibility

        val habitDailyVisibility =
            if (taskType == TaskType.DAILY || taskType == TaskType.HABIT) View.VISIBLE else View.GONE
        binding.adjustStreakTitleView.visibility = habitDailyVisibility
        binding.adjustStreakWrapper.visibility = habitDailyVisibility
        if (taskType == TaskType.HABIT) {
            binding.habitAdjustPositiveInputLayout.hint = getString(R.string.positive_habit_form)
            binding.adjustStreakTitleView.text = getString(R.string.adjust_counter)
        } else {
            binding.habitAdjustPositiveInputLayout.hint = getString(R.string.streak)
            binding.adjustStreakTitleView.text = getString(R.string.adjust_streak)
        }

        val todoDailyViewsVisibility =
            if (taskType == TaskType.DAILY || taskType == TaskType.TODO) View.VISIBLE else View.GONE

        binding.checklistTitleView.visibility =
            if (isChallengeTask) View.GONE else todoDailyViewsVisibility
        binding.checklistContainer.visibility =
            if (isChallengeTask) View.GONE else todoDailyViewsVisibility

        binding.remindersTitleView.visibility =
            if (isChallengeTask) View.GONE else todoDailyViewsVisibility
        binding.remindersContainer.visibility =
            if (isChallengeTask) View.GONE else todoDailyViewsVisibility
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

            if (groupID != null) {
                binding.habitResetStreakTitleView.visibility = View.GONE
                binding.habitResetStreakButtons.visibility = View.GONE
            }
        }
    }

    private fun setTagViews() {
        binding.tagsWrapper.removeAllViews()
        val padding = 20.dpToPx(this)
        for (tag in tags) {
            val view = CheckBox(this)
            view.setPadding(padding, view.paddingTop, view.paddingRight, view.paddingBottom)
            view.text = tag.name
            view.setTextColor(getThemeColor(R.attr.colorPrimaryDark))
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
        binding.notesEditText.setText(task.notes?.addZeroWidthSpace())
        viewModel.taskDifficulty.value = TaskDifficulty.valueOf(task.priority)
        when (taskType) {
            TaskType.HABIT -> {
                viewModel.habitScoringPositive.value = task.up ?: false
                viewModel.habitScoringNegative.value = task.down ?: false
                task.frequency?.let {
                    viewModel.habitResetOption.value =
                        HabitResetOption.from(it) ?: HabitResetOption.DAILY
                }
                binding.habitAdjustPositiveStreakView.setText((task.counterUp ?: 0).toString())
                binding.habitAdjustNegativeStreakView.setText((task.counterDown ?: 0).toString())
                (binding.habitAdjustPositiveStreakView.parent as ViewGroup).visibility =
                    if (task.up == true) View.VISIBLE else View.GONE
                (binding.habitAdjustNegativeStreakView.parent as ViewGroup).visibility =
                    if (task.down == true) View.VISIBLE else View.GONE
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
        task.attribute?.let { viewModel.selectedAttribute.value = it }

        if (task.isGroupTask) {
            (binding.habitAdjustPositiveStreakView.parent as ViewGroup).visibility = View.GONE
            (binding.habitAdjustNegativeStreakView.parent as ViewGroup).visibility = View.GONE
            binding.statWrapper.visibility = View.GONE
            binding.habitResetStreakTitleView.visibility = View.GONE
            binding.habitResetStreakButtons.visibility = View.GONE

            assignedIDs =
                task.group?.assignedUsersDetail?.map { it.assignedUserID }?.filterNotNull()
                    ?.toMutableStateList() ?: mutableStateListOf()
            task.group?.assignedUsersDetail?.forEach {
                it.completedDate?.let { date ->
                    taskCompletedMap[it.assignedUserID ?: ""] = date
                }
            }
        }

        setAllTagSelections()
    }

    private fun updateTagViewsColors() {
        binding.tagsWrapper.children.forEach { view ->
            val tagView = view as? AppCompatCheckBox
            val colorStateList = ColorStateList(
                arrayOf(
                    intArrayOf(-android.R.attr.state_checked), // unchecked
                    intArrayOf(android.R.attr.state_checked) // checked
                ),
                intArrayOf(getThemeColor(R.attr.colorTintedBackgroundOffset), tintColor)
            )
            tagView?.buttonTintList = colorStateList
        }
    }

    private fun configureTask(thisTask: Task): Task {
        thisTask.type = taskType
        thisTask.dateCreated = Date()

        thisTask.text = binding.textEditText.text.toString()
        thisTask.notes = binding.notesEditText.text.toString().removeZeroWidthSpace()
        thisTask.priority = viewModel.taskDifficulty.value.value
        if (usesTaskAttributeStats) {
            thisTask.attribute = viewModel.selectedAttribute.value
        }
        if (taskType == TaskType.HABIT) {
            thisTask.up = viewModel.habitScoringPositive.value
            thisTask.down = viewModel.habitScoringNegative.value
            thisTask.frequency = viewModel.habitResetOption.value.value
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

        if (groupID != null) {
            if (thisTask.group?.groupID != groupID) {
                thisTask.group = TaskGroupPlan()
                thisTask.group?.groupID = groupID
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

        val assignChanges = mapOf(
            "assign" to mutableListOf<String>(),
            "unassign" to mutableListOf<String>()
        )
        if (thisTask.isGroupTask) {
            for (id in assignedIDs) {
                if (thisTask.group?.assignedUsersDetail?.firstOrNull { it.assignedUserID == id } == null) {
                    assignChanges["assign"]?.add(id)
                }
            }
            for (details in thisTask.group?.assignedUsersDetail ?: listOf()) {
                if (assignedIDs.firstOrNull { it == details.assignedUserID } == null) {
                    details.assignedUserID?.let { assignChanges["unassign"]?.add(it) }
                }
            }
        }

        if (!isChallengeTask) {
            if (isCreating) {
                taskRepository.createTaskInBackground(thisTask, assignChanges)
            } else {
                taskRepository.updateTaskInBackground(thisTask, assignChanges)
            }
            if (isDiscardCancelled) {
                analyticsManager.logEvent("back_to_task", bundleOf(Pair("is_creating", isCreating)))
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
            task?.id?.let {
                lifecycleScope.launchCatching {
                    taskRepository.deleteTask(it)
                }
            }
            finish()
        }
        alert.addCancelButton()
        alert.show()
    }

    private fun showChallengeDeleteTask() {
        lifecycleScope.launchCatching {
            val tasks = taskRepository.getTasksForChallenge(task?.challengeID).firstOrNull()
                ?: return@launchCatching
            val taskCount = tasks.size
            val alert = HabiticaAlertDialog(this@TaskFormActivity)
            alert.setTitle(getString(R.string.delete_challenge_task_title))
            alert.setMessage(
                getString(
                    R.string.delete_challenge_task_description,
                    taskCount,
                    challenge?.name ?: ""
                )
            )
            alert.addButton(
                R.string.leave_delete_task,
                isPrimary = true,
                isDestructive = true
            ) { _, _ ->
                challenge?.let {
                    lifecycleScope.launchCatching {
                        challengeRepository.leaveChallenge(it, "keep-all")
                        taskRepository.deleteTask(task?.id ?: "")
                        userRepository.retrieveUser(true, true)
                    }
                }
            }
            alert.addButton(
                getString(R.string.leave_delete_x_tasks, taskCount),
                isPrimary = false,
                isDestructive = true
            ) { _, _ ->
                challenge?.let {
                    lifecycleScope.launchCatching {
                        challengeRepository.leaveChallenge(it, "remove-all")
                        userRepository.retrieveUser(true, true)
                    }
                }
            }
            alert.setExtraCloseButtonVisibility(View.VISIBLE)
            alert.show()
        }
    }

    private fun showBrokenChallengeDialog() {
        val task = task ?: return
        if (!task.isValid) {
            return
        }
        lifecycleScope.launchCatching {
            val tasks = taskRepository.getTasksForChallenge(task.challengeID).firstOrNull()
                ?: return@launchCatching
            val taskCount = tasks.size
            val dialog = HabiticaAlertDialog(this@TaskFormActivity)
            dialog.setTitle(R.string.broken_challenge)
            dialog.setMessage(
                getString(
                    R.string.broken_challenge_description,
                    taskCount
                )
            )
            dialog.addButton(
                getString(R.string.keep_x_tasks, taskCount),
                true
            ) { _, _ ->
                lifecycleScope.launchCatching {
                    taskRepository.unlinkAllTasks(task.challengeID, "keep-all")
                    userRepository.retrieveUser(true, true)
                }
            }
            dialog.addButton(
                getString(R.string.delete_x_tasks, taskCount),
                false,
                true
            ) { _, _ ->
                lifecycleScope.launchCatching {

                    taskRepository.unlinkAllTasks(task.challengeID, "remove-all")
                    userRepository.retrieveUser(true, true)
                }
            }
            dialog.setExtraCloseButtonVisibility(View.VISIBLE)
            dialog.show()
        }
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

    private fun showAssignDialog() {
        showAsBottomSheet { onClose ->
            AssignSheet(
                groupMembers,
                assignedIDs,
                {
                    if (assignedIDs.contains(it)) {
                        assignedIDs.remove(it)
                    } else {
                        assignedIDs.add(it)
                    }
                },
                onClose
            )
        }
    }

    companion object {
        const val SELECTED_TAGS_KEY = "selectedTags"
        const val TASK_ID_KEY = "taskId"
        const val GROUP_ID_KEY = "groupId"
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
