package com.habitrpg.android.habitica.ui.fragments.social

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.text.toHtml
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentQuestDetailBinding
import com.habitrpg.android.habitica.extensions.fromHtml
import com.habitrpg.android.habitica.helpers.HapticFeedbackManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.Group
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.helpers.MarkdownParser
import javax.inject.Inject
import javax.inject.Named

class QuestDetailFragment : BaseMainFragment<FragmentQuestDetailBinding>() {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String
    @Inject
    lateinit var userViewModel: MainUserViewModel

    override var binding: FragmentQuestDetailBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentQuestDetailBinding {
        return FragmentQuestDetailBinding.inflate(inflater, container, false)
    }

    private var party: Group? = null
    private var quest: Quest? = null
    private var beginQuestMessage: String? = null

    private val isQuestActive: Boolean
        get() = quest?.active == true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showsBackButton = true
        super.onViewCreated(view, savedInstanceState)

        binding?.questBeginButton?.setOnClickListener { onQuestBegin() }
        binding?.questCancelButton?.setOnClickListener { onQuestCancel() }
        binding?.questLeaveButton?.setOnClickListener { onQuestLeave() }

        compositeSubscription.add(
            userRepository.getUserFlowable()
                .map {
                    it.party?.id ?: ""
                }
                .skipWhile { it.isBlank() }
                .distinctUntilChanged()
                .flatMap { socialRepository.getGroupFlowable(it) }
                .doOnNext { updateParty(it) }
                .map {
                    it.quest?.key ?: ""
                }
                .skipWhile {
                    it.isBlank()
                }
                .distinctUntilChanged()
                .flatMap { inventoryRepository.getQuestContent(it) }
                .subscribe(
                    {
                        updateQuestContent(it)
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
    }

    private fun updateParty(group: Group?) {
        if (binding?.titleView == null || group == null || group.quest == null) {
            return
        }
        party = group
        quest = group.quest
        setQuestParticipants(group.quest?.participants)
        compositeSubscription.add(
            socialRepository.getMember(quest?.leader).subscribe(
                { member ->
                    if (context != null && binding?.questLeaderView != null) {
                        binding?.questLeaderView?.text = context?.getString(R.string.quest_leader_header, member.displayName)
                    }
                },
                RxErrorHandler.handleEmptyError()
            )
        )

        val user = userViewModel.user.value
        if (binding?.questResponseWrapper != null) {
            if (userId != party?.quest?.leader && user?.party?.quest?.key == group.quest?.key && user?.party?.quest?.RSVPNeeded == false) {
                binding?.questLeaveButton?.visibility = View.VISIBLE
            } else {
                binding?.questLeaveButton?.visibility = View.GONE
            }
            if (showLeaderButtons()) {
                binding?.questCancelButton?.visibility = View.VISIBLE
                if (isQuestActive) {
                    binding?.questBeginButton?.visibility = View.GONE
                } else {
                    binding?.questBeginButton?.visibility = View.VISIBLE
                }
            } else {
                binding?.questCancelButton?.visibility = View.GONE
                binding?.questBeginButton?.visibility = View.GONE
            }
        }
    }

    private fun showLeaderButtons(): Boolean {
        return userId == party?.quest?.leader || userId == party?.leaderID
    }

    private fun updateQuestContent(questContent: QuestContent) {
        if (binding?.titleView == null || !questContent.isManaged) {
            return
        }
        binding?.titleView?.text = questContent.text
        // We need to do this, because the quest description can contain markdown AND HTML.
        binding?.descriptionView?.setText(MarkdownParser.parseMarkdown(questContent.notes).toHtml().fromHtml(), TextView.BufferType.SPANNABLE)

        binding?.questScrollImageView?.loadImage("inventory_quest_scroll_" + questContent.key)
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
            @SuppressLint("SetTextI18n")
            binding?.participantsHeaderCount?.text = participantCount.toString() + "/" + participants?.size
            beginQuestMessage = getString(R.string.quest_begin_message, participantCount, participants?.size)
        }
    }

    override fun onDestroyView() {
        socialRepository.close()
        userRepository.close()
        inventoryRepository.close()
        super.onDestroyView()
    }

    private fun onQuestBegin() {
        HapticFeedbackManager.tap(requireView())
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
        HapticFeedbackManager.tap(requireView())
        context?.let {
            if (isQuestActive) {
                val builder = AlertDialog.Builder(getActivity())
                    .setMessage(R.string.quest_abort_message)
                    .setPositiveButton(R.string.yes) { _, _ ->
                        party?.id?.let { partyID ->
                            @Suppress("DEPRECATION")
                            socialRepository.abortQuest(partyID)
                                .flatMap { userRepository.retrieveUser() }
                                .subscribe({ getActivity()?.supportFragmentManager?.popBackStack() }, RxErrorHandler.handleEmptyError())
                        }
                    }.setNegativeButton(R.string.no) { _, _ -> }
                builder.show()
            } else {
                val alert = HabiticaAlertDialog(it)
                alert.setMessage(R.string.quest_cancel_message)
                alert.addButton(R.string.yes, true) { _, _ ->
                    party?.id?.let { partyID ->
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
    }

    private fun onQuestLeave() {
        HapticFeedbackManager.tap(requireView())
        val builder = AlertDialog.Builder(getActivity())
            .setMessage(if (quest?.active == true) R.string.quest_leave_message else R.string.quest_leave_message_nostart)
            .setPositiveButton(R.string.yes) { _, _ ->
                party?.id?.let { partyID ->
                    @Suppress("DEPRECATION")
                    socialRepository.leaveQuest(partyID)
                        .flatMap { userRepository.retrieveUser() }
                        .flatMap { socialRepository.retrieveGroup(partyID) }
                        .subscribe({ getActivity()?.supportFragmentManager?.popBackStack() }, RxErrorHandler.handleEmptyError())
                }
            }.setNegativeButton(R.string.no) { _, _ -> }
        builder.show()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }
}
