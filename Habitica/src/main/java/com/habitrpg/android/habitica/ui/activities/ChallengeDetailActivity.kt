package com.habitrpg.android.habitica.ui.activities

import android.app.AlertDialog
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.events.HabitScoreEvent
import com.habitrpg.android.habitica.events.TaskUpdatedEvent
import com.habitrpg.android.habitica.events.commands.BuyRewardCommand
import com.habitrpg.android.habitica.events.commands.ChecklistCheckedCommand
import com.habitrpg.android.habitica.events.commands.TaskCheckedCommand
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.interactors.*
import com.habitrpg.android.habitica.models.LeaveChallengeBody
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeDetailDialogHolder
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeTasksRecyclerViewFragment
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.utils.Action1
import io.reactivex.functions.Consumer
import net.pherth.android.emoji_library.EmojiParser
import net.pherth.android.emoji_library.EmojiTextView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.*
import javax.inject.Inject
import javax.inject.Named


class ChallengeDetailActivity : BaseActivity() {


    @Inject
    internal lateinit var challengeRepository: ChallengeRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    internal lateinit var userId: String
    @Inject
    internal lateinit var userRepository: UserRepository

    private val floatingMenuWrapper: FrameLayout by bindView(R.id.floating_menu_wrapper)
    private val detailTabs: TabLayout by bindView(R.id.detail_tabs)
    private val toolbar: Toolbar by bindView(R.id.toolbar)
    // region UseCases

    @Inject
    internal lateinit var habitScoreUseCase: HabitScoreUseCase

    @Inject
    internal lateinit var dailyCheckUseCase: DailyCheckUseCase

    @Inject
    internal lateinit var todoCheckUseCase: TodoCheckUseCase

    @Inject
    internal lateinit var buyRewardUseCase: BuyRewardUseCase

    @Inject
    internal lateinit var checklistCheckUseCase: ChecklistCheckUseCase

    @Inject
    internal lateinit var displayItemDropUseCase: DisplayItemDropUseCase

    @Inject
    internal lateinit var notifyUserUseCase: NotifyUserUseCase

    // endregion

    private var challenge: Challenge? = null
    private var user: User? = null

    override fun getLayoutResId(): Int {
        return R.layout.activity_challenge_detail
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_challenge_details, menu)

        if (challenge?.leaderId != userId) {
            menu.setGroupVisible(R.id.challenge_edit_action_group, false)
        }

        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(toolbar)

        supportActionBar?.setTitle(R.string.challenge_details)
        detailTabs.visibility = View.GONE

        val extras = intent.extras

        val challengeId = extras.getString(CHALLENGE_ID)

        val fullList = ArrayList<Task>()

        userRepository.getUser(userId).firstElement().subscribe(Consumer { user ->
            this@ChallengeDetailActivity.user = user
            createTaskRecyclerFragment(fullList)
        }, RxErrorHandler.handleEmptyError())

        if (challengeId != null) {
            challengeRepository.getChallengeTasks(challengeId)
                    .firstElement()
                    .subscribe(Consumer { taskList ->
                        val resultList = ArrayList<Task>()

                        val todos = ArrayList<Task>()
                        val habits = ArrayList<Task>()
                        val dailies = ArrayList<Task>()
                        val rewards = ArrayList<Task>()

                        for (entry in taskList.tasks.entries) {
                            when (entry.value.type) {
                                Task.TYPE_TODO -> todos.add(entry.value)
                                Task.TYPE_HABIT ->

                                    habits.add(entry.value)
                                Task.TYPE_DAILY ->

                                    dailies.add(entry.value)
                                Task.TYPE_REWARD ->

                                    rewards.add(entry.value)
                            }
                        }


                        if (!habits.isEmpty()) {
                            val dividerTask = Task()
                            dividerTask.id = "divhabits"
                            dividerTask.type = "divider"
                            dividerTask.text = "Challenge Habits"

                            resultList.add(dividerTask)
                            resultList.addAll(habits)
                        }


                        if (!dailies.isEmpty()) {
                            val dividerTask = Task()
                            dividerTask.id = "divdailies"
                            dividerTask.type = "divider"
                            dividerTask.text = "Challenge Dailies"

                            resultList.add(dividerTask)
                            resultList.addAll(dailies)
                        }


                        if (!todos.isEmpty()) {
                            val dividerTask = Task()
                            dividerTask.id = "divtodos"
                            dividerTask.type = "divider"
                            dividerTask.text = "Challenge To-Dos"

                            resultList.add(dividerTask)
                            resultList.addAll(todos)
                        }

                        if (!rewards.isEmpty()) {
                            val dividerTask = Task()
                            dividerTask.id = "divrewards"
                            dividerTask.type = "divider"
                            dividerTask.text = "Challenge Rewards"

                            resultList.add(dividerTask)
                            resultList.addAll(rewards)
                        }


                        fullList.addAll(resultList)
                    }, RxErrorHandler.handleEmptyError())
        }

        if (challengeId != null) {
            challengeRepository.getChallenge(challengeId).subscribe(Consumer { challenge ->
                this@ChallengeDetailActivity.challenge = challenge
                val challengeViewHolder = ChallengeViewHolder(findViewById(R.id.challenge_header))
                challengeViewHolder.bind(challenge)
            }, RxErrorHandler.handleEmptyError())
        }
    }

    private fun createTaskRecyclerFragment(fullList: List<Task>) {
        val fragment = ChallengeTasksRecyclerViewFragment.newInstance(user, fullList)

        if (supportFragmentManager.fragments == null) {
            supportFragmentManager.beginTransaction().add(R.id.fragment_container, fragment).commitAllowingStateLoss()
        } else {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out)
            transaction.replace(R.id.fragment_container, fragment).addToBackStack(null).commitAllowingStateLoss()
        }
    }

    override fun onDestroy() {
        challengeRepository.close()
        userRepository.close()
        super.onDestroy()
    }

    override fun injectActivity(component: AppComponent?) {
        component?.inject(this)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                openChallengeEditActivity()
                return true
            }
            R.id.action_leave -> {
                showChallengeLeaveDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openChallengeEditActivity() {
        val intent = Intent(this, CreateChallengeActivity::class.java)
        intent.putExtra(CreateChallengeActivity.CHALLENGE_ID_KEY, challenge?.id)

        startActivity(intent)

    }

    private fun showChallengeLeaveDialog() {
        AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.challenge_leave_title))
                .setMessage(this.getString(R.string.challenge_leave_text, challenge?.name ?: ""))
                .setPositiveButton(this.getString(R.string.yes)) { dialog, _ ->
                    dialog.dismiss()

                    showRemoveTasksDialog(object: Action1<String> {
                        override fun call(t: String) {
                            challengeRepository.leaveChallenge(challenge, LeaveChallengeBody(t))
                                    .subscribe(Consumer { finish() }, RxErrorHandler.handleEmptyError())
                        }
                    })
                }
                .setNegativeButton(this.getString(R.string.no)) { dialog, _ -> dialog.dismiss() }.show()
    }

    // refactor as an UseCase later - see ChallengeDetailDialogHolder
    private fun showRemoveTasksDialog(callback: Action1<String>) {
        AlertDialog.Builder(this)
                .setTitle(this.getString(R.string.challenge_remove_tasks_title))
                .setMessage(this.getString(R.string.challenge_remove_tasks_text))
                .setPositiveButton(this.getString(R.string.remove_tasks)) { dialog, _ ->
                    callback.call("remove-all")
                    dialog.dismiss()
                }
                .setNegativeButton(this.getString(R.string.keep_tasks)) { dialog, _ ->
                    callback.call("keep-all")
                    dialog.dismiss()
                }.show()
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onBackPressed() {
        finish()
    }

    internal inner class ChallengeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val challengeName: EmojiTextView by bindView(itemView, R.id.challenge_name)
        private val challengeDescription: EmojiTextView by bindView(itemView, R.id.challenge_description)
        private val memberCountTextView: TextView by bindView(itemView, R.id.challenge_member_count)
        private val gemPrizeTextView: TextView by bindView(itemView, R.id.gem_amount)
        private val showMoreButton: Button by bindView(itemView, R.id.btn_show_more)

        private var challenge: Challenge? = null

        init {
            showMoreButton.setOnClickListener { onShowMore() }

            val gemDrawable = BitmapDrawable(itemView.resources, HabiticaIconsHelper.imageOfGem())
            gemPrizeTextView.setCompoundDrawablesWithIntrinsicBounds(gemDrawable, null, null, null)
        }

        fun bind(challenge: Challenge) {
            this.challenge = challenge

            challengeName.text = EmojiParser.parseEmojis(challenge.name)
            challengeDescription.text = MarkdownParser.parseMarkdown(challenge.description)

            memberCountTextView.text = challenge.memberCount.toString()

            if (challenge.prize == 0) {
                gemPrizeTextView.visibility = View.GONE
            } else {
                gemPrizeTextView.visibility = View.VISIBLE
                gemPrizeTextView.text = challenge.prize.toString()
            }
        }

        fun onShowMore() {
            challenge.notNull {
                ChallengeDetailDialogHolder.showDialog(this@ChallengeDetailActivity,
                        this@ChallengeDetailActivity.challengeRepository,
                        it,
                        object: Action1<Challenge> {
                            override fun call(t: Challenge) {
                                onBackPressed()
                            }
                        })
            }

        }
    }

    @Subscribe
    fun onEvent(event: TaskCheckedCommand) {
        when (event.Task.type) {
            Task.TYPE_DAILY -> {
                dailyCheckUseCase.observable(DailyCheckUseCase.RequestValues(user, event.Task, !event.Task.completed))
                        .subscribe(Consumer { this.onTaskDataReceived(it) }, RxErrorHandler.handleEmptyError())
            }
            Task.TYPE_TODO -> {
                todoCheckUseCase.observable(TodoCheckUseCase.RequestValues(user, event.Task, !event.Task.completed))
                        .subscribe(Consumer { this.onTaskDataReceived(it) }, RxErrorHandler.handleEmptyError())
            }
        }
    }

    @Subscribe
    fun onEvent(event: ChecklistCheckedCommand) {
        checklistCheckUseCase.observable(ChecklistCheckUseCase.RequestValues(event.task.id, event.item.id))
                .subscribe(Consumer { EventBus.getDefault().post(TaskUpdatedEvent(event.task)) }, RxErrorHandler.handleEmptyError())
    }

    @Subscribe
    fun onEvent(event: HabitScoreEvent) {
        habitScoreUseCase.observable(HabitScoreUseCase.RequestValues(user, event.habit, event.Up))
                .subscribe(Consumer { this.onTaskDataReceived(it) }, RxErrorHandler.handleEmptyError())
    }

    @Subscribe
    fun onEvent(event: BuyRewardCommand) {
        if ((user?.stats?.gp ?: 0.toDouble()) < event.Reward.value) {
            HabiticaSnackbar.showSnackbar(floatingMenuWrapper, getString(R.string.no_gold), HabiticaSnackbar.SnackbarDisplayType.FAILURE)
            return
        }


        if (event.Reward.specialTag == null || event.Reward.specialTag != "item") {

            buyRewardUseCase.observable(BuyRewardUseCase.RequestValues(user, event.Reward))
                    .subscribe(Consumer { HabiticaSnackbar.showSnackbar(floatingMenuWrapper, getString(R.string.notification_purchase_reward), HabiticaSnackbar.SnackbarDisplayType.NORMAL) }, RxErrorHandler.handleEmptyError())
        }

    }

    private fun onTaskDataReceived(data: TaskScoringResult) {
        if (user != null) {
            notifyUserUseCase.observable(NotifyUserUseCase.RequestValues(this, floatingMenuWrapper,
                    user, data.experienceDelta, data.healthDelta, data.goldDelta, data.manaDelta, data.questDamage, data.hasLeveledUp))
        }

        displayItemDropUseCase.observable(DisplayItemDropUseCase.RequestValues(data, this, floatingMenuWrapper))
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    companion object {

        var CHALLENGE_ID = "CHALLENGE_ID"
    }
}
