package com.habitrpg.android.habitica.ui.fragments.social.guilds

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.MainNavDirections
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentGuildDetailBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.activities.GroupInviteActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.common.habitica.helpers.setMarkdown
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewModel
import com.habitrpg.common.habitica.views.HabiticaIcons
import com.habitrpg.common.habitica.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.SnackbarActivity
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class GuildDetailFragment : BaseFragment<FragmentGuildDetailBinding>() {

    @Inject
    lateinit var configManager: AppConfigManager

    override var binding: FragmentGuildDetailBinding? = null

    @Inject
    lateinit var challengeRepository: ChallengeRepository

    @Inject
    lateinit var userRepository: UserRepository

    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGuildDetailBinding {
        return FragmentGuildDetailBinding.inflate(inflater, container, false)
    }

    var viewModel: GroupViewModel? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.refreshLayout?.setOnRefreshListener { this.refresh() }

        viewModel?.getGroupData()?.observe(viewLifecycleOwner, { updateGuild(it) })
        viewModel?.getLeaderData()?.observe(viewLifecycleOwner, { setLeader(it) })
        viewModel?.getIsMemberData()?.observe(viewLifecycleOwner, { updateMembership(it) })

        binding?.guildDescription?.movementMethod = LinkMovementMethod.getInstance()
        binding?.guildSummary?.movementMethod = LinkMovementMethod.getInstance()
        binding?.guildBankIcon?.setImageBitmap(HabiticaIconsHelper.imageOfGem())
        binding?.leaveButton?.setOnClickListener {
            leaveGuild()
        }
        binding?.joinButton?.setOnClickListener {
            viewModel?.joinGroup {
                (this.activity as? SnackbarActivity)?.showSnackbar(title = getString(R.string.joined_guild))
            }
        }
        binding?.inviteButton?.setOnClickListener {
            val intent = Intent(activity, GroupInviteActivity::class.java)
            sendInvitesResult.launch(intent)
        }
        binding?.leaderWrapper?.setOnClickListener {
            viewModel?.leaderID?.let { leaderID ->
                val profileDirections = MainNavDirections.openProfileActivity(leaderID)
                MainNavigationController.navigate(profileDirections)
            }
        }
    }

    private fun setLeader(leader: Member?) {
        if (leader == null) {
            return
        }
        binding?.leaderAvatarView?.setAvatar(leader)
        binding?.leaderProfileName?.username = leader.displayName
        binding?.leaderProfileName?.tier = leader.contributor?.level ?: 0
        binding?.leaderUsername?.text = leader.formattedUsername
    }

    private fun updateMembership(isMember: Boolean?) {
        binding?.joinButton?.visibility = if (isMember == true) View.GONE else View.VISIBLE
        binding?.leaveButton?.visibility = if (isMember == true) View.VISIBLE else View.GONE
    }

    private val sendInvitesResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val inviteData = HashMap<String, Any>()
            inviteData["inviter"] = viewModel?.user?.value?.profile?.name ?: ""
            val emails = it.data?.getStringArrayExtra(GroupInviteActivity.EMAILS_KEY)
            if (emails != null && emails.isNotEmpty()) {
                val invites = ArrayList<HashMap<String, String>>()
                emails.forEach { email ->
                    val invite = HashMap<String, String>()
                    invite["name"] = ""
                    invite["email"] = email
                    invites.add(invite)
                }
                inviteData["emails"] = invites
            }
            val userIDs = it.data?.getStringArrayExtra(GroupInviteActivity.USER_IDS_KEY)
            if (userIDs != null && userIDs.isNotEmpty()) {
                val invites = ArrayList<String>()
                userIDs.forEach { invites.add(it) }
                inviteData["usernames"] = invites
            }
            viewModel?.inviteToGroup(inviteData)
        }
    }

    private fun refresh() {
        viewModel?.retrieveGroup {
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    private fun getGroupChallenges(): List<Challenge> {
        val groupChallenges = mutableListOf<Challenge>()
        userRepository.getUserFlowable().forEach {
            it.challenges?.forEach {
                challengeRepository.getChallenge(it.challengeID).forEach {
                    if (it.groupId.equals(viewModel?.groupID)) {
                        groupChallenges.add(it)
                    }
                }
            }
        }
        return groupChallenges
    }

    internal fun leaveGuild() {
        val context = context
        if (context != null) {
            val groupChallenges = getGroupChallenges()
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500)
                if (groupChallenges.isNotEmpty()) {
                    val alert = HabiticaAlertDialog(context)
                    alert.setTitle(R.string.guild_challenges)
                    alert.setMessage(R.string.leave_guild_challenges_confirmation)
                    alert.addButton(R.string.keep_challenges, true) { _, _ ->
                        viewModel?.leaveGroup(groupChallenges, true) { showLeaveSnackbar() }
                    }
                    alert.addButton(R.string.leave_challenges_delete_tasks, isPrimary = false, isDestructive = true) { _, _ ->
                        viewModel?.leaveGroup(groupChallenges, false) { showLeaveSnackbar() }
                    }
                    alert.setExtraCloseButtonVisibility(View.VISIBLE)
                    alert.show()
                } else {
                    val alert = HabiticaAlertDialog(context)
                    alert.setTitle(R.string.leave_guild_confirmation)
                    alert.setMessage(R.string.rejoin_guild)
                    alert.addButton(R.string.leave, isPrimary = true, isDestructive = true) { _, _ ->
                        viewModel?.leaveGroup(groupChallenges, false) {
                            showLeaveSnackbar()
                        }
                    }
                    alert.setExtraCloseButtonVisibility(View.VISIBLE)
                    alert.show()
                }
            }
        }
    }

    private fun showLeaveSnackbar() {
        (this.activity as? MainActivity)?.showSnackbar(title = getString(R.string.left_guild))
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun updateGuild(guild: Group?) {
        binding?.titleView?.text = guild?.name
        binding?.guildMembersIcon?.setImageBitmap(HabiticaIcons.imageOfGuildCrestMedium((guild?.memberCount ?: 0).toFloat()))
        binding?.guildMembersText?.text = guild?.memberCount.toString()
        binding?.guildBankText?.text = guild?.gemCount.toString()
        binding?.guildSummary?.setMarkdown(guild?.summary)
        binding?.guildDescription?.setMarkdown(guild?.description)
    }

    companion object {
        fun newInstance(viewModel: GroupViewModel?): GuildDetailFragment {
            val args = Bundle()

            val fragment = GuildDetailFragment()
            fragment.arguments = args
            fragment.viewModel = viewModel
            return fragment
        }
    }
}
