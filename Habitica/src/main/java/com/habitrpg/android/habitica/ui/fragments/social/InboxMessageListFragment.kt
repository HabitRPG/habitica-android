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
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentInboxMessageListBinding
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.social.InboxAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.ReportBottomSheetFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.InboxViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.Companion.showSnackbar
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@AndroidEntryPoint
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

    private val viewModel: InboxViewModel by viewModels()

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

        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.activity)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = false
        binding?.recyclerView?.layoutManager = layoutManager
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            socialRepository.getMember(viewModel.recipientID ?: viewModel.recipientUsername ?: "").collect {
                mainActivity?.title = it?.displayName
                chatAdapter?.replyToUser = it
            }
        }
        chatAdapter = InboxAdapter(viewModel.user.value)
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
            adapter.onFlagMessage = { showFlagMessageBottomSheet(it) }
            adapter.onCopyMessage = { copyMessageToClipboard(it) }
        }

        viewModel.messages.observe(viewLifecycleOwner) {
            markMessagesAsRead(it)
            chatAdapter?.submitList(it)
        }

        binding?.chatBarView?.sendAction = { sendMessage(it) }
        binding?.chatBarView?.maxChatLength = configManager.maxChatLength()

        binding?.chatBarView?.hasAcceptedGuidelines = true

        lifecycleScope.launchCatching {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                while (true) {
                    refreshConversation()
                    delay(30.toDuration(DurationUnit.SECONDS))
                }
            }
        }
    }

    override fun onResume() {
        if (viewModel.recipientID?.isNotBlank() != true && viewModel.recipientUsername?.isNotBlank() != true) {
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
        this.mainActivity?.menuInflater?.inflate(R.menu.inbox_chat, menu)
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

    private fun markMessagesAsRead(messages: List<ChatMessage>) {
        socialRepository.markSomePrivateMessagesAsRead(viewModel.user.value, messages)
    }

    private fun refreshConversation() {
        if (viewModel.memberID?.isNotBlank() != true) { return }
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            socialRepository.retrieveInboxMessages(viewModel.recipientID ?: "", 0)
            viewModel.invalidateDataSource()
        }
    }

    private fun sendMessage(chatText: String) {
        viewModel.memberID?.let { userID ->
            lifecycleScope.launch(
                ExceptionHandler.coroutine { error ->
                    ExceptionHandler.reportError(error)
                    binding?.let {
                        val alert = HabiticaAlertDialog(it.chatBarView.context)
                        alert.setTitle("You cannot reply to this conversation")
                        alert.setMessage("This user is unable to receive your private message")
                        alert.addOkButton()
                        alert.show()
                    }
                    binding?.chatBarView?.message = chatText
                }
            ) {
                socialRepository.postPrivateMessage(userID, chatText)
                delay(200.toDuration(DurationUnit.MILLISECONDS))
                viewModel.invalidateDataSource()
            }
        }
    }

    private fun copyMessageToClipboard(chatMessage: ChatMessage) {
        val clipMan = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val messageText = ClipData.newPlainText("Chat message", chatMessage.text)
        clipMan?.setPrimaryClip(messageText)
        val activity = activity as? MainActivity
        if (activity != null && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            showSnackbar(activity.snackbarContainer, getString(R.string.chat_message_copied), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
        }
    }

    private fun showFlagMessageBottomSheet(chatMessage : ChatMessage) {
        val reportBottomSheetFragment = ReportBottomSheetFragment.newInstance(
            reportType = ReportBottomSheetFragment.REPORT_TYPE_MESSAGE,
            profileName = chatMessage.username ?: "",
            messageId = chatMessage.id,
            messageText = chatMessage.text ?: "",
            groupId = chatMessage.groupId ?: "",
            userIdBeingReported = chatMessage.userID ?: "",
            sourceView = this::class.simpleName ?: ""
        )

        reportBottomSheetFragment.show(childFragmentManager, ReportBottomSheetFragment.TAG)
    }

    private fun showDeleteConfirmationDialog(chatMessage: ChatMessage) {
        val context = context
        if (context != null) {
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.confirm_delete_tag_title)
            dialog.setMessage(R.string.confirm_delete_tag_message)
            dialog.addButton(R.string.yes, true, true) { _, _ ->
                lifecycleScope.launchCatching {
                    socialRepository.deleteMessage(chatMessage)
                    viewModel.invalidateDataSource()
                }
            }
            dialog.addButton(R.string.no, false)
            dialog.show()
        }
    }

    private fun openProfile() {
        viewModel.recipientID?.let { FullProfileActivity.open(it) }
    }
}
