package com.habitrpg.android.habitica.ui.fragments.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.QrCodeManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.invitations.PartyInvite
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_group_info.*
import javax.inject.Inject


class GroupInformationFragment : BaseFragment() {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository

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

        if (user != null) {
            setUser(user)
        } else {
            compositeSubscription.add(userRepository.getUser().subscribe(Consumer {
                user = it
                setUser(user)
            }, RxErrorHandler.handleEmptyError()))
        }

        updateGroup(group)

        if (this.group == null) {
            val qrCodeManager = QrCodeManager(userRepository, this.context)
            qrCodeManager.setUpView(qrLayout)
        }

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

        userIdView.setOnClickListener {
            val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText(context?.getString(R.string.user_id), user?.id)
            clipboard?.primaryClip = clip
            val activity = activity as? MainActivity
            if (activity != null) {
                HabiticaSnackbar.showSnackbar(activity.floatingMenuWrapper, getString(R.string.id_copied), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
            }
        }

        craetePartyButton.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://habitica.com/party"))
            startActivity(browserIntent)
        }
    }

    private fun setUser(user: User?) {
        if (group == null && user?.invitations?.party?.id != null) {
            setInvitation(user.invitations?.party)
        } else {
            setInvitation(null)
        }
        userIdView.text = user?.id
    }

    private fun setInvitation(invitation: PartyInvite?) {
        invitationWrapper.visibility = if (invitation == null) View.GONE else View.VISIBLE
    }

    override fun onDestroy() {
        userRepository.close()
        socialRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: AppComponent) {
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

        groupDescriptionView.text = group?.description
        leadernameWrapper.visibility = if (group?.leaderName != null) View.VISIBLE else View.GONE
        leadernameTextView.text = group?.leaderName
        leaderMessageWrapper.visibility = if (group?.leaderMessage != null) View.VISIBLE else View.GONE
        leaderMessageTextView.text = group?.leaderMessage
        leadernameWrapper.visibility = if (group?.balance != null && group.balance > 0) View.VISIBLE else View.GONE
        leadernameTextView.text = (group?.balance ?: 0 * 4.0).toString()
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
