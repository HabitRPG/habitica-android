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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import io.reactivex.functions.Consumer
import javax.inject.Inject

class GuildDetailFragment : BaseFragment() {

    var isMember: Boolean = false
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository
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

    var groupID: String? = null
    var guild: Group? = null
    private var user: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_guild_detail, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        refreshLayout.setOnRefreshListener { this.refresh() }

        compositeSubscription.add(socialRepository.getGroup(groupID ?: "")
                .doOnNext {
                    guild = it
                    updateGuild(it)
                }
                .distinctUntilChanged { group1, group2 -> group1.id == group2.id }
                .flatMap { socialRepository.getMember(it.leaderID) }
                .subscribe(Consumer {
                setLeader(it)
        }, RxErrorHandler.handleEmptyError()))

        guildDescriptionView.movementMethod = LinkMovementMethod.getInstance()

        leaveGuildButton.setOnClickListener {
            compositeSubscription.add(socialRepository.leaveGroup(groupID).subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
        }
        joinGuildButton.setOnClickListener {
            compositeSubscription.add(socialRepository.joinGroup(groupID).subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
        }

        joinGuildButton.visibility = if (isMember) View.GONE else View.VISIBLE
        leaveGuildButton.visibility = if (isMember) View.VISIBLE else View.GONE
    }

    private fun setLeader(leader: Member) {
        leaderAvatarView.setAvatar(leader)
        leaderProfileNameView.username = leader.profile?.name
        leaderProfileNameView.tier = leader.contributor?.level ?: 0
        leaderUsernameView.text = leader.formattedUsername
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GroupFormActivity.GROUP_FORM_ACTIVITY -> {
                if (resultCode == Activity.RESULT_OK) {
                    val bundle = data?.extras
                    this.socialRepository.updateGroup(this.guild,
                            bundle?.getString("name"),
                            bundle?.getString("description"),
                            bundle?.getString("leader"),
                            bundle?.getBoolean("leaderCreateChallenge"))
                            .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                }
            }
        }
    }

    private fun refresh() {
        compositeSubscription.add(socialRepository.retrieveGroup(guild?.id ?: "").subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
    }

    override fun onDestroy() {
        userRepository.close()
        socialRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun updateGuild(guild: Group?) {
        guildTitleView?.text = guild?.name
        guildDescriptionView.text = MarkdownParser.parseMarkdown(guild?.description)
        //gemCountWrapper.visibility = if (group?.balance != null && group.balance > 0) View.VISIBLE else View.GONE
        //gemCountTextView.text = (group?.balance ?: 0 * 4.0).toInt().toString()
    }

    companion object {

        fun newInstance(group: Group?, user: User?): GuildDetailFragment {
            val args = Bundle()

            val fragment = GuildDetailFragment()
            fragment.arguments = args
            fragment.groupID = group?.id
            fragment.user = user
            return fragment
        }
    }

}
