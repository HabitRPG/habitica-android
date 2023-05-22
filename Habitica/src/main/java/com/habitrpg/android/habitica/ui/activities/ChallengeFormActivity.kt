package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.ActivityCreateChallengeBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengeTasksRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengesOverviewFragmentDirections
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.viewmodels.TasksViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaProgressDialog
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.shared.habitica.models.tasks.TaskType
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@AndroidEntryPoint
class ChallengeFormActivity : BaseActivity() {

    private lateinit var binding: ActivityCreateChallengeBinding

    @Inject
    internal lateinit var challengeRepository: ChallengeRepository

    @Inject
    internal lateinit var socialRepository: SocialRepository

    @Inject
    internal lateinit var userViewModel: MainUserViewModel

    lateinit var tasksViewModel: TasksViewModel

    private lateinit var challengeTasks: ChallengeTasksRecyclerViewAdapter

    private lateinit var locationAdapter: GroupArrayAdapter
    private var challengeId: String? = null
    private var groupID: String? = null
    private var editMode: Boolean = false

    private val addedTasks = HashMap<String, Task>()
    private val updatedTasks = HashMap<String, Task>()
    private val removedTasks = HashMap<String, Task>()

    override var overrideModernHeader: Boolean? = true

    // Add {*} Items
    private lateinit var addHabit: Task
    private lateinit var addDaily: Task
    private lateinit var addTodo: Task
    private lateinit var addReward: Task
    private var user: User? = null

    private var savingInProgress = false

    override fun getContentView(layoutResId: Int?): View {
        binding = ActivityCreateChallengeBinding.inflate(layoutInflater)
        return binding.root
    }

    private val challengeData: Challenge
        get() {
            val c = Challenge()

            val locationPos = binding.challengeLocationSpinner.selectedItemPosition

            if (challengeId != null) {
                c.id = challengeId
            }

            if (groupID != null) {
                c.groupId = groupID
            } else {
                if (locationAdapter.count > locationPos && locationPos >= 0) {
                    val locationGroup = locationAdapter.getItem(locationPos)
                    if (locationGroup != null) {
                        c.groupId = locationGroup.id
                    }
                }
            }
            c.name = binding.createChallengeTitle.text.toString()
            c.description = binding.createChallengeDescription.text.toString()
            c.shortName = binding.createChallengeTag.text.toString()
            c.prize = Integer.parseInt(binding.createChallengePrize.text.toString())

            return c
        }

    override fun getLayoutResId(): Int {
        return R.layout.activity_create_challenge
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_challenge, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save && !savingInProgress && validateAllFields()) {
            savingInProgress = true
            val dialog = HabiticaProgressDialog.show(this, R.string.saving)

            lifecycleScope.launchCatching({
                dialog?.dismiss()
                savingInProgress = false
                ExceptionHandler.reportError(it)
            }) {
                val challenge = if (editMode) {
                    updateChallenge()
                } else {
                    createChallenge()
                }

                challengeId = challenge?.id
                challengeRepository.retrieveChallenges(0, true)

                dialog?.dismiss()
                savingInProgress = false
                finish()
                if (!editMode) {
                    lifecycleScope.launch(context = Dispatchers.Main) {
                        delay(500L)
                        MainNavigationController.navigate(
                            ChallengesOverviewFragmentDirections.openChallengeDetail(
                                challengeId ?: ""
                            )
                        )
                    }
                }
            }
        } else if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    private fun validateAllFields(): Boolean {
        val errorMessages = ArrayList<String>()

        if (getEditTextString(binding.createChallengeTitle).isEmpty()) {
            val titleEmptyError = getString(R.string.challenge_create_error_title)
            binding.createChallengeTitleInputLayout.error = titleEmptyError
            errorMessages.add(titleEmptyError)
        } else {
            binding.createChallengeTitleInputLayout.isErrorEnabled = false
        }

        if (getEditTextString(binding.createChallengeTag).isEmpty()) {
            val tagEmptyError = getString(R.string.challenge_create_error_tag)

            binding.createChallengeTagInputLayout.error = tagEmptyError
            errorMessages.add(tagEmptyError)
        } else {
            binding.createChallengeTagInputLayout.isErrorEnabled = false
        }

        val prizeError = checkPrizeAndMinimumForTavern()

        if (prizeError.isNotEmpty()) {
            errorMessages.add(prizeError)
        }

        // all "Add {*}"-Buttons are one task itself, so we need atleast more than 4
        if (challengeTasks.taskList.size <= 4) {
            binding.createChallengeTaskError.visibility = View.VISIBLE
            errorMessages.add(getString(R.string.challenge_create_error_no_tasks))
        } else {
            binding.createChallengeTaskError.visibility = View.GONE
        }
        if (errorMessages.isNotEmpty()) {
            val alert = HabiticaAlertDialog(this)
            alert.setMessage(errorMessages.joinToString("\n"))
            alert.addCloseButton(true)
            alert.enqueue()
        }
        return errorMessages.size == 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        addHabit = createTask(resources.getString(R.string.add_habit))
        addDaily = createTask(resources.getString(R.string.add_daily))
        addTodo = createTask(resources.getString(R.string.add_todo))
        addReward = createTask(resources.getString(R.string.add_reward))

        super.onCreate(savedInstanceState)

        setupToolbar(findViewById(R.id.toolbar))

        val intent = intent
        val bundle = intent.extras

        tasksViewModel = ViewModelProvider(this)[TasksViewModel::class.java]

        ChallengeTasksRecyclerViewAdapter(
            tasksViewModel, 0, this, "",
            openTaskDisabled = false,
            taskActionsDisabled = true
        ).also { challengeTasks = it }

        challengeTasks.onTaskOpen = {
            if (it.isValid) {
                openNewTaskActivity(it.type, it)
            }
        }
        locationAdapter = GroupArrayAdapter(this)

        if (bundle != null) {
            challengeId = bundle.getString(CHALLENGE_ID_KEY, null)
        }

        fillControls()

        if (challengeId != null) {
            fillControlsByChallenge()
        }

        userViewModel.user.observe(this) { user = it }
        binding.gemIconView.setImageBitmap(HabiticaIconsHelper.imageOfGem())

        binding.challengeAddGemBtn.setOnClickListener { onAddGem() }
        binding.challengeRemoveGemBtn.setOnClickListener { onRemoveGem() }
    }

    public override fun onDestroy() {
        socialRepository.close()
        challengeRepository.close()
        super.onDestroy()
    }

    private fun onAddGem() {
        var stringValue = binding.createChallengePrize.text.toString()
        if (stringValue.isEmpty()) {
            stringValue = "0"
        }
        var currentVal = Integer.parseInt(stringValue)
        currentVal++

        binding.createChallengePrize.setText(currentVal.toString())

        checkPrizeAndMinimumForTavern()
    }

    private fun onRemoveGem() {
        var stringValue = binding.createChallengePrize.text.toString()
        if (stringValue.isEmpty()) {
            stringValue = "0"
        }
        var currentVal = Integer.parseInt(stringValue)
        currentVal--

        binding.createChallengePrize.setText(currentVal.toString())

        checkPrizeAndMinimumForTavern()
    }

    private fun checkPrizeAndMinimumForTavern(): String {
        var errorResult = ""

        var inputValue = binding.createChallengePrize.text.toString()

        if (inputValue.isEmpty()) {
            inputValue = "0"
        }

        val currentVal = try {
            Integer.parseInt(inputValue)
        } catch (_: NumberFormatException) {
            0
        }

        // 0 is Tavern
        val selectedLocation = binding.challengeLocationSpinner.selectedItemPosition

        val gemCount = user?.gemCount?.toDouble() ?: 0.toDouble()

        if (selectedLocation == 0 && currentVal == 0) {
            binding.createChallengeGemError.visibility = View.VISIBLE
            val error = getString(R.string.challenge_create_error_tavern_one_gem)
            binding.createChallengeGemError.text = error
            errorResult = error
        } else if (currentVal > gemCount) {
            binding.createChallengeGemError.visibility = View.VISIBLE
            val error = getString(R.string.challenge_create_error_enough_gems)
            binding.createChallengeGemError.text = error
            errorResult = error
        } else {
            binding.createChallengeGemError.visibility = View.GONE
        }

        binding.challengeRemoveGemBtn.isEnabled = currentVal != 0

        return errorResult
    }

    private fun fillControls() {
        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowHomeEnabled(true)
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

            supportActionBar.title = ""
            supportActionBar.setBackgroundDrawable(ColorDrawable(getThemeColor(R.attr.colorPrimaryOffset)))
            supportActionBar.elevation = 0f
        }

        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            val groups = socialRepository.getUserGroups("guild").firstOrNull()?.toMutableList()
                ?: return@launch
            val partyID = userRepository.getUser().firstOrNull()?.party?.id
            val party = if (partyID?.isNotBlank() == true) {
                socialRepository.retrieveGroup(partyID)
            } else {
                null
            }
            if (groups.firstOrNull { it.id == "00000000-0000-4000-A000-000000000000" } == null) {
                val tavern = Group()
                tavern.id = "00000000-0000-4000-A000-000000000000"
                tavern.name = getString(R.string.public_challenge)
                groups.add(0, tavern)
            }
            if (party != null) {
                groups.add(party)
            }
            locationAdapter.clear()
            locationAdapter.addAll(groups)
        }

        binding.challengeLocationSpinner.adapter = locationAdapter
        binding.challengeLocationSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    adapterView: AdapterView<*>,
                    view: View?,
                    i: Int,
                    l: Long
                ) {
                    checkPrizeAndMinimumForTavern()
                }

                override fun onNothingSelected(adapterView: AdapterView<*>) { /* no-on */
                }
            }
        binding.createChallengePrize.setOnKeyListener { _, _, _ ->
            checkPrizeAndMinimumForTavern()

            false
        }

        val taskList = ArrayList<Task>()
        taskList.add(addHabit)
        taskList.add(addDaily)
        taskList.add(addTodo)
        taskList.add(addReward)

        challengeTasks.setTasks(taskList)
        challengeTasks.onAddItem = { t ->
            when (t.text) {
                addHabit.text -> openNewTaskActivity(TaskType.HABIT, null)
                addDaily.text -> openNewTaskActivity(TaskType.DAILY, null)
                addTodo.text -> openNewTaskActivity(TaskType.TODO, null)
                addReward.text -> openNewTaskActivity(TaskType.REWARD, null)
            }
        }

        binding.createChallengeTaskList.addOnItemTouchListener(object :
                androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener() {
                override fun onInterceptTouchEvent(
                    rv: androidx.recyclerview.widget.RecyclerView,
                    e: MotionEvent
                ): Boolean {
                    // Stop only scrolling.
                    return rv.scrollState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
                }
            })
        binding.createChallengeTaskList.adapter = challengeTasks
        binding.createChallengeTaskList.layoutManager =
            androidx.recyclerview.widget.LinearLayoutManager(this)
    }

    private fun fillControlsByChallenge() {
        challengeId?.let {
            lifecycleScope.launchCatching {
                challengeRepository.getChallenge(it).collect { challenge ->
                    groupID = challenge.groupId
                    editMode = true
                    binding.createChallengeTitle.setText(challenge.name)
                    binding.createChallengeDescription.setText(challenge.description)
                    binding.createChallengeTag.setText(challenge.shortName)
                    binding.createChallengePrize.setText(challenge.prize.toString())
                    binding.challengeCreationViews.visibility = View.GONE

                    for (i in 0 until locationAdapter.count) {
                        val group = locationAdapter.getItem(i)

                        if (group != null && challenge.groupId == group.id) {
                            binding.challengeLocationSpinner.setSelection(i)
                            break
                        }
                    }
                    checkPrizeAndMinimumForTavern()
                }
            }
            lifecycleScope.launchCatching {
                challengeRepository.getChallengeTasks(it).collect { tasks ->
                    tasks.forEach { task ->
                        addOrUpdateTaskInList(task, true)
                    }
                }
            }
        }
    }

    private fun openNewTaskActivity(type: TaskType?, task: Task?) {
        val bundle = Bundle()

        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, type?.value ?: "")
        if (task != null) {
            bundle.putParcelable(TaskFormActivity.PARCELABLE_TASK, task)
        }

        bundle.putBoolean(TaskFormActivity.SET_IGNORE_FLAG, true)
        bundle.putBoolean(TaskFormActivity.IS_CHALLENGE_TASK, true)
        bundle.putString(TaskFormActivity.USER_ID_KEY, user?.id)

        val intent = Intent(this, TaskFormActivity::class.java)
        intent.putExtras(bundle)

        newTaskResult.launch(intent)
    }

    private val newTaskResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val task = it.data?.getParcelableExtra<Task>(TaskFormActivity.PARCELABLE_TASK)
                if (task != null) {
                    addOrUpdateTaskInList(task)
                }
            }
        }

    private suspend fun createChallenge(): Challenge? {
        val c = challengeData

        val taskList = challengeTasks.taskList
        taskList.remove(addHabit)
        taskList.remove(addDaily)
        taskList.remove(addTodo)
        taskList.remove(addReward)

        return challengeRepository.createChallenge(c, taskList)
    }

    private suspend fun updateChallenge(): Challenge? {
        val c = challengeData

        val taskList = challengeTasks.taskList
        taskList.remove(addHabit)
        taskList.remove(addDaily)
        taskList.remove(addTodo)
        taskList.remove(addReward)

        return challengeRepository.updateChallenge(
            c, taskList, ArrayList(addedTasks.values),
            ArrayList(updatedTasks.values),
            ArrayList(removedTasks.keys)
        )
    }

    private fun addOrUpdateTaskInList(task: Task, isExistingTask: Boolean = false) {
        if (!challengeTasks.replaceTask(task)) {
            val taskAbove = when (task.type) {
                TaskType.HABIT -> addHabit
                TaskType.DAILY -> addDaily
                TaskType.TODO -> addTodo
                else -> addReward
            }
            if (!isExistingTask) {
                // If the task is new we create a unique id for it
                // Doing it we solve the issue #1278
                task.id = UUID.randomUUID().toString()
            }

            challengeTasks.addTaskUnder(task, taskAbove)

            if (editMode && !isExistingTask) {
                addedTasks[task.id ?: ""] = task
            }
        } else {
            // don't need to add the task to updatedTasks if its already been added right now
            if (editMode && !addedTasks.containsKey(task.id)) {
                updatedTasks[task.id ?: ""] = task
            }
        }
    }

    private fun getEditTextString(editText: EditText): String {
        return editText.text.toString()
    }

    private class GroupArrayAdapter(context: Context) :
        ArrayAdapter<Group>(context, android.R.layout.simple_spinner_item) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val checkedTextView = super.getView(position, convertView, parent) as? TextView
            checkedTextView?.text = getItem(position)?.name
            return checkedTextView ?: View(context)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val checkedTextView =
                super.getDropDownView(position, convertView, parent) as? AppCompatCheckedTextView
            checkedTextView?.text = getItem(position)?.name
            return checkedTextView ?: View(context)
        }
    }

    companion object {
        const val CHALLENGE_ID_KEY = "challengeId"

        private fun createTask(taskName: String): Task {
            val t = Task()

            t.id = "addtask"
            t.text = taskName

            return t
        }
    }
}
