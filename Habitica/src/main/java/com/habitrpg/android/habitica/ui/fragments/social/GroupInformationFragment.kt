package com.habitrpg.android.habitica.ui.fragments.social

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.invitations.PartyInvite
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.GroupFormActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_group_info.*
import javax.inject.Inject


class GroupInformationFragment : BaseFragment() {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var configManager: AppConfigManager

    var group: Group? = null
    set(value) {
        field = value
        updateGroup(value)
    }
    private var user: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
            inflater.inflate(R.layout.fragment_group_info, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        refreshLayout?.setOnRefreshListener { this.refresh() }

        if (user != null) {
            setUser(user)
        } else {
            compositeSubscription.add(userRepository.getUser().subscribe(Consumer {
                user = it
                setUser(user)
            }, RxErrorHandler.handleEmptyError()))
        }

        updateGroup(group)

        buttonPartyInviteAccept.setOnClickListener {
            val userId = user?.invitations?.party?.id
            if (userId != null) {
                socialRepository.joinGroup(userId)
                        .doOnNext { setInvitation(null) }
                        .flatMap { userRepository.retrieveUser(false) }
                        .flatMap { socialRepository.retrieveGroup("party") }
                        .flatMap<List<Member>> { group1 -> socialRepository.retrieveGroupMembers(group1.id, true) }
                        .subscribe(Consumer {  }, RxErrorHandler.handleEmptyError())
            }
        }

        buttonPartyInviteReject.setOnClickListener {
            val userId = user?.invitations?.party?.id
            if (userId != null) {
                socialRepository.rejectGroupInvite(userId)
                        .subscribe(Consumer { setInvitation(null) }, RxErrorHandler.handleEmptyError())
            }
        }

        username_textview.setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(context?.getString(R.string.username), user?.username)
            clipboard?.primaryClip = clip
            val activity = activity as? MainActivity
            if (activity != null) {
                HabiticaSnackbar.showSnackbar(activity.snackbarContainer, getString(R.string.username_copied), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
            }
        }

        craetePartyButton.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("groupType", "party")
            bundle.putString("leader", user?.id)
            val intent = Intent(activity, GroupFormActivity::class.java)
            intent.putExtras(bundle)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityForResult(intent, GroupFormActivity.GROUP_FORM_ACTIVITY)
        }

        context?.let { context ->
            DataBindingUtils.loadImage("timeTravelersShop_background_fall") {bitmap ->
                val aspectRatio = bitmap.width / bitmap.height.toFloat()
                val height = context.resources.getDimension(R.dimen.shop_height).toInt()
                val width = Math.round(height * aspectRatio)
                val drawable = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(bitmap, width, height, false))
                drawable.tileModeX = Shader.TileMode.REPEAT
                if (drawable != null) {
                    Observable.just(drawable)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Consumer {
                                if (no_party_background != null) {
                                    no_party_background.background = it
                                }
                            }, RxErrorHandler.handleEmptyError())
                }
            }
        }

        groupDescriptionView.movementMethod = LinkMovementMethod.getInstance()
        groupSummaryView.movementMethod = LinkMovementMethod.getInstance()

        if (configManager.noPartyLinkPartyGuild()) {
            join_party_description_textview.text = MarkdownParser.parseMarkdown(getString(R.string.join_party_description_guild, "[Party Wanted Guild](https://habitica.com/groups/guild/f2db2a7f-13c5-454d-b3ee-ea1f5089e601)"))
            join_party_description_textview.setOnClickListener {
                context?.let { FirebaseAnalytics.getInstance(it).logEvent("clicked_party_wanted", null) }
                MainNavigationController.navigate(R.id.guildFragment, bundleOf("groupID" to "f2db2a7f-13c5-454d-b3ee-ea1f5089e601"))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            GroupFormActivity.GROUP_FORM_ACTIVITY -> {
                if (resultCode == Activity.RESULT_OK) {
                    val bundle = data?.extras
                    if (bundle?.getString("groupType") == "party") {
                        socialRepository.createGroup(bundle.getString("name"),
                                bundle.getString("description"),
                                bundle.getString("leader"),
                                "party",
                                bundle.getString("privacy"),
                                bundle.getBoolean("leaderCreateChallenge"))
                                .flatMap {
                                    userRepository.retrieveUser(false)
                                }
                                .subscribe(Consumer {
                                }, RxErrorHandler.handleEmptyError())
                    } else {
                        this.socialRepository.updateGroup(this.group,
                                bundle?.getString("name"),
                                bundle?.getString("description"),
                                bundle?.getString("leader"),
                                bundle?.getBoolean("leaderCreateChallenge"))
                                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                    }
                }
            }
        }
    }

    private fun refresh() {
        if (group != null) {
            compositeSubscription.add(socialRepository.retrieveGroup(group?.id ?: "").subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
        } else {
            compositeSubscription.add(userRepository.retrieveUser(false, forced = true)
                    .filter { it.hasParty() }
                    .flatMap { socialRepository.retrieveGroup("party") }
                    .flatMap<List<Member>> { group1 -> socialRepository.retrieveGroupMembers(group1.id, true) }
                    .doOnComplete { refreshLayout.isRefreshing = false }
                    .subscribe(Consumer {  }, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun setUser(user: User?) {
        if (group == null && user?.invitations?.party?.id != null) {
            setInvitation(user.invitations?.party)
        } else {
            setInvitation(null)
        }
        username_textview.text = user?.formattedUsername
    }

    private fun setInvitation(invitation: PartyInvite?) {
        invitationWrapper.visibility = if (invitation == null) View.GONE else View.VISIBLE
    }

    override fun onDestroy() {
        userRepository.close()
        socialRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun updateGroup(group: Group?) {
        if (noPartyWrapper == null) {
            return
        }

        val hasGroup = group != null
        val groupItemVisibility = if (hasGroup) View.VISIBLE else View.GONE
        noPartyWrapper.visibility = if (hasGroup) View.GONE else View.VISIBLE
        groupNameView.visibility = groupItemVisibility
        groupDescriptionView.visibility = groupItemVisibility
        groupDescriptionWrapper.visibility = groupItemVisibility

        groupNameView.text = group?.name
        groupDescriptionView.text = MarkdownParser.parseMarkdown(group?.description)
        groupSummaryView.text = MarkdownParser.parseMarkdown(group?.summary)
        gemCountWrapper.visibility = if (group?.balance != null && group.balance > 0) View.VISIBLE else View.GONE
        gemCountTextView.text = (group?.balance ?: 0 * 4.0).toInt().toString()
    }

    companion object {

        fun newInstance(group: Group?, user: User?): GroupInformationFragment {
            val args = Bundle()

            val fragment = GroupInformationFragment()
            fragment.arguments = args
            fragment.group = group
            fragment.user = user
            return fragment
        }
    }

}
