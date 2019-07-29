package com.habitrpg.android.habitica.ui.fragments.social

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.MarkdownParser
import com.habitrpg.android.habitica.ui.helpers.bindOptionalView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.functions.Consumer
import javax.inject.Inject
import javax.inject.Named

class QuestDetailFragment : BaseMainFragment() {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    private val questTitleView: TextView? by bindOptionalView(R.id.title_view)
    private val questScrollImageView: SimpleDraweeView? by bindOptionalView(R.id.quest_scroll_image_view)
    private val questLeaderView: TextView? by bindOptionalView(R.id.quest_leader_view)
    private val questDescriptionView: TextView? by bindOptionalView(R.id.description_view)
    private val questParticipantList: LinearLayout? by bindOptionalView(R.id.quest_participant_list)
    private val participantHeader: TextView? by bindOptionalView(R.id.participants_header)
    private val participantHeaderCount: TextView? by bindOptionalView(R.id.participants_header_count)
    private val questParticipantResponseWrapper: ViewGroup? by bindOptionalView(R.id.quest_participant_response_wrapper)
    private val questLeaderResponseWrapper: ViewGroup? by bindOptionalView(R.id.quest_leader_response_wrapper)
    private val questAcceptButton: Button? by bindOptionalView(R.id.quest_accept_button)
    private val questRejectButton: Button? by bindOptionalView(R.id.quest_reject_button)
    private val questBeginButton: Button? by bindOptionalView(R.id.quest_begin_button)
    private val questCancelButton: Button? by bindOptionalView(R.id.quest_cancel_button)
    private val questAbortButton: Button? by bindOptionalView(R.id.quest_abort_button)

    var partyId: String? = null
    var questKey: String? = null
    private var party: Group? = null
    private var quest: Quest? = null
    private var beginQuestMessage: String? = null

    private val isQuestActive: Boolean
        get() = quest?.active == true


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        this.hidesToolbar = true
        return inflater.inflate(R.layout.fragment_quest_detail, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = QuestDetailFragmentArgs.fromBundle(it)
            partyId = args.partyID
            questKey = args.questKey
        }

        resetViews()

        questAcceptButton?.setOnClickListener { onQuestAccept() }
        questRejectButton?.setOnClickListener { onQuestReject() }
        questBeginButton?.setOnClickListener { onQuestBegin() }
        questCancelButton?.setOnClickListener { onQuestCancel() }
        questAbortButton?.setOnClickListener { onQuestAbort() }
    }

    override fun onResume() {
        super.onResume()
        compositeSubscription.add(socialRepository.getGroup(partyId)
                .subscribe(Consumer { this.updateParty(it) }, RxErrorHandler.handleEmptyError()))
        if (questKey != null) {
            compositeSubscription.add(inventoryRepository.getQuestContent(questKey ?: "")
                    .subscribe(Consumer { this.updateQuestContent(it) }, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun updateParty(group: Group?) {
        if (questTitleView == null || group == null || group.quest == null) {
            return
        }
        party = group
        quest = group.quest
        setQuestParticipants(group.quest?.participants)
        compositeSubscription.add(socialRepository.getMember(quest?.leader).firstElement().subscribe(Consumer { member ->
            if (context != null && questLeaderView != null && member != null) {
                questLeaderView?.text = context?.getString(R.string.quest_leader_header, member.displayName)
            }
        }, RxErrorHandler.handleEmptyError()))

        if (questLeaderResponseWrapper != null) {
            if (showParticipatantButtons()) {
                questLeaderResponseWrapper?.visibility = View.GONE
                questParticipantResponseWrapper?.visibility = View.VISIBLE
            } else if (showLeaderButtons()) {
                questParticipantResponseWrapper?.visibility = View.GONE
                questLeaderResponseWrapper?.visibility = View.VISIBLE
                if (isQuestActive) {
                    questBeginButton?.visibility = View.GONE
                    questCancelButton?.visibility = View.GONE
                    questAbortButton?.visibility = View.VISIBLE
                } else {
                    questBeginButton?.visibility = View.VISIBLE
                    questCancelButton?.visibility = View.VISIBLE
                    questAbortButton?.visibility = View.GONE
                }
            } else {
                questLeaderResponseWrapper?.visibility = View.GONE
                questParticipantResponseWrapper?.visibility = View.GONE
            }
        }
    }

    private fun showLeaderButtons(): Boolean {
        return userId == party?.quest?.leader || userId == party?.leaderID
    }

    private fun showParticipatantButtons(): Boolean {
        return if (user == null || user?.party == null || user?.party?.quest == null) {
            false
        } else !isQuestActive && user?.party?.quest?.RSVPNeeded == true
    }


    private fun updateQuestContent(questContent: QuestContent) {
        if (questTitleView == null || !questContent.isManaged) {
            return
        }
        questTitleView?.text = questContent.text
        //TODO: FIX
        questDescriptionView?.text = MarkdownParser.parseMarkdown(questContent.notes)
        DataBindingUtils.loadImage(questScrollImageView, "inventory_quest_scroll_" + questContent.key)
    }

    private fun setQuestParticipants(participants: List<Member>?) {
        if (questParticipantList == null) {
            return
        }
        questParticipantList?.removeAllViews()
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        var participantCount = 0
        for (participant in participants ?: emptyList()) {
            if (quest?.active == true && participant.participatesInQuest == false) {
                continue
            }
            val participantView = inflater?.inflate(R.layout.quest_participant, questParticipantList, false)
            val textView = participantView?.findViewById<View>(R.id.participant_name) as? TextView
            textView?.text = participant.displayName
            val statusTextView = participantView?.findViewById<View>(R.id.status_view) as? TextView
            if (quest?.active == false) {
                context?.let {
                    when {
                        participant.participatesInQuest == null -> {
                            statusTextView?.setText(R.string.pending)
                            statusTextView?.setTextColor(ContextCompat.getColor(it, R.color.gray_200))
                        }
                        participant.participatesInQuest == true -> {
                            statusTextView?.setText(R.string.accepted)
                            statusTextView?.setTextColor(ContextCompat.getColor(it, R.color.green_100))
                        }
                        else -> {
                            statusTextView?.setText(R.string.declined)
                            statusTextView?.setTextColor(ContextCompat.getColor(it, R.color.red_100))
                        }
                    }
                }
                questParticipantList?.addView(participantView)
            } else {
                statusTextView?.visibility = View.GONE
                if (participant.participatesInQuest == true) {
                    questParticipantList?.addView(participantView)
                }
            }
            if (quest?.active == true || participant.participatesInQuest == true) {
                participantCount += 1
            }
        }
        if (quest?.active == true) {
            participantHeader?.setText(R.string.participants)
            participantHeaderCount?.text = participantCount.toString()
        } else {
            participantHeader?.setText(R.string.invitations)
            participantHeaderCount?.text = participantCount.toString() + "/" + quest?.participants?.size
            beginQuestMessage = getString(R.string.quest_begin_message, participantCount, quest?.participants?.size)
        }
    }

    override fun onDestroyView() {
        socialRepository.close()
        userRepository.close()
        inventoryRepository.close()
        super.onDestroyView()
    }

    private fun onQuestAccept() {
        partyId?.let { partyID ->
        socialRepository.acceptQuest(user, partyID).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
        }
    }


    private fun onQuestReject() {
        partyId?.let { partyID ->
            socialRepository.rejectQuest(user, partyID).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
        }
    }

    private fun onQuestBegin() {
        val context = context
        if (context != null) {
            val alert = HabiticaAlertDialog(context)
            alert.setMessage(beginQuestMessage)
            alert.addButton(R.string.yes, true) { _, _ ->
                val party = party
                if (party != null) {
                    socialRepository.forceStartQuest(party)
                            .subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
                }
            }
            alert.addButton(R.string.no, false)
            alert.show()
        }
    }

    private fun onQuestCancel() {
        context?.let {
            val alert = HabiticaAlertDialog(it)
            alert.setMessage(R.string.quest_cancel_message)
            alert.addButton(R.string.yes, true) { _, _ ->
                partyId?.let { partyID ->
                    @Suppress("DEPRECATION")
                    socialRepository.cancelQuest(partyID)
                            .subscribe(Consumer { getActivity()?.fragmentManager?.popBackStack() }, RxErrorHandler.handleEmptyError())
                }
            }
            alert.addButton(R.string.no, false)
            alert.show()
        }
    }

    private fun onQuestAbort() {
        val builder = AlertDialog.Builder(getActivity())
                .setMessage(R.string.quest_abort_message)
                .setPositiveButton(R.string.yes) { _, _ ->
                    partyId?.let { partyID ->
                        @Suppress("DEPRECATION")
                        socialRepository.abortQuest(partyID)
                                .subscribe(Consumer { getActivity()?.fragmentManager?.popBackStack() }, RxErrorHandler.handleEmptyError())
                    }
                }.setNegativeButton(R.string.no) { _, _ -> }
        builder.show()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }
}
