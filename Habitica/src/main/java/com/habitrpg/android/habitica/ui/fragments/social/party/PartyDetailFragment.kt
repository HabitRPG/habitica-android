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
import androidx.lifecycle.lifecycleScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentPartyDetailBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.HapticFeedbackManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemDialogFragment
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.viewHolders.GroupMemberViewHolder
import com.habitrpg.android.habitica.ui.viewmodels.PartyViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.extensions.DataBindingUtils
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.helpers.setMarkdown
import com.habitrpg.common.habitica.views.AvatarView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
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

        binding?.invitationsView?.getLeader = null

        binding?.invitationsView?.acceptCall = {
            viewModel?.joinGroup(it) {
                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                    val user = userRepository.retrieveUser(false)
                    parentFragmentManager.popBackStack()
                    MainNavigationController.navigate(
                        R.id.partyFragment,
                        bundleOf(Pair("partyID", user?.party?.id))
                    )
                }
            }
        }

        binding?.invitationsView?.rejectCall = {
            lifecycleScope.launchCatching {
                socialRepository.rejectGroupInvite(it)
                userRepository.retrieveUser(false, true)
            }
        }

        viewModel?.getGroupData()?.observe(viewLifecycleOwner) { updateParty(it) }
        viewModel?.user?.observe(viewLifecycleOwner) { updateUser(it) }
        viewModel?.getMembersData()?.observe(viewLifecycleOwner) { updateMembersList(it) }
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
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500)
                val content = inventoryRepository.getQuestContent(party.quest?.key ?: "").firstOrNull()
                if (content != null) {
                    updateQuestContent(content)
                }
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
                        lifecycleScope.launch(ExceptionHandler.coroutine()) {
                            val member = socialRepository.retrieveMember(id) ?: return@launch
                            binding?.root?.findViewById<AvatarView>(R.id.groupleader_avatar_view)
                                ?.setAvatar(member)
                            binding?.root?.findViewById<TextView>(R.id.groupleader_text_view)?.text =
                                getString(
                                    R.string.invitation_title,
                                    member.displayName,
                                    groupName
                                )
                        }
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
        binding?.questScrollImageView?.loadImage("inventory_quest_scroll_" + questContent.key)
        if (questContent.hasGifImage()) {
            binding?.questImageView?.loadImage("quest_" + questContent.key, "gif")
        } else {
            context?.let { context ->
                DataBindingUtils.loadImage(context, "quest_" + questContent.key) {
                    if (binding?.questImageView?.drawable?.constantState != it.constantState || binding?.questImageView?.drawable == null) {
                        binding?.questImageView?.setImageDrawable(it)
                    }
                    val params = binding?.questImageView?.layoutParams ?: return@loadImage
                    params.height = it.intrinsicHeight.dpToPx(context)
                    binding?.questImageView?.layoutParams = params
                }
            }
            binding?.questImageView?.loadImage("quest_" + questContent.key)
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

    private fun updateMembersList(members: List<Member>?) {
        val leaderID = viewModel?.leaderID
        members?.forEachIndexed { index, member ->
            val memberView = (
                if ((binding?.membersWrapper?.childCount ?: 0) > index) {
                    binding?.membersWrapper?.getChildAt(index)
                } else {
                    val view = binding?.membersWrapper?.inflate(R.layout.party_member, false)
                    binding?.membersWrapper?.addView(view)
                    view
                }
                ) ?: return@forEachIndexed
            val viewHolder = GroupMemberViewHolder(memberView)
            viewHolder.bind(member, leaderID ?: "", viewModel?.user?.value?.id)
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
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                socialRepository.postPrivateMessage(userID, emojiEditText.text.toString())
                (activity as? MainActivity)?.snackbarContainer?.let { it1 ->
                    HabiticaSnackbar.showSnackbar(
                        it1,
                        String.format(getString(R.string.profile_message_sent_to), username), HabiticaSnackbar.SnackbarDisplayType.NORMAL
                    )
                }
            }
            activity?.dismissKeyboard()
        }
        addMessageDialog?.addButton(android.R.string.cancel, false) { _, _ -> activity?.dismissKeyboard() }
        addMessageDialog?.setAdditionalContentView(newMessageView)
        addMessageDialog?.show()
    }

    private fun showTransferOwnerShipDialog(userID: String, displayName: String) {
        val dialog = context?.let { HabiticaAlertDialog(it) }
        dialog?.addButton(R.string.transfer, true) { _, _ ->
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                socialRepository.transferGroupOwnership(viewModel?.groupID ?: "", userID)
                (activity as? MainActivity)?.snackbarContainer?.let { it1 ->
                    HabiticaSnackbar.showSnackbar(
                        it1,
                        String.format(getString(R.string.transferred_ownership), displayName), HabiticaSnackbar.SnackbarDisplayType.NORMAL
                    )
                }
            }
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
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                socialRepository.removeMemberFromGroup(viewModel?.groupID ?: "", userID)
                (activity as? MainActivity)?.snackbarContainer?.let { it1 ->
                    HabiticaSnackbar.showSnackbar(
                        it1,
                        String.format(getString(R.string.removed_member), displayName), HabiticaSnackbar.SnackbarDisplayType.NORMAL
                    )
                }
            }
            activity?.dismissKeyboard()
        }
        dialog?.addButton(android.R.string.cancel, false) { _, _ -> activity?.dismissKeyboard() }
        dialog?.setTitle(context?.getString(R.string.remove_member_confirm, displayName))
        dialog?.show()
        dialog?.show()
    }

    private fun inviteNewQuest() {
        val fragment = ItemDialogFragment()
        fragment.itemType = "quests"
        fragment.itemTypeText = getString(R.string.quest)
        fragment.isModal = true
        fragment.show(parentFragmentManager, "questDialog")
    }

    private fun getGroupChallenges(): List<Challenge> {
        val groupChallenges = mutableListOf<Challenge>()
        lifecycleScope.launchCatching {
            userRepository.getUser().collect {
                it?.challenges?.forEach { membership ->
                    val challenge = challengeRepository.getChallenge(membership.challengeID).firstOrNull()
                    if (challenge != null && challenge.groupId == viewModel?.groupID) {
                        groupChallenges.add(challenge)
                    }
                }
            }
        }
        return groupChallenges
    }

    internal fun leaveParty() {
        val context = context
        if (context != null) {
            val groupChallenges = getGroupChallenges()
            lifecycleScope.launch(Dispatchers.Main) {
                delay(500)
                if (groupChallenges.isNotEmpty()) {
                    val alert = HabiticaAlertDialog(context)
                    alert.setTitle(R.string.party_challenges)
                    alert.setMessage(R.string.leave_party_challenges_confirmation)
                    alert.addButton(R.string.keep_challenges, true) { _, _ ->
                        viewModel?.leaveGroup(groupChallenges, true) {
                            parentFragmentManager.popBackStack()
                            MainNavigationController.navigate(R.id.noPartyFragment)
                        }
                    }
                    alert.addButton(R.string.leave_challenges_delete_tasks, false, isDestructive = true) { _, _ ->
                        viewModel?.leaveGroup(groupChallenges, false) {
                            parentFragmentManager.popBackStack()
                            MainNavigationController.navigate(R.id.noPartyFragment)
                        }
                    }
                    alert.setExtraCloseButtonVisibility(View.VISIBLE)
                    alert.show()
                } else {
                    val alert = HabiticaAlertDialog(context)
                    alert.setTitle(R.string.leave_party_confirmation)
                    alert.setMessage(R.string.rejoin_party)
                    alert.addButton(R.string.leave, isPrimary = true, isDestructive = true) { _, _ ->
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
        HapticFeedbackManager.tap(requireView())
        viewModel?.acceptQuest()
    }

    private fun onQuestReject() {
        HapticFeedbackManager.tap(requireView())
        viewModel?.rejectQuest()
    }

    private fun questDetailButtonClicked() {
        MainNavigationController.navigate(PartyFragmentDirections.openQuestDetail())
    }
}
