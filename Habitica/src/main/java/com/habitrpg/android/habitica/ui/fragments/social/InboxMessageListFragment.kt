package com.habitrpg.android.habitica.ui.fragments.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.MainNavDirections
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentInboxMessageListBinding
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.social.InboxAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.InboxViewModel
import com.habitrpg.android.habitica.ui.viewmodels.InboxViewModelFactory
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.Companion.showSnackbar
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class InboxMessageListFragment : BaseMainFragment<FragmentInboxMessageListBinding>() {

    override var binding: FragmentInboxMessageListBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentInboxMessageListBinding {
        return FragmentInboxMessageListBinding.inflate(inflater, container, false)
    }

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var configManager: AppConfigManager

    private var chatAdapter: InboxAdapter? = null
    private var chatRoomUser: String? = null
    private var replyToUserUUID: String? = null

    private val viewModel: InboxViewModel by viewModels(factoryProducer = {
        InboxViewModelFactory(replyToUserUUID, chatRoomUser)
    })

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

        arguments?.let {
            val args = InboxMessageListFragmentArgs.fromBundle(it)
            setReceivingUser(args.username, args.userID)
        }

        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.getActivity())
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = false
        binding?.recyclerView?.layoutManager = layoutManager
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            val member = if (replyToUserUUID?.isNotBlank() == true) {
                apiClient.getMember(replyToUserUUID!!)
            } else {
                apiClient.getMemberWithUsername(chatRoomUser ?: "")
            }
            setReceivingUser(member?.username, member?.id)
            activity?.title = member?.displayName
            chatAdapter = InboxAdapter(viewModel.user.value, member)
            chatAdapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    if (positionStart == 0) {
                        binding?.recyclerView?.scrollToPosition(0)
                    }
                }
            })
            binding?.recyclerView?.adapter = chatAdapter
            binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
            chatAdapter?.let { adapter ->
                    adapter.onOpenProfile = {
                            FullProfileActivity.open(it)
                        }
                adapter.onDeleteMessage = { showDeleteConfirmationDialog(it) }
                adapter.onFlagMessage = { showFlagConfirmationDialog(it) }
                adapter.onCopyMessage = { copyMessageToClipboard(it) }
            }
        }

        viewModel.messages.observe(viewLifecycleOwner) {
            markMessagesAsRead(it)
            chatAdapter?.submitList(it)
        }


        binding?.chatBarView?.sendAction = { sendMessage(it) }
        binding?.chatBarView?.maxChatLength = configManager.maxChatLength()

        binding?.chatBarView?.hasAcceptedGuidelines = true

        lifecycleScope.launchWhenResumed {
            while (true) {
                refreshConversation()
                delay(30.toDuration(DurationUnit.SECONDS))
            }
        }
    }

    override fun onResume() {
        if (replyToUserUUID?.isNotBlank() != true && chatRoomUser?.isNotBlank() != true) {
            parentFragmentManager.popBackStack()
        }
        super.onResume()
    }

    override fun onAttach(context: Context) {
        view?.invalidate()
        view?.forceLayout()

        super.onAttach(context)
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.activity?.menuInflater?.inflate(R.menu.inbox_chat, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.open_profile -> {
                openProfile()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun markMessagesAsRead(messages: List<ChatMessage>) {
        socialRepository.markSomePrivateMessagesAsRead(viewModel.user.value, messages)
    }

    private fun refreshConversation() {
        if (viewModel.memberID?.isNotBlank() != true) { return }
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            socialRepository.retrieveInboxMessages(replyToUserUUID ?: "", 0)
            viewModel.invalidateDataSource()
        }
    }

    private fun sendMessage(chatText: String) {
        viewModel.memberID?.let { userID ->
            lifecycleScope.launch(ExceptionHandler.coroutine { error ->
                ExceptionHandler.reportError(error)
                binding?.let {
                    val alert = HabiticaAlertDialog(it.chatBarView.context)
                    alert.setTitle("You cannot reply to this conversation")
                    alert.setMessage("This user is unable to receive your private message")
                    alert.addOkButton()
                    alert.show()
                }
                binding?.chatBarView?.message = chatText
            }) {
                socialRepository.postPrivateMessage(userID, chatText)
                delay(200.toDuration(DurationUnit.MILLISECONDS))
                viewModel.invalidateDataSource()
            }
        }
    }

    private fun setReceivingUser(chatRoomUser: String?, replyToUserUUID: String?) {
        this.chatRoomUser = chatRoomUser
        this.replyToUserUUID = replyToUserUUID
        activity?.title = chatRoomUser
    }

    private fun copyMessageToClipboard(chatMessage: ChatMessage) {
        val clipMan = getActivity()?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val messageText = ClipData.newPlainText("Chat message", chatMessage.text)
        clipMan?.setPrimaryClip(messageText)
        val activity = getActivity() as? MainActivity
        if (activity != null && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            showSnackbar(activity.snackbarContainer, getString(R.string.chat_message_copied), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
        }
    }

    private fun showFlagConfirmationDialog(chatMessage: ChatMessage) {
        val directions = MainNavDirections.actionGlobalReportMessageActivity(chatMessage.text ?: "", chatMessage.user ?: "", chatMessage.id, null)
        MainNavigationController.navigate(directions)
    }

    private fun showDeleteConfirmationDialog(chatMessage: ChatMessage) {
        val context = context
        if (context != null) {
            AlertDialog.Builder(context)
                .setTitle(R.string.confirm_delete_tag_title)
                .setMessage(R.string.confirm_delete_tag_message)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.yes) { _, _ ->
                    lifecycleScope.launchCatching {
                        socialRepository.deleteMessage(chatMessage)
                    }
                }
                .setNegativeButton(R.string.no, null).show()
        }
    }

    private fun openProfile() {
        replyToUserUUID?.let { FullProfileActivity.open(it) }
    }
}
