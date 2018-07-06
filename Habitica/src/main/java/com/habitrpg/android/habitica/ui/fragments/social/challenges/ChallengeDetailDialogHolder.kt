package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.LeaveChallengeBody
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.activities.ChallengeDetailActivity
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.utils.Action1
import io.reactivex.functions.Consumer
import net.pherth.android.emoji_library.EmojiParser
import net.pherth.android.emoji_library.EmojiTextView
import java.util.*

class ChallengeDetailDialogHolder private constructor(view: View, private val context: Activity) {

    private val notJoinedHeader: LinearLayout? by bindView(view, R.id.challenge_not_joined_header)
    private val joinedHeader: LinearLayout? by bindView(view, R.id.challenge_joined_header)
    private val joinButton: Button? by bindView(view, R.id.challenge_join_btn)
    private val leaveButton: Button? by bindView(view, R.id.challenge_leave_btn)
    private val challengeName: EmojiTextView? by bindView(view, R.id.challenge_name)
    private val challengeDescription: EmojiTextView? by bindView(view, R.id.challenge_description)
    private val challengeLeader: TextView? by bindView(view, R.id.challenge_leader)
    private val gemAmountView: TextView? by bindView(view, R.id.gem_amount)
    private val memberCountView: TextView? by bindView(view, R.id.challenge_member_count)
    private val taskGrouplayout: LinearLayout? by bindView(view, R.id.task_group_layout)
    private val openChallengeButton: Button? by bindView(view, R.id.challenge_go_to_btn)

    private var dialog: AlertDialog? = null
    lateinit var challengeRepository: ChallengeRepository
    private var challenge: Challenge? = null
    private var challengeLeftAction: Action1<Challenge>? = null

    init {
        joinButton?.setOnClickListener { joinChallenge() }
        leaveButton?.setOnClickListener { leaveChallenge() }
        challengeLeader?.setOnClickListener { openLeaderProfile() }
        openChallengeButton?.setOnClickListener { openChallengeActivity() }
    }

    fun bind(dialog: AlertDialog, challengeRepository: ChallengeRepository, challenge: Challenge,
             challengeLeftAction: Action1<Challenge>) {
        this.dialog = dialog
        this.challengeRepository = challengeRepository
        this.challenge = challenge
        this.challengeLeftAction = challengeLeftAction

        changeViewsByChallenge(challenge)
    }

    private fun changeViewsByChallenge(challenge: Challenge) {
        setJoined(challenge.isParticipating)

        challengeName?.text = EmojiParser.parseEmojis(challenge.name)
        challengeDescription?.text = MarkdownParser.parseMarkdown(challenge.description)
        challengeLeader?.text = challenge.leaderName

        gemAmountView?.text = challenge.prize.toString()
        memberCountView?.text = challenge.memberCount.toString()

        challengeRepository.getChallengeTasks(challenge.id)
                .subscribe(Consumer { taskList ->
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

                    if (habits.size > 0) {
                        addHabits(habits)
                    }

                    if (dailies.size > 0) {
                        addDailys(dailies)
                    }

                    if (todos.size > 0) {
                        addTodos(todos)
                    }

                    if (rewards.size > 0) {
                        addRewards(rewards)
                    }
                }, RxErrorHandler.handleEmptyError())
    }

    private fun addHabits(habits: ArrayList<Task>) {
        val taskGroup = context.layoutInflater.inflate(R.layout.dialog_challenge_detail_task_group, taskGrouplayout, false) as LinearLayout
        val groupName = taskGroup.findViewById<View>(R.id.task_group_name) as TextView

        val tasksLayout = taskGroup.findViewById<View>(R.id.tasks_layout) as LinearLayout

        groupName.text = habits.size.toString() + " " + getLabelByTypeAndCount(Challenge.TASK_ORDER_HABITS, habits.size)

        val size = habits.size
        for (i in 0 until size) {
            val task = habits[i]

            val entry = context.layoutInflater.inflate(R.layout.dialog_challenge_detail_habit, tasksLayout, false)
            val habitTitle = entry.findViewById<View>(R.id.habit_title) as TextView
            val plusImg = entry.findViewById<View>(if (task.up == true) R.id.plus_img_tinted else R.id.plus_img) as ImageView
            val minusImg = entry.findViewById<View>(if (task.down == true) R.id.minus_img_tinted else R.id.minus_img) as ImageView

            plusImg.visibility = View.VISIBLE
            minusImg.visibility = View.VISIBLE

            habitTitle.text = EmojiParser.parseEmojis(task.text)
            tasksLayout.addView(entry)
        }
        taskGrouplayout?.addView(taskGroup)
    }

    private fun addDailys(dailies: ArrayList<Task>) {
        val taskGroup = context.layoutInflater.inflate(R.layout.dialog_challenge_detail_task_group, taskGrouplayout, false) as LinearLayout
        val groupName = taskGroup.findViewById<View>(R.id.task_group_name) as TextView

        val tasksLayout = taskGroup.findViewById<View>(R.id.tasks_layout) as LinearLayout

        val size = dailies.size
        groupName.text = dailies.size.toString() + " " + getLabelByTypeAndCount(Challenge.TASK_ORDER_DAILYS, size)

        for (i in 0 until size) {
            val task = dailies[i]

            val entry = context.layoutInflater.inflate(R.layout.dialog_challenge_detail_daily, tasksLayout, false)
            val title = entry.findViewById<View>(R.id.daily_title) as TextView
            title.text = EmojiParser.parseEmojis(task.text)

            if (task.checklist != null && task.checklist?.isEmpty() == false) {
                val checklistIndicatorWrapper = entry.findViewById<View>(R.id.checklistIndicatorWrapper)

                checklistIndicatorWrapper.visibility = View.VISIBLE

                val checkListAllTextView = entry.findViewById<View>(R.id.checkListAllTextView) as TextView
                checkListAllTextView.text = task.checklist?.size.toString()
            }
            tasksLayout.addView(entry)
        }
        taskGrouplayout?.addView(taskGroup)
    }

    private fun addTodos(todos: ArrayList<Task>) {
        val taskGroup = context.layoutInflater.inflate(R.layout.dialog_challenge_detail_task_group, taskGrouplayout, false) as LinearLayout
        val groupName = taskGroup.findViewById<View>(R.id.task_group_name) as TextView

        val tasksLayout = taskGroup.findViewById<View>(R.id.tasks_layout) as LinearLayout

        val size = todos.size
        groupName.text = todos.size.toString() + " " + getLabelByTypeAndCount(Challenge.TASK_ORDER_TODOS, size)

        for (i in 0 until size) {
            val task = todos[i]

            val entry = context.layoutInflater.inflate(R.layout.dialog_challenge_detail_todo, tasksLayout, false)
            val title = entry.findViewById<View>(R.id.todo_title) as TextView
            title.text = EmojiParser.parseEmojis(task.text)

            if (task.checklist != null && task.checklist?.isEmpty() == false) {
                val checklistIndicatorWrapper = entry.findViewById<View>(R.id.checklistIndicatorWrapper)

                checklistIndicatorWrapper.visibility = View.VISIBLE

                val checkListAllTextView = entry.findViewById<View>(R.id.checkListAllTextView) as TextView
                checkListAllTextView.text = task.checklist?.size.toString()
            }
            tasksLayout.addView(entry)
        }
        taskGrouplayout?.addView(taskGroup)
    }

    private fun addRewards(rewards: ArrayList<Task>) {
        val taskGroup = context.layoutInflater.inflate(R.layout.dialog_challenge_detail_task_group, taskGrouplayout, false) as LinearLayout
        val groupName = taskGroup.findViewById<View>(R.id.task_group_name) as TextView

        val tasksLayout = taskGroup.findViewById<View>(R.id.tasks_layout) as LinearLayout

        val size = rewards.size
        groupName.text = rewards.size.toString() + " " + getLabelByTypeAndCount(Challenge.TASK_ORDER_REWARDS, size)

        for (i in 0 until size) {
            val task = rewards[i]

            val entry = context.layoutInflater.inflate(R.layout.dialog_challenge_detail_reward, tasksLayout, false)
            (entry.findViewById<View>(R.id.gold_icon) as ImageView).setImageBitmap(HabiticaIconsHelper.imageOfGold())
            val title = entry.findViewById<View>(R.id.reward_title) as TextView
            title.text = EmojiParser.parseEmojis(task.text)
            tasksLayout.addView(entry)
        }
        taskGrouplayout?.addView(taskGroup)
    }

    private fun getLabelByTypeAndCount(type: String, count: Int): String {
        return when (type) {
            Challenge.TASK_ORDER_DAILYS -> context.getString(if (count == 1) R.string.daily else R.string.dailies)
            Challenge.TASK_ORDER_HABITS -> context.getString(if (count == 1) R.string.habit else R.string.habits)
            Challenge.TASK_ORDER_REWARDS -> context.getString(if (count == 1) R.string.reward else R.string.rewards)
            else -> context.getString(if (count == 1) R.string.todo else R.string.todos)
        }
    }

    private fun setJoined(joined: Boolean) {
        joinedHeader?.visibility = if (joined) View.VISIBLE else View.GONE
        leaveButton?.visibility = if (joined) View.VISIBLE else View.GONE

        notJoinedHeader?.visibility = if (joined) View.GONE else View.VISIBLE
        joinButton?.visibility = if (joined) View.GONE else View.VISIBLE
    }

    private fun openLeaderProfile() {
        FullProfileActivity.open(context, challenge?.leaderId)
    }

    private fun openChallengeActivity() {
        val bundle = Bundle()
        bundle.putString(ChallengeDetailActivity.CHALLENGE_ID, challenge?.id)

        val intent = Intent(context, ChallengeDetailActivity::class.java)
        intent.putExtras(bundle)
        context.startActivity(intent)
        this.dialog?.dismiss()
    }

    private fun joinChallenge() {
        this.challengeRepository.joinChallenge(challenge).subscribe(Consumer<Challenge> { this.changeViewsByChallenge(it) }, RxErrorHandler.handleEmptyError())
    }

    private fun leaveChallenge() {
        AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.challenge_leave_title))
                .setMessage(context.getString(R.string.challenge_leave_text, challenge?.name))
                .setPositiveButton(context.getString(R.string.yes)) { dialog, _ ->
                    showRemoveTasksDialog(object : Action1<String> {
                        override fun call(t: String) {
                            challengeRepository.leaveChallenge(challenge, LeaveChallengeBody(t))
                                    .subscribe(Consumer {
                                        challenge.notNull {
                                            challengeLeftAction?.call(it)
                                        }
                                        dialog.dismiss()
                                    }, RxErrorHandler.handleEmptyError())
                        }
                    })
                }
                .setNegativeButton(context.getString(R.string.no)) { dialog, _ -> dialog.dismiss() }.show()
    }

    // refactor as an UseCase later - see ChallengeDetailActivity
    private fun showRemoveTasksDialog(callback: Action1<String>) {
        AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.challenge_remove_tasks_title))
                .setMessage(context.getString(R.string.challenge_remove_tasks_text))
                .setPositiveButton(context.getString(R.string.remove_tasks)) { dialog, _ ->
                    callback.call("remove-all")
                    dialog.dismiss()
                }
                .setNegativeButton(context.getString(R.string.keep_tasks)) { dialog, _ ->
                    callback.call("keep-all")
                    dialog.dismiss()
                }.show()
    }

    companion object {

        fun showDialog(activity: Activity?, challengeRepository: ChallengeRepository, challenge: Challenge, challengeLeftAction: Action1<Challenge>) {
            if (activity == null) {
                return
            }
            val dialogLayout = activity.layoutInflater.inflate(R.layout.dialog_challenge_detail, null)

            val challengeDetailDialogHolder = ChallengeDetailDialogHolder(dialogLayout, activity)

            val builder = AlertDialog.Builder(activity)
                    .setView(dialogLayout)

            challengeDetailDialogHolder.bind(builder.show(), challengeRepository, challenge, challengeLeftAction)
        }
    }
}
