package com.habitrpg.android.habitica.ui.fragments.social

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentQuestDetailBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.setMarkdown
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import javax.inject.Inject
import javax.inject.Named

class QuestDetailFragment : BaseMainFragment<FragmentQuestDetailBinding>() {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    override var binding: FragmentQuestDetailBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentQuestDetailBinding {
        return FragmentQuestDetailBinding.inflate(inflater, container, false)
    }

    var partyId: String? = null
    var questKey: String? = null
    private var party: Group? = null
    private var quest: Quest? = null
    private var beginQuestMessage: String? = null

    private val isQuestActive: Boolean
        get() = quest?.active == true


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = QuestDetailFragmentArgs.fromBundle(it)
            partyId = args.partyID
            questKey = args.questKey
        }

        binding?.questAcceptButton?.setOnClickListener { onQuestAccept() }
        binding?.questRejectButton?.setOnClickListener { onQuestReject() }
        binding?.questBeginButton?.setOnClickListener { onQuestBegin() }
        binding?.questCancelButton?.setOnClickListener { onQuestCancel() }
        binding?.questAbortButton?.setOnClickListener { onQuestAbort() }
    }

    override fun onResume() {
        super.onResume()
        compositeSubscription.add(socialRepository.getGroup(partyId)
                .subscribe({ this.updateParty(it) }, RxErrorHandler.handleEmptyError()))
        if (questKey != null) {
            compositeSubscription.add(inventoryRepository.getQuestContent(questKey ?: "")
                    .subscribe({ this.updateQuestContent(it) }, RxErrorHandler.handleEmptyError()))
        }
    }

    private fun updateParty(group: Group?) {
        if (binding?.titleView == null || group == null || group.quest == null) {
            return
        }
        party = group
        quest = group.quest
        setQuestParticipants(group.quest?.participants)
        compositeSubscription.add(socialRepository.getMember(quest?.leader).subscribe({ member ->
            if (context != null && binding?.questLeaderView != null && member != null) {
                binding?.questLeaderView?.text = context?.getString(R.string.quest_leader_header, member.displayName)
            }
        }, RxErrorHandler.handleEmptyError()))

        if (binding?.questLeaderResponseWrapper != null) {
            if (showParticipatantButtons()) {
                binding?.questLeaderResponseWrapper?.visibility = View.GONE
                binding?.questParticipantResponseWrapper?.visibility = View.VISIBLE
            } else if (showLeaderButtons()) {
                binding?.questParticipantResponseWrapper?.visibility = View.GONE
                binding?.questLeaderResponseWrapper?.visibility = View.VISIBLE
                if (isQuestActive) {
                    binding?.questBeginButton?.visibility = View.GONE
                    binding?.questCancelButton?.visibility = View.GONE
                    binding?.questAbortButton?.visibility = View.VISIBLE
                } else {
                    binding?.questBeginButton?.visibility = View.VISIBLE
                    binding?.questCancelButton?.visibility = View.VISIBLE
                    binding?.questAbortButton?.visibility = View.GONE
                }
            } else {
                binding?.questLeaderResponseWrapper?.visibility = View.GONE
                binding?.questParticipantResponseWrapper?.visibility = View.GONE
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
        if (binding?.titleView == null || !questContent.isManaged) {
            return
        }
        binding?.titleView?.text = questContent.text
        binding?.descriptionView?.setMarkdown(questContent.notes)
        DataBindingUtils.loadImage(binding?.questScrollImageView, "inventory_quest_scroll_" + questContent.key)
    }

    private fun setQuestParticipants(participants: List<Member>?) {
        if (binding?.questParticipantList == null) {
            return
        }
        binding?.questParticipantList?.removeAllViews()
        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        var participantCount = 0
        for (participant in participants ?: emptyList()) {
            if (quest?.active == true && participant.participatesInQuest == false) {
                continue
            }
            val participantView = inflater?.inflate(R.layout.quest_participant, binding?.questParticipantList, false)
            val textView = participantView?.findViewById<View>(R.id.participant_name) as? TextView
            textView?.text = participant.displayName
            val statusTextView = participantView?.findViewById<View>(R.id.status_view) as? TextView
            if (quest?.active == false) {
                context?.let {
                    when (participant.participatesInQuest) {
                        null -> {
                            statusTextView?.setText(R.string.pending)
                            statusTextView?.setTextColor(ContextCompat.getColor(it, R.color.text_ternary))
                        }
                        true -> {
                            statusTextView?.setText(R.string.accepted)
                            statusTextView?.setTextColor(ContextCompat.getColor(it, R.color.text_green))
                        }
                        else -> {
                            statusTextView?.setText(R.string.declined)
                            statusTextView?.setTextColor(ContextCompat.getColor(it, R.color.text_red))
                        }
                    }
                }
                binding?.questParticipantList?.addView(participantView)
            } else {
                statusTextView?.visibility = View.GONE
                if (participant.participatesInQuest == true) {
                    binding?.questParticipantList?.addView(participantView)
                }
            }
            if (participant.participatesInQuest == true) {
                participantCount += 1
            }
        }
        if (quest?.active == true) {
            binding?.participantsHeader?.setText(R.string.participants)
            binding?.participantsHeaderCount?.text = participantCount.toString()
        } else {
            binding?.participantsHeader?.setText(R.string.invitations)
            binding?.participantsHeaderCount?.text = participantCount.toString() + "/" + quest?.participants?.size
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
        socialRepository.acceptQuest(user, partyID).subscribe({ }, RxErrorHandler.handleEmptyError())
        }
    }


    private fun onQuestReject() {
        partyId?.let { partyID ->
            socialRepository.rejectQuest(user, partyID).subscribe({ }, RxErrorHandler.handleEmptyError())
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
                            .subscribe({ }, RxErrorHandler.handleEmptyError())
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
                            .flatMap { userRepository.retrieveUser() }
                            .subscribe({ getActivity()?.supportFragmentManager?.popBackStack() }, RxErrorHandler.handleEmptyError())
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
                                .flatMap { userRepository.retrieveUser() }
                                .subscribe({ getActivity()?.supportFragmentManager?.popBackStack() }, RxErrorHandler.handleEmptyError())
                    }
                }.setNegativeButton(R.string.no) { _, _ -> }
        builder.show()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }
}
