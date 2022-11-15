package com.habitrpg.android.habitica.ui.fragments.social.challenges

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.DialogChallengeDetailTaskGroupBinding
import com.habitrpg.android.habitica.databinding.FragmentChallengeDetailBinding
import com.habitrpg.android.habitica.extensions.addCloseButton
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.ui.activities.ChallengeFormActivity
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewHolders.tasks.DailyViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.tasks.HabitViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.tasks.RewardViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.tasks.TodoViewHolder
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.helpers.EmojiParser
import com.habitrpg.common.habitica.helpers.setMarkdown
import com.habitrpg.shared.habitica.models.tasks.TaskType
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

class ChallengeDetailFragment : BaseMainFragment<FragmentChallengeDetailBinding>() {

    @Inject
    lateinit var challengeRepository: ChallengeRepository
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userViewModel: MainUserViewModel

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    @Suppress("ReturnCount")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showsBackButton = true
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

        loadTasks()

        binding?.joinButton?.setOnClickListener {
            challenge?.let { challenge ->
                challengeRepository.joinChallenge(challenge)
                    .subscribe({
                               lifecycleScope.launch(ExceptionHandler.coroutine()) {
                                   userRepository.retrieveUser(true)
                               }
                    }, ExceptionHandler.rx())
            }
        }
        binding?.leaveButton?.setOnClickListener { showChallengeLeaveDialog() }

        refresh()
    }

    private fun loadTasks() {
        challengeID?.let { id ->
            compositeSubscription.add(
                challengeRepository.getChallenge(id)
                    .doOnNext {
                        set(it)
                    }
                    .map {
                        return@map (it.leaderId ?: "")
                    }
                    .filter { it.isNotEmpty() }
                    .subscribe({
                        lifecycleScope.launch(ExceptionHandler.coroutine()) {
                            set(socialRepository.retrieveMember(it))
                        }
                    }, ExceptionHandler.rx())
            )
            compositeSubscription.add(
                challengeRepository.getChallengeTasks(id).subscribe(
                    { taskList ->
                        binding?.taskGroupLayout?.removeAllViewsInLayout()

                        val todos = ArrayList<Task>()
                        val habits = ArrayList<Task>()
                        val dailies = ArrayList<Task>()
                        val rewards = ArrayList<Task>()

                        for (entry in taskList) {
                            val type = entry.type ?: continue
                            when (type) {
                                TaskType.TODO -> todos.add(entry)
                                TaskType.HABIT -> habits.add(entry)
                                TaskType.DAILY -> dailies.add(entry)
                                TaskType.REWARD -> rewards.add(entry)
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
                    },
                    ExceptionHandler.rx()
                )
            )

            compositeSubscription.add(
                challengeRepository.isChallengeMember(id).subscribe(
                    { isMember ->
                        setJoined(isMember)
                    },
                    ExceptionHandler.rx()
                )
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
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
            val uriUrl = "https://habitica.com/challenges/$challengeID".toUri()
            val launchBrowser = Intent(Intent.ACTION_VIEW, uriUrl)
            val l = context.packageManager.queryIntentActivities(launchBrowser, PackageManager.MATCH_DEFAULT_ONLY)
            val notHabitica = l.firstOrNull { !it.activityInfo.processName.contains("habitica") }
            if (notHabitica != null) {
                launchBrowser.setPackage(notHabitica.activityInfo.processName)
            }
            startActivity(launchBrowser)
        }
        dialog.addCloseButton()
        dialog.show()
    }

    private fun refresh() {
        challengeID?.let { id ->
            challengeRepository.retrieveChallenge(id)
                .flatMap { challengeRepository.retrieveChallengeTasks(id) }
                .subscribe({ taskList ->
                    if (binding?.taskGroupLayout?.childCount == 0 && taskList.tasks.isNotEmpty()) {
                        loadTasks()
                    }
                }, {
                    if (it is HttpException && it.code() == 404) {
                        MainNavigationController.navigateBack()
                    }
                    ExceptionHandler.reportError(it)
                })
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

    private fun set(creator: Member?) {
        if (creator == null) return
        binding?.creatorAvatarview?.setAvatar(creator)
        binding?.creatorLabel?.tier = creator.contributor?.level ?: 0
        binding?.creatorLabel?.username = creator.displayName
        isCreator = creator.id == userViewModel.userID
        this.activity?.invalidateOptionsMenu()
    }

    private fun setJoined(joined: Boolean) {
        binding?.joinButton?.visibility = if (!joined) View.VISIBLE else View.GONE
        binding?.joinButtonWrapper?.visibility = if (!joined) View.VISIBLE else View.GONE
        binding?.leaveButton?.visibility = if (joined) View.VISIBLE else View.GONE
        binding?.leaveButtonWrapper?.visibility = if (joined) View.VISIBLE else View.GONE
    }

    private fun addHabits(habits: ArrayList<Task>) {
        val groupBinding = DialogChallengeDetailTaskGroupBinding.inflate(layoutInflater, binding?.taskGroupLayout, true)
        groupBinding.taskGroupName.text = getLabelByTypeAndCount(Challenge.TASK_ORDER_HABITS, habits.size)
        groupBinding.taskCountView.text = habits.size.toString()
        for (i in 0 until habits.size) {
            val task = habits[i]
            val entry = groupBinding.tasksLayout.inflate(R.layout.habit_item_card)
            val viewHolder = HabitViewHolder(entry, { _, _ -> }, {}, {}, null)
            viewHolder.isLocked = true
            viewHolder.bind(task, i, "normal")
            groupBinding.tasksLayout.addView(entry)
        }
    }

    private fun addDailys(dailies: ArrayList<Task>) {
        val groupBinding = DialogChallengeDetailTaskGroupBinding.inflate(layoutInflater, binding?.taskGroupLayout, true)
        groupBinding.taskGroupName.text = getLabelByTypeAndCount(Challenge.TASK_ORDER_DAILYS, dailies.size)
        groupBinding.taskCountView.text = dailies.size.toString()

        for (i in 0 until dailies.size) {
            val task = dailies[i]
            val entry = groupBinding.tasksLayout.inflate(R.layout.daily_item_card)
            val viewHolder = DailyViewHolder(entry, { _, _ -> }, { _, _ -> }, {}, {}, null)
            viewHolder.isLocked = true
            viewHolder.bind(task, i, "normal")
            groupBinding.tasksLayout.addView(entry)
        }
    }

    private fun addTodos(todos: ArrayList<Task>) {
        val groupBinding = DialogChallengeDetailTaskGroupBinding.inflate(layoutInflater, binding?.taskGroupLayout, true)
        groupBinding.taskGroupName.text = getLabelByTypeAndCount(Challenge.TASK_ORDER_TODOS, todos.size)
        groupBinding.taskCountView.text = todos.size.toString()

        for (i in 0 until todos.size) {
            val task = todos[i]
            val entry = groupBinding.tasksLayout.inflate(R.layout.todo_item_card)
            val viewHolder = TodoViewHolder(entry, { _, _ -> }, { _, _ -> }, {}, {}, null)
            viewHolder.isLocked = true
            viewHolder.bind(task, i, "normal")
            groupBinding.tasksLayout.addView(entry)
        }
    }

    private fun addRewards(rewards: ArrayList<Task>) {
        val groupBinding = DialogChallengeDetailTaskGroupBinding.inflate(layoutInflater, binding?.taskGroupLayout, true)
        groupBinding.taskGroupName.text = getLabelByTypeAndCount(Challenge.TASK_ORDER_REWARDS, rewards.size)
        groupBinding.taskCountView.text = rewards.size.toString()

        for (i in 0 until rewards.size) {
            val task = rewards[i]
            val entry = groupBinding.tasksLayout.inflate(R.layout.reward_item_card)
            val viewHolder = RewardViewHolder(entry, { _, _ -> }, {}, {}, null)
            viewHolder.isLocked = true
            viewHolder.bind(task, i, true, "normal", null)
            groupBinding.tasksLayout.addView(entry)
        }
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
            challengeRepository.leaveChallenge(challenge, "keep-all").subscribe({}, ExceptionHandler.rx())
        }
        alert.addButton(R.string.leave_delete_tasks, isPrimary = false, isDestructive = true) { _, _ ->
            val challenge = challenge ?: return@addButton
            challengeRepository.leaveChallenge(challenge, "remove-all").subscribe({}, ExceptionHandler.rx())
        }
        alert.setExtraCloseButtonVisibility(View.VISIBLE)
        alert.show()
    }
}
