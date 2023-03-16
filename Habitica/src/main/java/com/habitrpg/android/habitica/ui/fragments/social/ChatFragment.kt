package com.habitrpg.android.habitica.ui.fragments.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.MainNavDirections
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.databinding.FragmentChatBinding
import com.habitrpg.android.habitica.extensions.observeOnce
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.GroupViewModel
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.Companion.showSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class ChatFragment : BaseFragment<FragmentChatBinding>() {

    override var binding: FragmentChatBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentChatBinding {
        return FragmentChatBinding.inflate(inflater, container, false)
    }

    lateinit var viewModel: GroupViewModel

    @Inject
    lateinit var configManager: AppConfigManager

    private var chatAdapter: ChatRecyclerViewAdapter? = null
    private var navigatedOnceToFragment = false
    private var isScrolledToBottom = true
    var isRefreshing = true
    var autocompleteContext: String = ""

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = false
        binding?.recyclerView?.layoutManager = layoutManager

        chatAdapter = ChatRecyclerViewAdapter(null, true)
        chatAdapter?.let { adapter ->
            adapter.onOpenProfile = { userId -> FullProfileActivity.open(userId) }
            adapter.onDeleteMessage = { this.showDeleteConfirmationDialog(it) }
            adapter.onFlagMessage = { this.showFlagConfirmationDialog(it) }
            adapter.onReply = { setReplyTo(it) }
            adapter.onCopyMessage = { this.copyMessageToClipboard(it) }
            adapter.onMessageLike = { viewModel.likeMessage(it) }
        }

        binding?.chatBarView?.sendAction = { sendChatMessage(it) }
        binding?.chatBarView?.maxChatLength = configManager.maxChatLength()
        binding?.chatBarView?.autocompleteContext = "party"
        binding?.chatBarView?.groupID = viewModel.getGroupData().value?.id

        binding?.recyclerView?.adapter = chatAdapter
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        binding?.recyclerView?.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                dx: Int,
                dy: Int
            ) {
                super.onScrolled(recyclerView, dx, dy)
                isScrolledToBottom = layoutManager.findFirstVisibleItemPosition() == 0
            }
        })

        viewModel.chatmessages.observe(viewLifecycleOwner, { setChatMessages(it) })

        binding?.chatBarView?.onCommunityGuidelinesAccepted = {
            viewModel.updateUser("flags.communityGuidelinesAccepted", true)
            binding?.chatBarView?.hasAcceptedGuidelines = true
        }

        viewModel.user.observeOnce(viewLifecycleOwner) {
            chatAdapter?.user = it
            binding?.chatBarView?.hasAcceptedGuidelines =
                it?.flags?.communityGuidelinesAccepted == true
        }

        lifecycleScope.launchWhenResumed {
            while (true) {
                refresh()
                delay(30.toDuration(DurationUnit.SECONDS))
            }
        }
    }

    private fun setReplyTo(username: String?) {
        val previousMessage = binding?.chatBarView?.message ?: ""
        if (previousMessage.contains("@$username")) {
            return
        }
        binding?.chatBarView?.message = "@$username $previousMessage"
    }

    private fun refresh() {
        viewModel.retrieveGroupChat {
            if (isScrolledToBottom || isRefreshing) {
                lifecycleScope.launch {
                    delay(500)
                    binding?.recyclerView?.scrollToPosition(0)
                }
            }
            isRefreshing = false
        }
    }

    fun setNavigatedToFragment() {
        navigatedOnceToFragment = true
        markMessagesAsSeen()
    }

    private fun markMessagesAsSeen() {
        if (navigatedOnceToFragment) {
            viewModel.markMessagesSeen()
        }
    }

    private fun copyMessageToClipboard(chatMessage: ChatMessage) {
        val clipMan = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val messageText = ClipData.newPlainText("Chat message", chatMessage.text)
        clipMan?.setPrimaryClip(messageText)
        val activity = activity as? MainActivity
        if (activity != null && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            showSnackbar(activity.snackbarContainer, getString(R.string.chat_message_copied), SnackbarDisplayType.NORMAL)
        }
    }

    private fun showFlagConfirmationDialog(chatMessage: ChatMessage) {
        val directions = MainNavDirections.actionGlobalReportMessageActivity(chatMessage.text ?: "", chatMessage.user ?: "", chatMessage.id, chatMessage.groupId)
        MainNavigationController.navigate(directions)
    }

    private fun showDeleteConfirmationDialog(chatMessage: ChatMessage) {
        val context = context
        if (context != null) {
            val dialog = HabiticaAlertDialog(context)
            dialog.setTitle(R.string.confirm_delete_tag_title)
            dialog.setMessage(R.string.confirm_delete_tag_message)
            dialog.addButton(R.string.yes, true, true) { _, _ ->
                viewModel.deleteMessage(chatMessage)
            }
            dialog.show()
        }
    }

    private fun setChatMessages(chatMessages: List<ChatMessage>) {
        chatAdapter?.data = chatMessages
        binding?.chatBarView?.chatMessages = chatMessages

        viewModel.gotNewMessages = true

        markMessagesAsSeen()
    }

    private fun sendChatMessage(chatText: String) {
        viewModel.postGroupChat(
            chatText,
            { binding?.recyclerView?.scrollToPosition(0) }
        ) { binding?.chatBarView?.message = chatText }
    }
}
