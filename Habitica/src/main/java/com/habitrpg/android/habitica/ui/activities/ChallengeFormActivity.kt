package com.habitrpg.android.habitica.ui.activities

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.adapter.social.challenges.ChallengeTasksRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import java.util.*
import javax.inject.Inject
import javax.inject.Named

class ChallengeFormActivity : BaseActivity() {

    private val createChallengeTitleInputLayout: TextInputLayout by bindView(R.id.create_challenge_title_input_layout)
    private val createChallengeTitle: EditText by bindView(R.id.create_challenge_title)
    private val createChallengeDescription: EditText by bindView(R.id.create_challenge_description)
    private val createChallengePrize: EditText by bindView(R.id.create_challenge_prize)
    private val createChallengeTagInputLayout: TextInputLayout by bindView(R.id.create_challenge_tag_input_layout)
    private val createChallengeTag: EditText by bindView(R.id.create_challenge_tag)
    private val createChallengeGemError: TextView by bindView(R.id.create_challenge_gem_error)
    private val createChallengeTaskError: TextView by bindView(R.id.create_challenge_task_error)
    private val challengeLocationSpinner: Spinner by bindView(R.id.challenge_location_spinner)
    private val challengeAddGemBtn: Button by bindView(R.id.challenge_add_gem_btn)
    private val challengeRemoveGemBtn: Button by bindView(R.id.challenge_remove_gem_btn)
    private val createChallengeTaskList: androidx.recyclerview.widget.RecyclerView by bindView(R.id.create_challenge_task_list)
    private val gemIconView: ImageView by bindView(R.id.gem_icon)
    private val challengeCreationViews: ViewGroup by bindView(R.id.challenge_creation_views)

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

    // Add {*} Items
    private var addHabit: Task? = null
    private var addDaily: Task? = null
    private var addTodo: Task? = null
    private var addReward: Task? = null
    private var user: User? = null

    private var savingInProgress = false

    private val challengeData: Challenge
        get() {
            val c = Challenge()

            val locationPos = challengeLocationSpinner.selectedItemPosition

            if (challengeId != null) {
                c.id = challengeId
            }

            if (groupID != null) {
                c.groupId = groupID
            } else {
                val locationGroup = locationAdapter.getItem(locationPos)
                if (locationGroup != null) {
                    c.groupId = locationGroup.id
                }
            }
            c.name = createChallengeTitle.text.toString()
            c.description = createChallengeDescription.text.toString()
            c.shortName = createChallengeTag.text.toString()
            c.prize = Integer.parseInt(createChallengePrize.text.toString())

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
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save && !savingInProgress && validateAllFields()) {
            savingInProgress = true
            @Suppress("DEPRECATION")
            val dialog = ProgressDialog.show(this, "", "Saving challenge data. Please wait...", true, false)

            val observable: Flowable<Challenge> = if (editMode) {
                updateChallenge()
            } else {
                createChallenge()
            }

            compositeSubscription.add(observable.subscribe({
                dialog.dismiss()
                savingInProgress = false
                finish()
            }, { throwable ->
                dialog.dismiss()
                savingInProgress = false
                RxErrorHandler.reportError(throwable)
            }))
        } else if (item.itemId == android.R.id.home) {
            finish()
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                val task = data?.getParcelableExtra<Task>(TaskFormActivity.PARCELABLE_TASK)
                if (task != null) {
                    addOrUpdateTaskInList(task)
                }
            }
        }
    }

    private fun validateAllFields(): Boolean {
        val errorMessages = ArrayList<String>()

        if (getEditTextString(createChallengeTitle).isEmpty()) {
            val titleEmptyError = getString(R.string.challenge_create_error_title)
            createChallengeTitleInputLayout.error = titleEmptyError
            errorMessages.add(titleEmptyError)
        } else {
            createChallengeTitleInputLayout.isErrorEnabled = false
        }

        if (getEditTextString(createChallengeTag).isEmpty()) {
            val tagEmptyError = getString(R.string.challenge_create_error_tag)

            createChallengeTagInputLayout.error = tagEmptyError
            errorMessages.add(tagEmptyError)
        } else {
            createChallengeTagInputLayout.isErrorEnabled = false
        }

        val prizeError = checkPrizeAndMinimumForTavern()

        if (prizeError.isNotEmpty()) {
            errorMessages.add(prizeError)
        }

        // all "Add {*}"-Buttons are one task itself, so we need atleast more than 4
        if (challengeTasks.taskList.size <= 4) {
            createChallengeTaskError.visibility = View.VISIBLE
            errorMessages.add(getString(R.string.challenge_create_error_no_tasks))
        } else {
            createChallengeTaskError.visibility = View.GONE
        }
        if (errorMessages.count() > 0) {
            val alert = HabiticaAlertDialog(this)
            alert.setMessage(errorMessages.joinToString("\n"))
            alert.show()
        }
        return errorMessages.size == 0
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = intent
        val bundle = intent.extras

        challengeTasks = ChallengeTasksRecyclerViewAdapter(null, 0, this, "", null, false, true)
        compositeSubscription.add(challengeTasks.taskOpenEvents.subscribe { openNewTaskActivity(it.type, it) })
        locationAdapter = GroupArrayAdapter(this)

        if (bundle != null) {
            challengeId = bundle.getString(CHALLENGE_ID_KEY, null)
        }

        fillControls()

        if (challengeId != null) {
            fillControlsByChallenge()
        }

        compositeSubscription.add(userRepository.getUser(userId).subscribe(Consumer { this.user = it }, RxErrorHandler.handleEmptyError()))
        gemIconView.setImageBitmap(HabiticaIconsHelper.imageOfGem())

        challengeAddGemBtn.setOnClickListener { onAddGem() }
        challengeRemoveGemBtn.setOnClickListener { onRemoveGem() }
    }


    public override fun onDestroy() {
        socialRepository.close()
        challengeRepository.close()
        super.onDestroy()
    }

    private fun onAddGem() {
        var stringValue = createChallengePrize.text.toString()
        if (stringValue.isEmpty()) {
            stringValue = "0"
        }
        var currentVal = Integer.parseInt(stringValue)
        currentVal++

        createChallengePrize.setText(currentVal.toString())

        checkPrizeAndMinimumForTavern()
    }

    private fun onRemoveGem() {
        var stringValue = createChallengePrize.text.toString()
        if (stringValue.isEmpty()) {
            stringValue = "0"
        }
        var currentVal = Integer.parseInt(stringValue)
        currentVal--

        createChallengePrize.setText(currentVal.toString())

        checkPrizeAndMinimumForTavern()
    }

    private fun checkPrizeAndMinimumForTavern(): String {
        var errorResult = ""

        var inputValue = createChallengePrize.text.toString()

        if (inputValue.isEmpty()) {
            inputValue = "0"
        }

        val currentVal = Integer.parseInt(inputValue)

        // 0 is Tavern
        val selectedLocation = challengeLocationSpinner.selectedItemPosition

        val gemCount = user?.gemCount?.toDouble() ?: 0.toDouble()

        if (selectedLocation == 0 && currentVal == 0) {
            createChallengeGemError.visibility = View.VISIBLE
            val error = getString(R.string.challenge_create_error_tavern_one_gem)
            createChallengeGemError.text = error
            errorResult = error
        } else if (currentVal > gemCount) {
            createChallengeGemError.visibility = View.VISIBLE
            val error = getString(R.string.challenge_create_error_enough_gems)
            createChallengeGemError.text = error
            errorResult = error
        } else {
            createChallengeGemError.visibility = View.GONE
        }

        challengeRemoveGemBtn.isEnabled = currentVal != 0

        return errorResult
    }

    private fun fillControls() {
        val resources = resources

        val supportActionBar = supportActionBar
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowHomeEnabled(true)
            supportActionBar.setHomeAsUpIndicator(R.drawable.ic_close_white_24dp)

            supportActionBar.title = ""
            supportActionBar.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.brand_200)))
            supportActionBar.elevation = 0f
        }

        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        compositeSubscription.add(socialRepository.getGroups("guild").subscribe(Consumer { groups ->
            val mutableGroups = groups.toMutableList()
            if (groups.firstOrNull { it.id == "00000000-0000-4000-A000-000000000000" } == null) {
                val tavern = Group()
                tavern.id = "00000000-0000-4000-A000-000000000000"
                tavern.name = getString(R.string.public_challenge)
                mutableGroups.add(0, tavern)
            }

            locationAdapter.clear()
            locationAdapter.addAll(mutableGroups)
        }, RxErrorHandler.handleEmptyError()))

        challengeLocationSpinner.adapter = locationAdapter
        challengeLocationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View, i: Int, l: Long) {
                checkPrizeAndMinimumForTavern()
            }

            override fun onNothingSelected(adapterView: AdapterView<*>) {

            }
        }
        createChallengePrize.setOnKeyListener { _, _, _ ->
            checkPrizeAndMinimumForTavern()

            false
        }

        addHabit = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_habit))
        addDaily = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_daily))
        addTodo = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_todo))
        addReward = createTask(ChallengeTasksRecyclerViewAdapter.TASK_TYPE_ADD_ITEM, resources.getString(R.string.add_reward))


        val taskList = ArrayList<Task>()
        addHabit?.let { taskList.add(it) }
        addDaily?.let { taskList.add(it) }
        addTodo?.let { taskList.add(it) }
        addReward?.let { taskList.add(it) }

        challengeTasks.setTasks(taskList)
        compositeSubscription.add(challengeTasks.addItemObservable().subscribe(Consumer { t ->
            when (t) {
                addHabit -> openNewTaskActivity(Task.TYPE_HABIT, null)
                addDaily -> openNewTaskActivity(Task.TYPE_DAILY, null)
                addTodo -> openNewTaskActivity(Task.TYPE_TODO, null)
                addReward -> openNewTaskActivity(Task.TYPE_REWARD, null)
            }
        }, RxErrorHandler.handleEmptyError()))

        createChallengeTaskList.addOnItemTouchListener(object : androidx.recyclerview.widget.RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: androidx.recyclerview.widget.RecyclerView, e: MotionEvent): Boolean {
                // Stop only scrolling.
                return rv.scrollState == androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
            }
        })
        createChallengeTaskList.adapter = challengeTasks
        createChallengeTaskList.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
    }

    private fun fillControlsByChallenge() {
        challengeId?.let {
            challengeRepository.getChallenge(it).subscribe(Consumer { challenge ->
                groupID = challenge.groupId
                editMode = true
                createChallengeTitle.setText(challenge.name)
                createChallengeDescription.setText(challenge.description)
                createChallengeTag.setText(challenge.shortName)
                createChallengePrize.setText(challenge.prize.toString())
                challengeCreationViews.visibility = View.GONE

                for (i in 0 until locationAdapter.count) {
                    val group = locationAdapter.getItem(i)

                    if (group != null && challenge.groupId == group.id) {
                        challengeLocationSpinner.setSelection(i)
                        break
                    }
                }
                checkPrizeAndMinimumForTavern()
            }, RxErrorHandler.handleEmptyError())
            challengeRepository.getChallengeTasks(it).subscribe(Consumer { tasks ->
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

        startActivityForResult(intent, 1000)
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

    private inner class GroupArrayAdapter internal constructor(context: Context) : ArrayAdapter<Group>(context, android.R.layout.simple_spinner_item) {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val checkedTextView = super.getView(position, convertView, parent) as? AppCompatTextView
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

        private fun createTask(taskType: String, taskName: String): Task {
            val t = Task()

            t.id = UUID.randomUUID().toString()
            t.type = taskType
            t.text = taskName

            if (taskType == Task.TYPE_HABIT) {
                t.up = true
                t.down = false
            }

            return t
        }
    }
}
