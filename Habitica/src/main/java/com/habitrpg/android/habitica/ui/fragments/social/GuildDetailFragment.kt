package com.habitrpg.android.habitica.ui.fragments.social

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import javax.inject.Inject

class GuildDetailFragment : BaseFragment() {

    @Inject
    lateinit var configManager: AppConfigManager

    val refreshLayout: SwipeRefreshLayout by bindView(R.id.refreshLayout)
    private val guildTitleView: TextView by bindView(R.id.title_view)
    private val guildDescriptionView: TextView by bindView(R.id.guild_description)
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

        leaveGuildButton.setOnClickListener {
            viewModel?.leaveGroup {
                val activity = activity as? MainActivity
                if (activity != null) {
                    HabiticaSnackbar.showSnackbar(activity.floatingMenuWrapper, getString(R.string.left_guild), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
                }
            }
        }
        joinGuildButton.setOnClickListener {
            viewModel?.joinGroup {
                val activity = activity as? MainActivity
                if (activity != null) {
                    HabiticaSnackbar.showSnackbar(activity.floatingMenuWrapper, getString(R.string.joined_guild), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
                }
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
        }
    }

    private fun refresh() {
        viewModel?.retrieveGroup {
            refreshLayout.isRefreshing = false
        }
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun updateGuild(guild: Group?) {
        guildTitleView.text = guild?.name
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
