package com.habitrpg.android.habitica.ui.fragments.social.party

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.Observer
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.shared.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment
import com.habitrpg.android.habitica.ui.helpers.*
import com.habitrpg.android.habitica.ui.viewHolders.GroupMemberViewHolder.GroupMemberViewHolder
import com.habitrpg.android.habitica.ui.viewmodels.PartyViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.social.OldQuestProgressView
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import net.pherth.android.emoji_library.EmojiEditText
import javax.inject.Inject
import javax.inject.Named


class PartyDetailFragment : BaseFragment() {

    var viewModel: PartyViewModel? = null

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    private val refreshLayout: androidx.swiperefreshlayout.widget.SwipeRefreshLayout? by bindView(R.id.refreshLayout)
    private val partyInvitationWrapper: ViewGroup? by bindView(R.id.party_invitation_wrapper)
    private val partyAcceptButton: Button? by bindView(R.id.party_invite_accept_button)
    private val partyRejectButton: Button? by bindView(R.id.party_invite_reject_button)
    private val titleView: TextView? by bindView(R.id.title_view)
    private val descriptionView: TextView? by bindView(R.id.description_view)
    private val newQuestButton: Button? by bindView(R.id.new_quest_button)
    private val questDetailButton: ViewGroup? by bindView(R.id.quest_detail_button)
    private val questScrollImageView: SimpleDraweeView? by bindView(R.id.quest_scroll_image_view)
    private val questTitleView: TextView? by bindOptionalView(R.id.quest_title_view)
    private val questParticipationView: TextView? by bindOptionalView(R.id.quest_participation_view)
    private val questImageWrapper: ViewGroup? by bindView(R.id.quest_image_wrapper)
    private val questImageView: SimpleDraweeView? by bindView(R.id.quest_image_view)
    private val questParticipantResponseWrapper: ViewGroup? by bindView(R.id.quest_participant_response_wrapper)
    private val questAcceptButton: Button? by bindView(R.id.quest_accept_button)
    private val questRejectButton: Button? by bindView(R.id.quest_reject_button)
    private val questProgressView: OldQuestProgressView? by bindView(R.id.quest_progress_view)
    private val membersWrapper: LinearLayout? by bindView(R.id.members_wrapper)
    private val leaveButton: Button? by bindView(R.id.leave_button)

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_party_detail, container, false)
    }

    override fun onDestroyView() {
        inventoryRepository.close()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()

        refreshLayout?.setOnRefreshListener { this.refreshParty() }

        partyAcceptButton?.setOnClickListener { onPartyInviteAccepted() }
        partyRejectButton?.setOnClickListener { onPartyInviteRejected() }
        questAcceptButton?.setOnClickListener { onQuestAccept() }
        questRejectButton?.setOnClickListener { onQuestReject() }
        newQuestButton?.setOnClickListener { inviteNewQuest() }
        questDetailButton?.setOnClickListener { questDetailButtonClicked() }
        leaveButton?.setOnClickListener { leaveParty() }

        viewModel?.getGroupData()?.observe(viewLifecycleOwner, Observer { updateParty(it) })
        viewModel?.getUserData()?.observe(viewLifecycleOwner, Observer { updateUser(it) })
        viewModel?.getMembersData()?.observe(viewLifecycleOwner, Observer { updateMembersList(it) })
    }

    private fun refreshParty() {
        viewModel?.retrieveGroup {
            refreshLayout?.isRefreshing = false
        }
    }

    private fun updateParty(party: Group?) {
        if (party == null) {
            return
        }
        if (titleView == null) {
            return
        }
        titleView?.text = party.name
        descriptionView?.text = MarkdownParser.parseMarkdown(party.description)

        if (party.quest?.key?.isEmpty() == false) {
            newQuestButton?.visibility = View.GONE
            questDetailButton?.visibility = View.VISIBLE
            questImageWrapper?.visibility = View.VISIBLE
            val mainHandler = Handler(context?.mainLooper)
            mainHandler.postDelayed({
                inventoryRepository.getQuestContent(party.quest?.key ?: "")
                        .firstElement()
                        .subscribe(Consumer<QuestContent> { this@PartyDetailFragment.updateQuestContent(it) }, RxErrorHandler.handleEmptyError())
            }, 500)
        } else {
            newQuestButton?.visibility = View.VISIBLE
            questDetailButton?.visibility = View.GONE
            questImageWrapper?.visibility = View.GONE
            questProgressView?.visibility = View.GONE
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

        if (partyInvitationWrapper != null) {
            partyInvitationWrapper?.visibility = invitationVisibility
        }

        if (questParticipantResponseWrapper != null) {
            if (showParticipantButtons()) {
                questParticipantResponseWrapper?.visibility = View.VISIBLE
            } else {
                questParticipantResponseWrapper?.visibility = View.GONE
            }
        }

        questProgressView?.configure(user, viewModel?.isUserOnQuest)
    }

    private fun showParticipantButtons(): Boolean {
        return viewModel?.showParticipantButtons() ?: false
    }

    private fun updateQuestContent(questContent: QuestContent) {
        if (questTitleView == null || !questContent.isValid) {
            return
        }
        questTitleView?.text = questContent.text
        DataBindingUtils.loadImage(questScrollImageView, "inventory_quest_scroll_" + questContent.key)
        if (questContent.hasGifImage()) {
            DataBindingUtils.loadImage(questImageView, "quest_" + questContent.key, "gif")
        } else {
            DataBindingUtils.loadImage(questImageView, "quest_" + questContent.key)
        }
        if (viewModel?.isQuestActive == true) {
            questProgressView?.visibility = View.VISIBLE
            questProgressView?.setData(questContent, viewModel?.getGroupData()?.value?.quest?.progress)

            questParticipationView?.text = context?.getString(R.string.number_participants, viewModel?.getGroupData()?.value?.quest?.members?.size)
        } else {
            questProgressView?.visibility = View.GONE
        }
    }

    private fun updateMembersList(members: RealmResults<Member>?) {
        membersWrapper?.removeAllViews()
        val leaderID = viewModel?.leaderID
        if (members != null) {
            for (member in members) {
                val memberView = membersWrapper?.inflate(R.layout.party_member, false) ?: continue
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
                membersWrapper?.addView(memberView)
            }
        }
    }

    private fun showSendMessageToUserDialog(userID: String, username: String) {
        val factory = LayoutInflater.from(context)
        val newMessageView = factory.inflate(R.layout.profile_new_message_dialog, null)

        val emojiEditText = newMessageView.findViewById<EmojiEditText>(R.id.edit_new_message_text)

        val newMessageTitle = newMessageView.findViewById<TextView>(R.id.new_message_title)
        newMessageTitle.text = String.format(getString(R.string.profile_send_message_to), username)

        val addMessageDialog = context?.let { HabiticaAlertDialog(it) }
        addMessageDialog?.addButton(android.R.string.ok, true) { _, _ ->
            socialRepository.postPrivateMessage(userID, emojiEditText.text.toString())
                    .subscribe(Consumer {
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
                    .subscribe(Consumer {
                        (activity as? MainActivity)?.snackbarContainer?.let { it1 ->
                            HabiticaSnackbar.showSnackbar(it1,
                                    String.format(getString(R.string.transferred_ownership), displayName), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
                        }
                    }, RxErrorHandler.handleEmptyError())
            activity?.dismissKeyboard()
        }
        dialog?.addButton(android.R.string.cancel, false) { _, _ -> activity?.dismissKeyboard() }
        dialog?.setTitle(context?.getString(R.string.transfer_ownership_confirm, displayName))
        dialog?.show()
    }

    private fun showRemoveMemberDialog(userID: String, displayName: String) {
        val dialog = context?.let { HabiticaAlertDialog(it) }
        dialog?.addButton(R.string.remove, true) { _, _ ->
            socialRepository.removeMemberFromGroup(viewModel?.groupID ?: "", userID)
                    .subscribe(Consumer {
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
        fragmentManager?.let { fragment.show(it, "questDialog") }
    }

    internal fun leaveParty() {
        val context = context
        if (context != null) {
            val alert = HabiticaAlertDialog(context)
            alert.setMessage(R.string.leave_party_confirmation)
            alert.addButton(R.string.keep_challenges, true) { _, _ ->
                viewModel?.leaveGroup(true) { }
            }
            alert.addButton(R.string.leave_challenges, true) { _, _ ->
                viewModel?.leaveGroup(false) { }
            }
            alert.addButton(R.string.no, false)
            alert.show()
        }
    }

    private fun onQuestAccept() {
        viewModel?.acceptQuest()
    }


    private fun onQuestReject() {
        viewModel?.rejectQuest()
    }

    private fun onPartyInviteAccepted() {
        viewModel?.getUserData()?.value?.invitations?.party?.id?.let {
            viewModel?.joinGroup(it)
        }
    }

    private fun onPartyInviteRejected() {
        viewModel?.getUserData()?.value?.invitations?.party?.id?.let {
            viewModel?.rejectGroupInvite(it)
        }
    }

    private fun questDetailButtonClicked() {
        viewModel?.getGroupData()?.value?.let { party ->
            MainNavigationController.navigate(PartyFragmentDirections.openQuestDetail(party.id, party.quest?.key ?: ""))
        }
    }
}
