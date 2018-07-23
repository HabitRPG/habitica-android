package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import net.pherth.android.emoji_library.EmojiParser
import net.pherth.android.emoji_library.EmojiTextView
import java.util.*
import javax.inject.Inject
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.models.members.Member


class ChallengeDetailFragment: BaseMainFragment() {

    @Inject
    lateinit var challengeRepository: ChallengeRepository
    @Inject
    lateinit var socialRepository: SocialRepository

    private val joinButtonWrapper: ViewGroup? by bindView(R.id.join_button_wrapper)
    private val joinButton: Button? by bindView(R.id.join_button)
    private val leaveButonWrapper: ViewGroup? by bindView(R.id.leave_button_wrapper)
    private val leaveButton: Button? by bindView(R.id.leave_button)
    private val challengeName: EmojiTextView? by bindView(R.id.challenge_name)
    private val challengeDescription: EmojiTextView? by bindView(R.id.challenge_description)
    private val challengeLeaderAvatarView: AvatarView? by bindView(R.id.creator_avatarview)
    private val challengeLeaderLabel: UsernameLabel? by bindView(R.id.creator_label)
    private val gemAmountView: TextView? by bindView(R.id.gem_amount)
    private val gemAmountIconView: ImageView? by bindView(R.id.gem_amount_icon)
    private val memberCountView: TextView? by bindView(R.id.participant_count)
    private val memberCountIconView: ImageView? by bindView(R.id.participant_count_icon)
    private val taskGrouplayout: LinearLayout? by bindView(R.id.task_group_layout)

    var challengeID: String? = null

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return container?.inflate(R.layout.fragment_challenge_detail)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gemAmountIconView?.setImageBitmap(HabiticaIconsHelper.imageOfGem_36())
        memberCountIconView?.setImageBitmap(HabiticaIconsHelper.imageOfParticipantIcon())

        challengeID.notNull {
            compositeSubscription.add(challengeRepository.getChallenge(it)
                    .doOnNext {
                        set(it)
                    }
                    .map {
                        return@map (it.leaderId ?: "")
                    }
                    .flatMap { creatorID ->
                        return@flatMap socialRepository.getMember(creatorID)
                    }
                    .subscribe(Consumer { set(it)}, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(challengeRepository.getChallengeTasks(it).subscribe(Consumer { taskList ->
                taskGrouplayout?.removeAllViewsInLayout()

                val todos = ArrayList<Task>()
                val habits = ArrayList<Task>()
                val dailies = ArrayList<Task>()
                val rewards = ArrayList<Task>()

                for (entry in taskList) {
                    when (entry.type) {
                        Task.TYPE_TODO -> todos.add(entry)
                        Task.TYPE_HABIT -> habits.add(entry)
                        Task.TYPE_DAILY -> dailies.add(entry)
                        Task.TYPE_REWARD -> rewards.add(entry)
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
            }, RxErrorHandler.handleEmptyError()))

            compositeSubscription.add(challengeRepository.isChallengeMember(it).subscribe(Consumer { isMember ->
                setJoined(isMember)
            }, RxErrorHandler.handleEmptyError()))
        }

        refresh()
    }

    private fun refresh() {
        challengeID.notNull {id ->
            challengeRepository.retrieveChallenge(id)
                    .flatMap { challengeRepository.retrieveChallengeTasks(id) }
                    .subscribe(Consumer {  }, RxErrorHandler.handleEmptyError(), Action {  })
        }
    }

    private fun set(challenge: Challenge) {
        challengeName?.text = EmojiParser.parseEmojis(challenge.name)
        challengeDescription?.text = MarkdownParser.parseMarkdown(challenge.description)
        challengeLeaderLabel?.username = challenge.leaderName

        gemAmountView?.text = challenge.prize.toString()
        memberCountView?.text = challenge.memberCount.toString()
    }

    private fun set(creator: Member) {
        challengeLeaderAvatarView?.setAvatar(creator)
        challengeLeaderLabel?.tier = creator.contributor?.level ?: 0
        challengeLeaderLabel?.username = creator.displayName
    }

    private fun setJoined(joined: Boolean) {
        joinButton?.visibility = if (!joined) View.VISIBLE else View.GONE
        joinButtonWrapper?.visibility = if (!joined) View.VISIBLE else View.GONE
        leaveButton?.visibility = if (joined) View.VISIBLE else View.GONE
        leaveButonWrapper?.visibility = if (joined) View.VISIBLE else View.GONE
    }

    private fun addHabits(habits: ArrayList<Task>) {
        val taskGroup = taskGrouplayout?.inflate(R.layout.dialog_challenge_detail_task_group)
        val groupName = taskGroup?.findViewById(R.id.task_group_name) as TextView?
        val tasksLayout = taskGroup?.findViewById(R.id.tasks_layout) as LinearLayout?

        groupName?.text = getLabelByTypeAndCount(Challenge.TASK_ORDER_HABITS, habits.size)
        taskGroup?.findViewById<TextView>(R.id.task_count_view)?.text = habits.size.toString()

        val size = habits.size
        for (i in 0 until size) {
            val task = habits[i]
            val entry = tasksLayout?.inflate(R.layout.dialog_challenge_detail_habit)
            val habitTitle = entry?.findViewById(R.id.habit_title) as TextView?

            entry?.findViewById<ImageView>(R.id.lock_icon_plus)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#DFDEDF")))
            entry?.findViewById<ImageView>(R.id.lock_icon_minus)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#DFDEDF")))
            context.notNull {
                if (task.up == true) {
                    entry?.findViewById<ImageView>(R.id.lock_icon_plus)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#B3FFFFFF")))
                    entry?.findViewById<View>(R.id.lock_icon_background_plus)?.setBackgroundColor(ContextCompat.getColor(it, task.mediumTaskColor))
                    val drawable = ContextCompat.getDrawable(it, R.drawable.circle_white)
                    drawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(it, task.darkTaskColor), PorterDuff.Mode.MULTIPLY)
                    entry?.findViewById<View>(R.id.lock_icon_plus)?.setBackgroundDrawable(drawable)
                }
                if (task.down == true) {
                    entry?.findViewById<ImageView>(R.id.lock_icon_minus)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#B3FFFFFF")))
                    entry?.findViewById<View>(R.id.lock_icon_background_minus)?.setBackgroundColor(ContextCompat.getColor(it, task.mediumTaskColor))
                    val drawable = ContextCompat.getDrawable(it, R.drawable.circle_white)
                    drawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(it, task.darkTaskColor), PorterDuff.Mode.MULTIPLY)
                    entry?.findViewById<View>(R.id.lock_icon_plus)?.setBackgroundDrawable(drawable)
                }
            }

            habitTitle?.text = EmojiParser.parseEmojis(task.text)
            tasksLayout?.addView(entry)
        }
        taskGrouplayout?.addView(taskGroup)
    }

    private fun addDailys(dailies: ArrayList<Task>) {
        val taskGroup = taskGrouplayout?.inflate(R.layout.dialog_challenge_detail_task_group)
        val groupName = taskGroup?.findViewById(R.id.task_group_name) as TextView?
        val tasksLayout = taskGroup?.findViewById(R.id.tasks_layout) as LinearLayout?

        val size = dailies.size
        groupName?.text = getLabelByTypeAndCount(Challenge.TASK_ORDER_DAILYS, size)
        taskGroup?.findViewById<TextView>(R.id.task_count_view)?.text = dailies.size.toString()

        for (i in 0 until size) {
            val task = dailies[i]
            val entry = tasksLayout?.inflate(R.layout.dialog_challenge_detail_daily)
            val title = entry?.findViewById(R.id.daily_title) as TextView?
            title?.text = EmojiParser.parseEmojis(task.text)
            entry?.findViewById<ImageView>(R.id.lock_icon)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#949494")))
            context.notNull {
                entry?.findViewById<View>(R.id.lock_icon_background)?.setBackgroundColor(ContextCompat.getColor(it, task.mediumTaskColor))
                val drawable = ContextCompat.getDrawable(it, R.drawable.circle_white)
                drawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(it, task.extraLightTaskColor), PorterDuff.Mode.MULTIPLY)
                entry?.findViewById<View>(R.id.lock_icon)?.setBackgroundDrawable(drawable)
            }
            if (task.checklist != null && task.checklist?.isEmpty() == false) {
                val checklistIndicatorWrapper = entry?.findViewById<View>(R.id.checklistIndicatorWrapper)

                checklistIndicatorWrapper?.visibility = View.VISIBLE

                val checkListAllTextView = entry?.findViewById<View>(R.id.checkListAllTextView) as TextView?
                checkListAllTextView?.text = task.checklist?.size.toString()
            }
            tasksLayout?.addView(entry)
        }
        taskGrouplayout?.addView(taskGroup)
    }

    private fun addTodos(todos: ArrayList<Task>) {
        val taskGroup = taskGrouplayout?.inflate(R.layout.dialog_challenge_detail_task_group)
        val groupName = taskGroup?.findViewById(R.id.task_group_name) as TextView?
        val tasksLayout = taskGroup?.findViewById(R.id.tasks_layout) as LinearLayout?

        val size = todos.size
        groupName?.text = getLabelByTypeAndCount(Challenge.TASK_ORDER_TODOS, size)
        taskGroup?.findViewById<TextView>(R.id.task_count_view)?.text = todos.size.toString()

        for (i in 0 until size) {
            val task = todos[i]
            val entry = tasksLayout?.inflate(R.layout.dialog_challenge_detail_todo)
            val title = entry?.findViewById(R.id.todo_title) as TextView?
            title?.text = EmojiParser.parseEmojis(task.text)
            entry?.findViewById<ImageView>(R.id.lock_icon)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#949494")))
            context.notNull {
                entry?.findViewById<View>(R.id.lock_icon_background)?.setBackgroundColor(ContextCompat.getColor(it, task.mediumTaskColor))
                val drawable = ContextCompat.getDrawable(it, R.drawable.circle_white)
                drawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(it, task.extraLightTaskColor), PorterDuff.Mode.MULTIPLY)
                entry?.findViewById<View>(R.id.lock_icon)?.setBackgroundDrawable(drawable)
            }

            if (task.checklist != null && task.checklist?.isEmpty() == false) {
                val checklistIndicatorWrapper = entry?.findViewById<View>(R.id.checklistIndicatorWrapper)

                checklistIndicatorWrapper?.visibility = View.VISIBLE

                val checkListAllTextView = entry?.findViewById<View>(R.id.checkListAllTextView) as TextView?
                checkListAllTextView?.text = task.checklist?.size.toString()
            }
            tasksLayout?.addView(entry)
        }
        taskGrouplayout?.addView(taskGroup)
    }

    private fun addRewards(rewards: ArrayList<Task>) {
        val taskGroup = taskGrouplayout?.inflate(R.layout.dialog_challenge_detail_task_group)
        val groupName = taskGroup?.findViewById(R.id.task_group_name) as TextView?

        val tasksLayout = taskGroup?.findViewById(R.id.tasks_layout) as LinearLayout?

        val size = rewards.size
        groupName?.text =  getLabelByTypeAndCount(Challenge.TASK_ORDER_REWARDS, size)
        taskGroup?.findViewById<TextView>(R.id.task_count_view)?.text = rewards.size.toString()

        for (i in 0 until size) {
            val task = rewards[i]

            val entry = tasksLayout?.inflate(R.layout.dialog_challenge_detail_reward)
            (entry?.findViewById<View>(R.id.gold_icon) as ImageView?)?.setImageBitmap(HabiticaIconsHelper.imageOfGold())
            val title = entry?.findViewById<View>(R.id.reward_title) as TextView?
            title?.text = EmojiParser.parseEmojis(task.text)
            tasksLayout?.addView(entry)
        }
        taskGrouplayout?.addView(taskGroup)
    }

    private fun getLabelByTypeAndCount(type: String, count: Int): String {
        return when (type) {
            Challenge.TASK_ORDER_DAILYS -> context?.getString(if (count == 1) R.string.daily else R.string.dailies)
            Challenge.TASK_ORDER_HABITS -> context?.getString(if (count == 1) R.string.habit else R.string.habits)
            Challenge.TASK_ORDER_REWARDS -> context?.getString(if (count == 1) R.string.reward else R.string.rewards)
            else -> context?.getString(if (count == 1) R.string.todo else R.string.todos)
        } ?: ""
    }
}