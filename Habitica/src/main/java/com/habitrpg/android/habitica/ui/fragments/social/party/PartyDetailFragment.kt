package com.habitrpg.android.habitica.ui.fragments.social.party

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentPartyDetailBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.helpers.setMarkdown
import com.habitrpg.android.habitica.ui.viewHolders.GroupMemberViewHolder
import com.habitrpg.android.habitica.ui.viewmodels.PartyViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.realm.RealmResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class PartyDetailFragment : BaseFragment<FragmentPartyDetailBinding>() {

    var viewModel: PartyViewModel? = null

    override var binding: FragmentPartyDetailBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentPartyDetailBinding {
        return FragmentPartyDetailBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var challengeRepository: ChallengeRepository

    @Inject
    lateinit var socialRepository: SocialRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onDestroyView() {
        inventoryRepository.close()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding?.refreshLayout?.setOnRefreshListener { this.refreshParty() }

        binding?.questAcceptButton?.setOnClickListener { onQuestAccept() }
        binding?.questRejectButton?.setOnClickListener { onQuestReject() }
        binding?.newQuestButton?.setOnClickListener { inviteNewQuest() }
        binding?.questDetailButton?.setOnClickListener { questDetailButtonClicked() }
        binding?.leaveButton?.setOnClickListener { leaveParty() }

        binding?.invitationsView?.setLeader = null

        binding?.invitationsView?.acceptCall = {
            viewModel?.joinGroup(it) {
                compositeSubscription.add(userRepository.retrieveUser(false)
                        .subscribe { user ->
                            parentFragmentManager.popBackStack()
                            MainNavigationController.navigate(R.id.partyFragment,
                                    bundleOf(Pair("partyID", user.party?.id)))

                        })
            }
        }

        binding?.invitationsView?.rejectCall = {
            socialRepository.rejectGroupInvite(it)
                    .flatMap { userRepository.retrieveUser(false, true) }
                    .subscribe({ }, RxErrorHandler.handleEmptyError())
        }

        viewModel?.getGroupData()?.observe(viewLifecycleOwner, { updateParty(it) })
        viewModel?.getUserData()?.observe(viewLifecycleOwner, { updateUser(it) })
        viewModel?.getMembersData()?.observe(viewLifecycleOwner, { updateMembersList(it) })
    }

    private fun refreshParty() {
        viewModel?.retrieveGroup {
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    private fun updateParty(party: Group?) {
        if (party == null) {
            return
        }
        if (binding?.titleView == null) {
            return
        }
        binding?.titleView?.text = party.name
        binding?.descriptionView?.setMarkdown(party.description)

        if (party.quest?.key?.isEmpty() == false) {
            binding?.newQuestButton?.visibility = View.GONE
            binding?.questDetailButton?.visibility = View.VISIBLE
            binding?.questImageWrapper?.visibility = View.VISIBLE
            GlobalScope.launch(Dispatchers.Main) {
                delay(500)
                inventoryRepository.getQuestContent(party.quest?.key ?: "")
                        .subscribe({ this@PartyDetailFragment.updateQuestContent(it) }, RxErrorHandler.handleEmptyError())
            }
        } else {
            binding?.newQuestButton?.visibility = View.VISIBLE
            binding?.questDetailButton?.visibility = View.GONE
            binding?.questImageWrapper?.visibility = View.GONE
            binding?.questProgressView?.visibility = View.GONE
        }
    }

    private fun updateUser(user: User?) {
        if (user?.party?.quest == null) {
            return
        }

        var invitationVisibility = View.GONE
        if (user.invitations?.party?.id?.isNotEmpty() == true) {
            invitationVisibility = View.VISIBLE
        }

        if (binding?.partyInvitationWrapper != null) {
            binding?.partyInvitationWrapper?.visibility = invitationVisibility
        }

        if (binding?.questParticipantResponseWrapper != null) {
            if (showParticipantButtons()) {
                binding?.questParticipantResponseWrapper?.visibility = View.VISIBLE
            } else {
                binding?.questParticipantResponseWrapper?.visibility = View.GONE
            }
        }

        binding?.questProgressView?.configure(user, viewModel?.isUserOnQuest)

        if ((user.invitations?.parties?.count() ?: 0) > 0) {
            binding?.partyInvitationWrapper?.visibility = View.VISIBLE
            user.invitations?.parties?.let {
                for (invitation in it) {
                    val leaderID = invitation.inviter
                    val groupName = invitation.name

                    leaderID.let { id ->
                        compositeSubscription.add(
                                socialRepository.getMember(id)
                                        .subscribe({ member ->
                                            binding?.root?.findViewById<AvatarView>(R.id.groupleader_avatar_view)?.setAvatar(member)
                                            binding?.root?.findViewById<TextView>(R.id.groupleader_text_view)?.text = getString(R.string.invitation_title, member.displayName, groupName)
                                        }, RxErrorHandler.handleEmptyError())
                        )
                    }

                    view?.findViewById<Button>(R.id.accept_button)?.setOnClickListener {
                        invitation.id?.let { it1 -> binding?.invitationsView?.acceptCall?.invoke(it1) }
                    }

                    view?.findViewById<Button>(R.id.reject_button)?.setOnClickListener {
                        invitation.id?.let { it1 -> binding?.invitationsView?.rejectCall?.invoke(it1) }
                    }
                }
            }
        } else {
            binding?.partyInvitationWrapper?.visibility = View.GONE
        }
    }

    private fun showParticipantButtons(): Boolean {
        return viewModel?.showParticipantButtons() ?: false
    }

    private fun updateQuestContent(questContent: QuestContent) {
        if (binding?.questTitleView == null || !questContent.isValid) {
            return
        }
        binding?.questTitleView?.text = questContent.text
        DataBindingUtils.loadImage(binding?.questScrollImageView, "inventory_quest_scroll_" + questContent.key)
        if (questContent.hasGifImage()) {
            DataBindingUtils.loadImage(binding?.questImageView, "quest_" + questContent.key, "gif")
        } else {
            DataBindingUtils.loadImage(binding?.questImageView, "quest_" + questContent.key)
        }
        binding?.questImageWrapper?.alpha = 1.0f
        binding?.questProgressView?.alpha = 1.0f
        context?.let { binding?.questParticipationView?.setTextColor(ContextCompat.getColor(it, R.color.text_quad)) }
        if (viewModel?.isQuestActive == true) {
            binding?.questProgressView?.visibility = View.VISIBLE
            binding?.questProgressView?.setData(questContent, viewModel?.getGroupData()?.value?.quest?.progress)

            val questParticipants = viewModel?.getGroupData()?.value?.quest?.members
            if (questParticipants?.find { it.key == userId } != null) {
                binding?.questParticipationView?.text = context?.getString(R.string.number_participants, questParticipants.size)
            } else {
                binding?.questParticipationView?.text = context?.getString(R.string.not_participating)
                context?.let { binding?.questParticipationView?.setTextColor(ContextCompat.getColor(it, R.color.red_10)) }
                binding?.questImageWrapper?.alpha = 0.5f
                binding?.questProgressView?.alpha = 0.5f
            }
        } else {
            binding?.questProgressView?.visibility = View.GONE
            val members = viewModel?.getGroupData()?.value?.quest?.members
            val responded = members?.filter { it.isParticipating != null }
            binding?.questParticipationView?.text = context?.getString(R.string.number_responded, responded?.size, members?.size)
        }
    }

    private fun updateMembersList(members: RealmResults<Member>?) {
        val leaderID = viewModel?.leaderID
        members?.forEachIndexed { index, member ->
            val memberView = (if (binding?.membersWrapper?.childCount ?: 0 > index) {
                binding?.membersWrapper?.getChildAt(index)
            } else {
                val view = binding?.membersWrapper?.inflate(R.layout.party_member, false)
                binding?.membersWrapper?.addView(view)
                view
            }) ?: return@forEachIndexed
            val viewHolder = GroupMemberViewHolder(memberView)
            viewHolder.bind(member, leaderID ?: "", viewModel?.getUserData()?.value?.id)
            viewHolder.onClickEvent = {
                FullProfileActivity.open(member.id ?: "")
            }
            viewHolder.sendMessageEvent = {
                member.id?.let { showSendMessageToUserDialog(it, member.displayName) }
            }
            viewHolder.transferOwnershipEvent = {
                member.id?.let { showTransferOwnerShipDialog(it, member.displayName) }
            }
            viewHolder.removeMemberEvent = {
                member.id?.let { showRemoveMemberDialog(it, member.displayName) }
            }
        }
    }

    private fun showSendMessageToUserDialog(userID: String, username: String) {
        val factory = LayoutInflater.from(context)
        val newMessageView = factory.inflate(R.layout.profile_new_message_dialog, null)

        val emojiEditText = newMessageView.findViewById<AppCompatEditText>(R.id.edit_new_message_text)

        val newMessageTitle = newMessageView.findViewById<TextView>(R.id.new_message_title)
        newMessageTitle.text = String.format(getString(R.string.profile_send_message_to), username)

        val addMessageDialog = context?.let { HabiticaAlertDialog(it) }
        addMessageDialog?.addButton(android.R.string.ok, true) { _, _ ->
            socialRepository.postPrivateMessage(userID, emojiEditText.text.toString())
                    .subscribe({
                        (activity as? MainActivity)?.snackbarContainer?.let { it1 ->
                            HabiticaSnackbar.showSnackbar(it1,
                                    String.format(getString(R.string.profile_message_sent_to), username), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
                        }
                    }, RxErrorHandler.handleEmptyError())
            activity?.dismissKeyboard()
        }
        addMessageDialog?.addButton(android.R.string.cancel, false) { _, _ -> activity?.dismissKeyboard() }
        addMessageDialog?.setAdditionalContentView(newMessageView)
        addMessageDialog?.show()
    }

    private fun showTransferOwnerShipDialog(userID: String, displayName: String) {
        val dialog = context?.let { HabiticaAlertDialog(it) }
        dialog?.addButton(R.string.transfer, true) { _, _ ->
            socialRepository.transferGroupOwnership(viewModel?.groupID ?: "", userID)
                    .subscribe({
                        (activity as? MainActivity)?.snackbarContainer?.let { it1 ->
                            HabiticaSnackbar.showSnackbar(it1,
                                    String.format(getString(R.string.transferred_ownership), displayName), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
                        }
                    }, RxErrorHandler.handleEmptyError())
            activity?.dismissKeyboard()
        }
        dialog?.addButton(android.R.string.cancel, false) { _, _ -> activity?.dismissKeyboard() }
        dialog?.setTitle(context?.getString(R.string.transfer_ownership_confirm))
        dialog?.setMessage(context?.getString(R.string.transfer_ownership_confirm_message, displayName))
        dialog?.show()
    }

    private fun showRemoveMemberDialog(userID: String, displayName: String) {
        val dialog = context?.let { HabiticaAlertDialog(it) }
        dialog?.addButton(R.string.remove, true) { _, _ ->
            socialRepository.removeMemberFromGroup(viewModel?.groupID ?: "", userID)
                    .subscribe({
                        (activity as? MainActivity)?.snackbarContainer?.let { it1 ->
                            HabiticaSnackbar.showSnackbar(it1,
                                    String.format(getString(R.string.removed_member), displayName), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
                        }
                    }, RxErrorHandler.handleEmptyError())
            activity?.dismissKeyboard()
        }
        dialog?.addButton(android.R.string.cancel, false) { _, _ -> activity?.dismissKeyboard() }
        dialog?.setTitle(context?.getString(R.string.remove_member_confirm, displayName))
        dialog?.show()
    }

    private fun inviteNewQuest() {
        val fragment = ItemRecyclerFragment()
        fragment.itemType = "quests"
        fragment.itemTypeText = getString(R.string.quest)
        fragment.isModal = true
        fragment.show(parentFragmentManager, "questDialog")
    }

    private fun getGroupChallenges(): List<Challenge> {
        var groupChallenges = mutableListOf<Challenge>()
        userRepository.getUser(userId).forEach {
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

    internal fun leaveParty() {
        val context = context
        if (context != null) {
            var groupChallenges = getGroupChallenges()
            GlobalScope.launch(Dispatchers.Main) {
                delay(500)
                if (groupChallenges.isNotEmpty()) {
                    val alert = HabiticaAlertDialog(context)
                    alert.setTitle(R.string.party_challenges)
                    alert.setMessage(R.string.leave_party_challenges_confirmation)
                    alert.addButton(R.string.keep_challenges, true) { _, _ ->
                        viewModel?.leaveGroup(groupChallenges,true) {
                            parentFragmentManager.popBackStack()
                            MainNavigationController.navigate(R.id.noPartyFragment)
                        }
                    }
                    alert.addButton(R.string.leave_group_challenges, false, isDestructive = true) { _, _ ->
                        viewModel?.leaveGroup(groupChallenges,false) {
                            parentFragmentManager.popBackStack()
                            MainNavigationController.navigate(R.id.noPartyFragment)
                        }
                    }
                    alert.setExtraCloseButtonVisibility(View.VISIBLE)
                    alert.show()
                } else {
                    val alert = HabiticaAlertDialog(context)
                    alert.setTitle("Are you sure you want to leave the party?")
                    alert.setMessage("You will not be able to rejoin this party unless invited.")
                    alert.addButton("Leave", isPrimary = true, isDestructive = true) { _, _ ->
                        viewModel?.leaveGroup(groupChallenges, false) {
                            parentFragmentManager.popBackStack()
                            MainNavigationController.navigate(R.id.noPartyFragment)
                        }
                    }
                    alert.setExtraCloseButtonVisibility(View.VISIBLE)
                    alert.show()
                }
            }
        }
    }

    private fun onQuestAccept() {
        viewModel?.acceptQuest()
    }


    private fun onQuestReject() {
        viewModel?.rejectQuest()
    }

    private fun questDetailButtonClicked() {
        viewModel?.getGroupData()?.value?.let { party ->
            MainNavigationController.navigate(PartyFragmentDirections.openQuestDetail())
        }
    }
}
