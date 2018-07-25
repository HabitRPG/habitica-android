package com.habitrpg.android.habitica.ui.fragments.social.party

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment
import com.habitrpg.android.habitica.ui.fragments.social.QuestDetailFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.social.OldQuestProgressView
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Named


class PartyDetailFragment : BaseFragment() {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    private val refreshLayout: SwipeRefreshLayout? by bindView(R.id.refreshLayout)
    private val partyInvitationWrapper: ViewGroup? by bindView(R.id.party_invitation_wrapper)
    private val titleView: TextView? by bindView(R.id.title_view)
    private val descriptionView: TextView? by bindView(R.id.description_view)
    private val newQuestButton: Button? by bindView(R.id.new_quest_button)
    private val questDetailButton: ViewGroup? by bindView(R.id.quest_detail_button)
    private val questScrollImageView: SimpleDraweeView? by bindView(R.id.quest_scroll_image_view)
    private val questTitleView: TextView? by bindView(R.id.quest_title_view)
    private val questParticipationView: TextView? by bindView(R.id.quest_participation_view)
    private val questImageWrapper: ViewGroup? by bindView(R.id.quest_image_wrapper)
    private val questImageView: SimpleDraweeView? by bindView(R.id.quest_image_view)
    private val questParticipantResponseWrapper: ViewGroup? by bindView(R.id.quest_participant_response_wrapper)
    private val questAcceptButton: Button? by bindView(R.id.quest_accept_button)
    private val questRejectButton: Button? by bindView(R.id.quest_reject_button)
    private val questProgressView: OldQuestProgressView? by bindView(R.id.quest_progress_view)
    private val questParticipantList: LinearLayout? by bindView(R.id.quest_participant_list)
    private val leaveButton: Button? by bindView(R.id.leave_button)


    var partyId: String? = null
    private var party: Group? = null
    private var quest: Quest? = null
    private var user: User? = null

    private val isQuestActive: Boolean
        get() = quest?.active == true

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_party_detail, container, false)
    }

    override fun onDestroyView() {
        socialRepository.close()
        userRepository.close()
        inventoryRepository.close()
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshLayout?.setOnRefreshListener { this.refreshParty() }

        compositeSubscription.add(socialRepository.getGroup(partyId).subscribe(Consumer { this.updateParty(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(userRepository.getUser(userId).subscribe(Consumer { this.updateUser(it) }, RxErrorHandler.handleEmptyError()))

        questAcceptButton?.setOnClickListener { onQuestAccept() }
        questRejectButton?.setOnClickListener { onQuestReject() }
        newQuestButton?.setOnClickListener { inviteNewQuest() }
        questDetailButton?.setOnClickListener { questDetailButtonClicked() }
        leaveButton?.setOnClickListener { leaveParty() }
    }

    private fun refreshParty() {
        socialRepository.retrieveGroup("party")
                .flatMap { group1 -> socialRepository.retrieveGroupMembers(group1.id, true) }
                .doOnComplete { refreshLayout?.isRefreshing = false }
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
    }

    private fun updateParty(party: Group?) {
        if (party == null) {
            return
        }
        this.party = party
        this.quest = party.quest
        if (titleView == null) {
            return
        }
        titleView?.text = party.name
        descriptionView?.text = MarkdownParser.parseMarkdown(party.description)

        if (quest?.key?.isEmpty() == false) {
            newQuestButton?.visibility = View.GONE
            questDetailButton?.visibility = View.VISIBLE
            questImageWrapper?.visibility = View.VISIBLE
            val mainHandler = Handler(context?.mainLooper)
            mainHandler.postDelayed({
                inventoryRepository.getQuestContent(quest?.key ?: "")
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
        this.user = user

        var invitationVisibility = View.GONE
        if (user.invitations?.party?.id != null) {
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

        questProgressView?.configure(user)
    }

    private fun showParticipantButtons(): Boolean {
        return !(user == null || user?.party == null || user?.party?.quest == null) && !isQuestActive && user?.party?.quest?.RSVPNeeded == true
    }

    private fun updateQuestContent(questContent: QuestContent) {
        if (questTitleView == null || !questContent.isValid) {
            return
        }
        questTitleView?.text = questContent.text
        DataBindingUtils.loadImage(questScrollImageView, "inventory_quest_scroll_" + questContent.key)
        DataBindingUtils.loadImage(questImageView, "quest_" + questContent.key)
        if (isQuestActive) {
            questProgressView?.visibility = View.VISIBLE
            questProgressView?.setData(questContent, quest?.progress)

            questParticipationView?.text = getString(R.string.number_participants, quest?.members?.size)
        } else {
            questProgressView?.visibility = View.GONE
        }
    }

    private fun inviteNewQuest() {
        val fragment = ItemRecyclerFragment()
        fragment.itemType = "quests"
        fragment.itemTypeText = getString(R.string.quest)
        fragment.show(fragmentManager, "questDialog")
    }

    private fun leaveParty() {
        val builder = AlertDialog.Builder(activity)
                .setMessage(R.string.leave_party_confirmation)
                .setPositiveButton(R.string.yes) { _, _ ->
                    socialRepository.leaveGroup(partyId)
                            .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                }.setNegativeButton(R.string.no) { _, _ -> }
        builder.show()
    }

    private fun onQuestAccept() {
        partyId.notNull {
            socialRepository.acceptQuest(user, it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
        }
    }


    private fun onQuestReject() {
        partyId.notNull {
            socialRepository.rejectQuest(user, it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
        }
    }

    fun onPartyInviteAccepted() {
        user?.invitations?.party?.id.notNull {
            socialRepository.joinGroup(it)
                    .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
        }
    }

    fun onPartyInviteRejected() {
        user?.invitations?.party?.id.notNull {
            socialRepository.rejectGroupInvite(it)
                    .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
        }
    }

    fun questDetailButtonClicked() {
        val fragment = QuestDetailFragment()
        fragment.partyId = partyId
        if (party != null && party?.quest != null) {
            fragment.questKey = party?.quest?.key
        }
        if (activity != null) {
            val activity = activity as? MainActivity
            activity?.displayFragment(fragment)
        }
    }
}
