package com.habitrpg.android.habitica.ui.fragments.social

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
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import kotlinx.android.synthetic.main.fragment_group_info.*
import kotlinx.android.synthetic.main.qr_code.*
import rx.functions.Action1
import javax.inject.Inject

class GroupInformationFragment : BaseFragment() {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository

    private var group: Group? = null
    private var user: User? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_group_info, container, false)

        if (user != null) {
            setUser(user)
        } else {
            compositeSubscription.add(userRepository.user.subscribe(Action1 {
                user = it
                setUser(user)
            }, RxErrorHandler.handleEmptyError()))
        }

        if (group != null) {
            setGroup(group)
        }

        if (this.group == null) {
            val qrCodeManager = QrCodeManager(userRepository, this.context)
            qrCodeManager.setUpView(qrLayout)
        }

        buttonPartyInviteAccept.setOnClickListener({
            if (user != null) {
                socialRepository.joinGroup(user?.invitations?.party?.id)
                        .doOnNext { setInvitation(null) }
                        .flatMap { userRepository.retrieveUser(false) }
                        .flatMap { socialRepository.retrieveGroup("party") }
                        .flatMap<List<Member>> { group1 -> socialRepository.retrieveGroupMembers(group1.id, true) }
                        .subscribe(Action1 {  }, RxErrorHandler.handleEmptyError())
            }
        })

        buttonPartyInviteReject.setOnClickListener({
            if (user != null) {
                socialRepository.rejectGroupInvite(user?.invitations?.party?.id)
                        .subscribe(Action1 { setInvitation(null) }, RxErrorHandler.handleEmptyError())
            }
        })

        return view
    }

    private fun setUser(user: User?) {
        if (group == null && user != null && user.invitations != null && user.invitations.party.id != null) {
            setInvitation(user.invitations.party)
        } else {
            setInvitation(null)
        }
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

    fun setGroup(group: Group?) {
        this.group = group

        val hasGroup = group != null
        val groupItemVisibility = if (hasGroup) View.VISIBLE else View.GONE
        qrWrapper.visibility = if (hasGroup) View.GONE else View.VISIBLE
        groupNameView.visibility = groupItemVisibility
        groupDescriptionView.visibility = groupItemVisibility

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
