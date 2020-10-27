package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.content.Intent
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentChallengeDetailBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.activities.ChallengeFormActivity
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.setMarkdown
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.helpers.EmojiParser
import java.util.*
import javax.inject.Inject


class ChallengeDetailFragment: BaseMainFragment<FragmentChallengeDetailBinding>() {

    @Inject
    lateinit var challengeRepository: ChallengeRepository
    @Inject
    lateinit var socialRepository: SocialRepository

    override var binding: FragmentChallengeDetailBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentChallengeDetailBinding {
        return FragmentChallengeDetailBinding.inflate(inflater, container, false)
    }

    var challengeID: String? = null
    var challenge: Challenge? = null
    private var isCreator = false

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @Suppress("ReturnCount")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = ChallengeDetailFragmentArgs.fromBundle(it)
            challengeID = args.challengeID
        }

        binding?.gemAmountIcon?.setImageBitmap(HabiticaIconsHelper.imageOfGem_36())
        binding?.participantCountIcon?.setImageBitmap(HabiticaIconsHelper.imageOfParticipantsIcon())
        binding?.challengeDescription?.movementMethod = LinkMovementMethod.getInstance()

        binding?.challengeCreatorWrapper?.setOnClickListener {
            val leaderID = challenge?.leaderId ?: return@setOnClickListener
            FullProfileActivity.open(leaderID)
        }

        challengeID?.let {id ->
            compositeSubscription.add(challengeRepository.getChallenge(id)
                    .doOnNext {
                        set(it)
                    }
                    .map {
                        return@map (it.leaderId ?: "")
                    }
                    .filter { it.isNotEmpty() }
                    .flatMap { creatorID ->
                        return@flatMap socialRepository.getMember(creatorID)
                    }
                    .subscribe({ set(it)}, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(challengeRepository.getChallengeTasks(id).subscribe({ taskList ->
                binding?.taskGroupLayout?.removeAllViewsInLayout()

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

            compositeSubscription.add(challengeRepository.isChallengeMember(id).subscribe({ isMember ->
                setJoined(isMember)
            }, RxErrorHandler.handleEmptyError()))
        }

        binding?.joinButton?.setOnClickListener { challenge?.let { challenge -> challengeRepository.joinChallenge(challenge)
                .flatMap { userRepository.retrieveUser(true) }
                .subscribe({}, RxErrorHandler.handleEmptyError()) } }
        binding?.leaveButton?.setOnClickListener { showChallengeLeaveDialog() }

        refresh()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_challenge_details, menu)
        val editMenuItem = menu.findItem(R.id.action_edit)
        editMenuItem?.isVisible = isCreator
        val endChallengeMenuItem = menu.findItem(R.id.action_end_challenge)
        endChallengeMenuItem?.isVisible = isCreator
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_edit -> {
                val intent = Intent(getActivity(), ChallengeFormActivity::class.java)
                val bundle = Bundle()
                bundle.putString(ChallengeFormActivity.CHALLENGE_ID_KEY, challengeID)
                intent.putExtras(bundle)
                startActivity(intent)
                return true
            }
            R.id.action_share -> {
                val shareGuildIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "${context?.getString(R.string.base_url)}/challenges/$challengeID")
                    type = "text/plain"
                }
                startActivity(Intent.createChooser(shareGuildIntent, context?.getString(R.string.share_challenge_with)))
            }
            R.id.action_end_challenge -> {
                showEndChallengeDialog()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showEndChallengeDialog() {
        val context = context ?: return
        val dialog = HabiticaAlertDialog(context)
        dialog.setTitle(R.string.action_end_challenge)
        dialog.setMessage(R.string.end_challenge_description)
        dialog.addButton(R.string.open_website, true, false) { _, _ ->
            val uriUrl = "https://habitica.com/challenges/${challengeID}".toUri()
            val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
            startActivity(launchBrowser)
        }
        dialog.addCloseButton()
        dialog.show()
    }

    private fun refresh() {
        challengeID?.let {id ->
            challengeRepository.retrieveChallenge(id)
                    .flatMap { challengeRepository.retrieveChallengeTasks(id) }
                    .subscribe({ }, RxErrorHandler.handleEmptyError(), { })
        }
    }

    private fun set(challenge: Challenge) {
        this.challenge = challenge
        binding?.challengeName?.text = EmojiParser.parseEmojis(challenge.name)
        binding?.challengeDescription?.setMarkdown(challenge.description)
        binding?.creatorLabel?.username = challenge.leaderName

        binding?.gemAmount?.text = challenge.prize.toString()
        binding?.participantCount?.text = challenge.memberCount.toString()
    }

    private fun set(creator: Member) {
        binding?.creatorAvatarview?.setAvatar(creator)
        binding?.creatorLabel?.tier = creator.contributor?.level ?: 0
        binding?.creatorLabel?.username = creator.displayName
        isCreator = creator.id == user?.id
        this.activity?.invalidateOptionsMenu()
    }

    private fun setJoined(joined: Boolean) {
        binding?.joinButton?.visibility = if (!joined) View.VISIBLE else View.GONE
        binding?.joinButtonWrapper?.visibility = if (!joined) View.VISIBLE else View.GONE
        binding?.leaveButton?.visibility = if (joined) View.VISIBLE else View.GONE
        binding?.leaveButtonWrapper?.visibility = if (joined) View.VISIBLE else View.GONE
    }

    private fun addHabits(habits: ArrayList<Task>) {
        val taskGroup = binding?.taskGroupLayout?.inflate(R.layout.dialog_challenge_detail_task_group)
        val groupName = taskGroup?.findViewById(R.id.task_group_name) as? TextView
        val tasksLayout = taskGroup?.findViewById(R.id.tasks_layout) as? LinearLayout

        groupName?.text = getLabelByTypeAndCount(Challenge.TASK_ORDER_HABITS, habits.size)
        taskGroup?.findViewById<TextView>(R.id.task_count_view)?.text = habits.size.toString()

        val size = habits.size
        for (i in 0 until size) {
            val task = habits[i]
            val entry = tasksLayout?.inflate(R.layout.dialog_challenge_detail_habit)
            val habitTitle = entry?.findViewById(R.id.habit_title) as? TextView

            entry?.findViewById<ImageView>(R.id.lock_icon_plus)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#DFDEDF")))
            entry?.findViewById<ImageView>(R.id.lock_icon_minus)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#DFDEDF")))
            context?.let {
                if (task.up == true) {
                    entry?.findViewById<ImageView>(R.id.lock_icon_plus)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#B3FFFFFF")))
                    entry?.findViewById<View>(R.id.lock_icon_background_plus)?.setBackgroundColor(ContextCompat.getColor(it, task.mediumTaskColor))
                    val drawable = ContextCompat.getDrawable(it, R.drawable.circle_white)
                    drawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(it, task.darkTaskColor), PorterDuff.Mode.MULTIPLY)
                    entry?.findViewById<View>(R.id.lock_icon_plus)?.background = drawable
                }
                if (task.down == true) {
                    entry?.findViewById<ImageView>(R.id.lock_icon_minus)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#B3FFFFFF")))
                    entry?.findViewById<View>(R.id.lock_icon_background_minus)?.setBackgroundColor(ContextCompat.getColor(it, task.mediumTaskColor))
                    val drawable = ContextCompat.getDrawable(it, R.drawable.circle_white)
                    drawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(it, task.darkTaskColor), PorterDuff.Mode.MULTIPLY)
                    entry?.findViewById<View>(R.id.lock_icon_plus)?.background = drawable
                }
            }

            habitTitle?.text = EmojiParser.parseEmojis(task.text)
            tasksLayout?.addView(entry)
        }
        binding?.taskGroupLayout?.addView(taskGroup)
    }

    private fun addDailys(dailies: ArrayList<Task>) {
        val taskGroup = binding?.taskGroupLayout?.inflate(R.layout.dialog_challenge_detail_task_group)
        val groupName = taskGroup?.findViewById(R.id.task_group_name) as? TextView
        val tasksLayout = taskGroup?.findViewById(R.id.tasks_layout) as? LinearLayout

        val size = dailies.size
        groupName?.text = getLabelByTypeAndCount(Challenge.TASK_ORDER_DAILYS, size)
        taskGroup?.findViewById<TextView>(R.id.task_count_view)?.text = dailies.size.toString()

        for (i in 0 until size) {
            val task = dailies[i]
            val entry = tasksLayout?.inflate(R.layout.dialog_challenge_detail_daily)
            val title = entry?.findViewById(R.id.daily_title) as? TextView?
            title?.text = EmojiParser.parseEmojis(task.text)
            entry?.findViewById<ImageView>(R.id.lock_icon)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#949494")))
            context?.let {
                entry?.findViewById<View>(R.id.lock_icon_background)?.setBackgroundColor(ContextCompat.getColor(it, task.mediumTaskColor))
                val drawable = ContextCompat.getDrawable(it, R.drawable.circle_white)
                drawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(it, task.extraLightTaskColor), PorterDuff.Mode.MULTIPLY)
                entry?.findViewById<View>(R.id.lock_icon)?.background = drawable
            }
            if (task.checklist != null && task.checklist?.isEmpty() == false) {
                val checklistIndicatorWrapper = entry?.findViewById<View>(R.id.checklistIndicatorWrapper)

                checklistIndicatorWrapper?.visibility = View.VISIBLE

                val checkListAllTextView = entry?.findViewById<View>(R.id.checkListAllTextView) as? TextView
                checkListAllTextView?.text = task.checklist?.size.toString()
            }
            tasksLayout?.addView(entry)
        }
        binding?.taskGroupLayout?.addView(taskGroup)
    }

    private fun addTodos(todos: ArrayList<Task>) {
        val taskGroup = binding?.taskGroupLayout?.inflate(R.layout.dialog_challenge_detail_task_group)
        val groupName = taskGroup?.findViewById(R.id.task_group_name) as? TextView
        val tasksLayout = taskGroup?.findViewById(R.id.tasks_layout) as? LinearLayout

        val size = todos.size
        groupName?.text = getLabelByTypeAndCount(Challenge.TASK_ORDER_TODOS, size)
        taskGroup?.findViewById<TextView>(R.id.task_count_view)?.text = todos.size.toString()

        for (i in 0 until size) {
            val task = todos[i]
            val entry = tasksLayout?.inflate(R.layout.dialog_challenge_detail_todo)
            val title = entry?.findViewById(R.id.todo_title) as? TextView
            title?.text = EmojiParser.parseEmojis(task.text)
            entry?.findViewById<ImageView>(R.id.lock_icon)?.setImageBitmap(HabiticaIconsHelper.imageOfLocked(Color.parseColor("#949494")))
            context?.let {
                entry?.findViewById<View>(R.id.lock_icon_background)?.setBackgroundColor(ContextCompat.getColor(it, task.mediumTaskColor))
                val drawable = ContextCompat.getDrawable(it, R.drawable.circle_white)
                drawable?.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(it, task.extraLightTaskColor), PorterDuff.Mode.MULTIPLY)
                entry?.findViewById<View>(R.id.lock_icon)?.background = drawable
            }

            if (task.checklist != null && task.checklist?.isEmpty() == false) {
                val checklistIndicatorWrapper = entry?.findViewById<View>(R.id.checklistIndicatorWrapper)

                checklistIndicatorWrapper?.visibility = View.VISIBLE

                val checkListAllTextView = entry?.findViewById<View>(R.id.checkListAllTextView) as? TextView
                checkListAllTextView?.text = task.checklist?.size.toString()
            }
            tasksLayout?.addView(entry)
        }
        binding?.taskGroupLayout?.addView(taskGroup)
    }

    private fun addRewards(rewards: ArrayList<Task>) {
        val taskGroup = binding?.taskGroupLayout?.inflate(R.layout.dialog_challenge_detail_task_group)
        val groupName = taskGroup?.findViewById(R.id.task_group_name) as? TextView

        val tasksLayout = taskGroup?.findViewById(R.id.tasks_layout) as? LinearLayout

        val size = rewards.size
        groupName?.text =  getLabelByTypeAndCount(Challenge.TASK_ORDER_REWARDS, size)
        taskGroup?.findViewById<TextView>(R.id.task_count_view)?.text = rewards.size.toString()

        for (i in 0 until size) {
            val task = rewards[i]

            val entry = tasksLayout?.inflate(R.layout.dialog_challenge_detail_reward)
            (entry?.findViewById<View>(R.id.gold_icon) as? ImageView)?.setImageBitmap(HabiticaIconsHelper.imageOfGold())
            val title = entry?.findViewById<View>(R.id.reward_title) as? TextView
            title?.text = EmojiParser.parseEmojis(task.text)
            tasksLayout?.addView(entry)
        }
        binding?.taskGroupLayout?.addView(taskGroup)
    }

    private fun getLabelByTypeAndCount(type: String, count: Int): String {
        return when (type) {
            Challenge.TASK_ORDER_DAILYS -> context?.getString(if (count == 1) R.string.daily else R.string.dailies)
            Challenge.TASK_ORDER_HABITS -> context?.getString(if (count == 1) R.string.habit else R.string.habits)
            Challenge.TASK_ORDER_REWARDS -> context?.getString(if (count == 1) R.string.reward else R.string.rewards)
            else -> context?.getString(if (count == 1) R.string.todo else R.string.todos)
        } ?: ""
    }

    private fun showChallengeLeaveDialog() {
        val context = context ?: return
        val alert = HabiticaAlertDialog(context)
        alert.setTitle(this.getString(R.string.challenge_leave_title))
        alert.setMessage(this.getString(R.string.challenge_leave_description))
        alert.addButton(R.string.leave_keep_tasks, true) { _, _ ->
            val challenge = challenge ?: return@addButton
            challengeRepository.leaveChallenge(challenge, "keep-all").subscribe({}, RxErrorHandler.handleEmptyError())
        }
        alert.addButton(R.string.leave_delte_tasks, isPrimary = false, isDestructive = true) { _, _ ->
            val challenge = challenge ?: return@addButton
            challengeRepository.leaveChallenge(challenge, "remove-all").subscribe({}, RxErrorHandler.handleEmptyError())
        }
        alert.setExtraCloseButtonVisibility(View.VISIBLE)
        alert.show()
    }
}
