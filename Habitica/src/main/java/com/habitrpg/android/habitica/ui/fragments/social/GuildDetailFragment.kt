package com.habitrpg.android.habitica.ui.fragments.social

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.MainNavDirections
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.activities.GroupInviteActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaIcons
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import java.util.*
import javax.inject.Inject

class GuildDetailFragment : BaseFragment() {

    @Inject
    lateinit var configManager: AppConfigManager

    val refreshLayout: SwipeRefreshLayout by bindView(R.id.refreshLayout)
    private val guildTitleView: TextView by bindView(R.id.title_view)
    private val guildMembersIconView: ImageView by bindView(R.id.guild_members_icon)
    private val guildMembersTextView: TextView by bindView(R.id.guild_members_text)
    private val guildBankIconView: ImageView by bindView(R.id.guild_bank_icon)
    private val guildBankTextView: TextView by bindView(R.id.guild_bank_text)
    private val guildSummaryView: TextView by bindView(R.id.guild_summary)
    private val guildDescriptionView: TextView by bindView(R.id.guild_description)
    private val leaderWrapperView: ViewGroup by bindView(R.id.leader_wrapper)
    private val leaderAvatarView: AvatarView by bindView(R.id.leader_avatar_view)
    private val leaderProfileNameView: UsernameLabel by bindView(R.id.leader_profile_name)
    private val leaderUsernameView: TextView by bindView(R.id.leader_username)

    val inviteToGuildButton: Button by bindView(R.id.invite_button)
    private val joinGuildButton: Button by bindView(R.id.join_button)
    private val leaveGuildButton: Button by bindView(R.id.leave_button)

    var viewModel: GroupViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_guild_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        refreshLayout.setOnRefreshListener { this.refresh() }

        viewModel?.getGroupData()?.observe(viewLifecycleOwner, Observer { updateGuild(it) })
        viewModel?.getLeaderData()?.observe(viewLifecycleOwner, Observer { setLeader(it) })
        viewModel?.getIsMemberData()?.observe(viewLifecycleOwner, Observer { updateMembership(it) })

        guildDescriptionView.movementMethod = LinkMovementMethod.getInstance()
        guildBankIconView.setImageBitmap(HabiticaIconsHelper.imageOfGem())
        leaveGuildButton.setOnClickListener {
            viewModel?.leaveGroup {
                val activity = activity as? MainActivity
                if (activity != null) {
                    HabiticaSnackbar.showSnackbar(activity.snackbarContainer, getString(R.string.left_guild), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
                }
            }
        }
        joinGuildButton.setOnClickListener {
            viewModel?.joinGroup {
                val activity = activity as? MainActivity
                if (activity != null) {
                    HabiticaSnackbar.showSnackbar(activity.snackbarContainer, getString(R.string.joined_guild), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
                }
            }
        }
        inviteToGuildButton.setOnClickListener {
            val intent = Intent(activity, GroupInviteActivity::class.java)
            startActivityForResult(intent, GroupInviteActivity.RESULT_SEND_INVITES)
        }
        leaderWrapperView.setOnClickListener {
            viewModel?.getGroupData()?.value?.leaderID?.let {leaderID ->
                val profileDirections = MainNavDirections.openProfileActivity(leaderID)
                MainNavigationController.navigate(profileDirections)
            }
        }
    }

    private fun setLeader(leader: Member?) {
        if (leader == null) {
            return
        }
        leaderAvatarView.setAvatar(leader)
        leaderProfileNameView.username = leader.profile?.name
        leaderProfileNameView.tier = leader.contributor?.level ?: 0
        leaderUsernameView.text = leader.formattedUsername
    }

    private fun updateMembership(isMember: Boolean?) {
        joinGuildButton.visibility = if (isMember == true) View.GONE else View.VISIBLE
        leaveGuildButton.visibility = if (isMember == true) View.VISIBLE else View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GroupFormActivity.GROUP_FORM_ACTIVITY -> {
                if (resultCode == Activity.RESULT_OK) {
                    viewModel?.updateGroup(data?.extras)
                }
            }
            GroupInviteActivity.RESULT_SEND_INVITES -> {
                if (resultCode == Activity.RESULT_OK) {
                    val inviteData = HashMap<String, Any>()
                    inviteData["inviter"] = viewModel?.getUserData()?.value?.profile?.name ?: ""
                    if (data?.getBooleanExtra(GroupInviteActivity.IS_EMAIL_KEY, false) == true) {
                        val emails = data.getStringArrayExtra(GroupInviteActivity.EMAILS_KEY)
                        val invites = ArrayList<HashMap<String, String>>()
                        for (email in emails) {
                            val invite = HashMap<String, String>()
                            invite["name"] = ""
                            invite["email"] = email
                            invites.add(invite)
                        }
                        inviteData["emails"] = invites
                    } else {
                        val userIDs = data?.getStringArrayExtra(GroupInviteActivity.USER_IDS_KEY)
                        val invites = ArrayList<String>()
                        Collections.addAll(invites, *userIDs)
                        inviteData["usernames"] = invites
                    }
                    viewModel?.inviteToGroup(inviteData)
                }
            }
        }
    }

    private fun refresh() {
        viewModel?.retrieveGroup {
            refreshLayout.isRefreshing = false
        }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun updateGuild(guild: Group?) {
        guildTitleView.text = guild?.name
        guildMembersIconView.setImageBitmap(HabiticaIcons.imageOfGuildCrestMedium((guild?.memberCount ?: 0).toFloat()))
        guildMembersTextView.text = guild?.memberCount.toString()
        guildBankTextView.text = guild?.gemCount.toString()
        guildSummaryView.text = MarkdownParser.parseMarkdown(guild?.summary)
        guildDescriptionView.text = MarkdownParser.parseMarkdown(guild?.description)
    }

    companion object {
        fun newInstance(viewModel: GroupViewModel?, user: User?): GuildDetailFragment {
            val args = Bundle()

            val fragment = GuildDetailFragment()
            fragment.arguments = args
            fragment.viewModel = viewModel
            return fragment
        }
    }
}
