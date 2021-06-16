package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.appcompat.widget.Toolbar
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.ActivityCreateChallengeBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.getThemeColor
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengeTasksRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengesOverviewFragmentDirections
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaProgressDialog
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.NumberFormatException
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class ChallengeFormActivity : BaseActivity() {


    private lateinit var binding: ActivityCreateChallengeBinding

    @Inject
    internal lateinit var challengeRepository: ChallengeRepository
    @Inject
    internal lateinit var socialRepository: SocialRepository
    @Inject
    internal lateinit var userRepository: UserRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    internal lateinit var userId: String

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
    private var addHabit: Task? = null
    private var addDaily: Task? = null
    private var addTodo: Task? = null
    private var addReward: Task? = null
    private var user: User? = null

    private var savingInProgress = false

    override fun getContentView(): View {
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

    override fun injectActivity(component: UserComponent?) {
        component?.inject(this)
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_create_challenge, menu)
        findViewById<Toolbar>(R.id.toolbar).let { ToolbarColorHelper.colorizeToolbar(it, this, overrideModernHeader) }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save && !savingInProgress && validateAllFields()) {
            savingInProgress = true
            val dialog = HabiticaProgressDialog.show(this, R.string.saving)

            val observable: Flowable<Challenge> = if (editMode) {
                updateChallenge()
            } else {
                createChallenge()
            }

            compositeSubscription.add(observable
                    .flatMap {
                        challengeId = it.id
                        challengeRepository.retrieveChallenges(0, true)
                    }
                    .subscribe({
                        dialog?.dismiss()
                        savingInProgress = false
                        finish()
                        if (!editMode) {
                            GlobalScope.launch(context = Dispatchers.Main) {
                                delay(500L)
                                MainNavigationController.navigate(ChallengesOverviewFragmentDirections.openChallengeDetail(challengeId ?: ""))
                            }
                        }
                    }, { throwable ->
                dialog?.dismiss()
                savingInProgress = false
                RxErrorHandler.reportError(throwable)
            }))
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
        if (errorMessages.count() > 0) {
            val alert = HabiticaAlertDialog(this)
            alert.setMessage(errorMessages.joinToString("\n"))
            alert.addCloseButton(true)
            alert.enqueue()
        }
        return errorMessages.size == 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val intent = intent
        val bundle = intent.extras

        ChallengeTasksRecyclerViewAdapter(null, 0, this, "",
            openTaskDisabled = false,
            taskActionsDisabled = true
        ).also { challengeTasks = it }
        compositeSubscription.add(challengeTasks.taskOpenEvents.subscribe {
            if (it.isValid) {
                openNewTaskActivity(it.type, it)
            }
        })
        locationAdapter = GroupArrayAdapter(this)

        if (bundle != null) {
            challengeId = bundle.getString(CHALLENGE_ID_KEY, null)
        }

        fillControls()

        if (challengeId != null) {
            fillControlsByChallenge()
        }

        compositeSubscription.add(userRepository.getUser(userId).subscribe({ this.user = it }, RxErrorHandler.handleEmptyError()))
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
        val resources = resources

        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowHomeEnabled(true)
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

            supportActionBar.title = ""
            supportActionBar.setBackgroundDrawable(ColorDrawable(getThemeColor(R.attr.colorPrimaryOffset)))
            supportActionBar.elevation = 0f
        }

        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        compositeSubscription.add(socialRepository.getUserGroups("guild").zipWith(userRepository.getUser()
                .map { it.party?.id ?: "" }
                .distinctUntilChanged()
                .flatMap {
                    if (it.isBlank()) {
                        return@flatMap Flowable.empty<Group>()
                    }
                    socialRepository.retrieveGroup(it)
                }, { user, groups -> Pair(user, groups) })
                .subscribe({ groups ->
            val mutableGroups = groups.first.toMutableList()
            if (groups.first.firstOrNull { it.id == "00000000-0000-4000-A000-000000000000" } == null) {
                val tavern = Group()
                tavern.id = "00000000-0000-4000-A000-000000000000"
                tavern.name = getString(R.string.public_challenge)
                mutableGroups.add(0, tavern)
            }
            if (groups.second != null) {
                mutableGroups.add(groups.second)
            }
            locationAdapter.clear()
            locationAdapter.addAll(mutableGroups)
        }, RxErrorHandler.handleEmptyError()))

        binding.challengeLocationSpinner.adapter = locationAdapter
        binding.challengeLocationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View?, i: Int, l: Long) {
                checkPrizeAndMinimumForTavern()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) { /* no-on */ }
        }
        binding.createChallengePrize.setOnKeyListener { _, _, _ ->
            checkPrizeAndMinimumForTavern()

            false
        }

        addHabit = createTask(resources.getString(R.string.add_habit))
        addDaily = createTask(resources.getString(R.string.add_daily))
        addTodo = createTask(resources.getString(R.string.add_todo))
        addReward = createTask(resources.getString(R.string.add_reward))


        val taskList = ArrayList<Task>()
        addHabit?.let { taskList.add(it) }
        addDaily?.let { taskList.add(it) }
        addTodo?.let { taskList.add(it) }
        addReward?.let { taskList.add(it) }

        challengeTasks.setTasks(taskList)
        compositeSubscription.add(challengeTasks.addItemObservable().subscribe({ t ->
            when (t) {
                addHabit -> openNewTaskActivity(Task.TYPE_HABIT, null)
                addDaily -> openNewTaskActivity(Task.TYPE_DAILY, null)
                addTodo -> openNewTaskActivity(Task.TYPE_TODO, null)
                addReward -> openNewTaskActivity(Task.TYPE_REWARD, null)
            }
        }, RxErrorHandler.handleEmptyError()))

        binding.createChallengeTaskList.addOnItemTouchListener(object : androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: androidx.recyclerview.widget.RecyclerView, e: MotionEvent): Boolean {
                // Stop only scrolling.
                return rv.scrollState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
            }
        })
        binding.createChallengeTaskList.adapter = challengeTasks
        binding.createChallengeTaskList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
    }

    private fun fillControlsByChallenge() {
        challengeId?.let {
            challengeRepository.getChallenge(it).subscribe({ challenge ->
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
            }, RxErrorHandler.handleEmptyError())
            challengeRepository.getChallengeTasks(it).subscribe({ tasks ->
                tasks.forEach { task ->
                    addOrUpdateTaskInList(task, true)
                }
            }, RxErrorHandler.handleEmptyError())
        }
    }

    private fun openNewTaskActivity(type: String?, task: Task?) {
        val bundle = Bundle()

        bundle.putString(TaskFormActivity.TASK_TYPE_KEY, type)
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

    private val newTaskResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val task = it.data?.getParcelableExtra<Task>(TaskFormActivity.PARCELABLE_TASK)
            if (task != null) {
                addOrUpdateTaskInList(task)
            }
        }
    }

    private fun createChallenge(): Flowable<Challenge> {
        val c = challengeData

        val taskList = challengeTasks.taskList
        taskList.remove(addHabit)
        taskList.remove(addDaily)
        taskList.remove(addTodo)
        taskList.remove(addReward)

        return challengeRepository.createChallenge(c, taskList)
    }

    private fun updateChallenge(): Flowable<Challenge> {
        val c = challengeData

        val taskList = challengeTasks.taskList
        taskList.remove(addHabit)
        taskList.remove(addDaily)
        taskList.remove(addTodo)
        taskList.remove(addReward)

        return challengeRepository.updateChallenge(c, taskList, ArrayList(addedTasks.values),
                ArrayList(updatedTasks.values),
                ArrayList(removedTasks.keys)
        )
    }

    private fun addOrUpdateTaskInList(task: Task, isExistingTask: Boolean = false) {
        if (!challengeTasks.replaceTask(task)) {
            val taskAbove: Task? = when (task.type) {
                Task.TYPE_HABIT -> addHabit
                Task.TYPE_DAILY -> addDaily
                Task.TYPE_TODO -> addTodo
                else -> addReward
            }
            if(!isExistingTask){
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

    private class GroupArrayAdapter(context: Context) : ArrayAdapter<Group>(context, android.R.layout.simple_spinner_item) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val checkedTextView = super.getView(position, convertView, parent) as? TextView
            checkedTextView?.text = getItem(position)?.name
            return checkedTextView ?: View(context)
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val checkedTextView = super.getDropDownView(position, convertView, parent) as? AppCompatCheckedTextView
            checkedTextView?.text = getItem(position)?.name
            return checkedTextView ?: View(context)
        }
    }

    companion object {
        const val CHALLENGE_ID_KEY = "challengeId"

        private fun createTask(taskName: String): Task {
            val t = Task()

            t.id = UUID.randomUUID().toString()
            t.type = ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM
            t.text = taskName

            return t
        }
    }
}
