package com.habitrpg.android.habitica.ui.fragments.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RemoteConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.KeyboardUtil
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.Companion.showSnackbar
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.fragment_inbox_message_list.*
import kotlinx.android.synthetic.main.tavern_chat_new_entry_item.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class InboxMessageListFragment : BaseMainFragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var configManager: RemoteConfigManager

    private var chatAdapter: ChatRecyclerViewAdapter? = null
    private var chatRoomUser: String? = null
    private var replyToUserUUID: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        hideToolbar()
        disableToolbarScrolling()

        return inflater.inflate(R.layout.fragment_inbox_message_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout?.setOnRefreshListener(this)

        arguments.notNull {
            val args = InboxMessageListFragmentArgs.fromBundle(it)
            setReceivingUser(args.username, args.userID)
        }

        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.getActivity())
        recyclerView.layoutManager = layoutManager

        chatAdapter = ChatRecyclerViewAdapter(null, true, user, false, configManager.enableUsernameRelease())
        recyclerView.adapter = chatAdapter
        recyclerView.itemAnimator = SafeDefaultItemAnimator()
        chatAdapter.notNull { adapter ->
            compositeSubscription.add(adapter.getUserLabelClickFlowable().subscribe(Consumer<String> {
                context.notNull { context -> FullProfileActivity.open(context, it) }
            }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getDeleteMessageFlowable().subscribe(Consumer { this.showDeleteConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getFlagMessageClickFlowable().subscribe(Consumer { this.showFlagConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getCopyMessageFlowable().subscribe(Consumer { this.copyMessageToClipboard(it) }, RxErrorHandler.handleEmptyError()))
        }

        chatBarView.sendAction = { sendMessage(it) }
        chatBarView.maxChatLength = configManager.maxChatLength()

        loadMessages()

        communityGuidelinesView.visibility = View.GONE
    }

    override fun onAttach(context: Context?) {
        view?.invalidate()
        view?.forceLayout()

        super.onAttach(context)
    }

    private fun loadMessages() {
        if (user?.isManaged == true) {
            compositeSubscription.add(socialRepository.getInboxMessages(replyToUserUUID)
                    .firstElement()
                    .subscribe(Consumer { this.chatAdapter?.updateData(it) }, RxErrorHandler.handleEmptyError()))
        }
    }

    override fun onDestroyView() {
        showToolbar()
        enableToolbarScrolling()
        super.onDestroyView()
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    private fun refreshUserInbox() {
        this.swipeRefreshLayout?.isRefreshing = true
        compositeSubscription.add(this.socialRepository.retrieveInboxMessages()
                .subscribe(Consumer {}, RxErrorHandler.handleEmptyError(), Action {
                    swipeRefreshLayout?.isRefreshing = false
                }))
    }

    override fun onRefresh() {
        this.refreshUserInbox()
    }

    private fun sendMessage(chatText: String) {
        replyToUserUUID?.notNull {userID ->
            socialRepository.postPrivateMessage(userID, chatText)
                    .delay(200, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Consumer {
                recyclerView?.scrollToPosition(0)
            }, RxErrorHandler.handleEmptyError())
            KeyboardUtil.dismissKeyboard(getActivity())
        }
    }

    fun setReceivingUser(chatRoomUser: String, replyToUserUUID: String) {
        this.chatRoomUser = chatRoomUser
        this.replyToUserUUID = replyToUserUUID
    }

    private fun copyMessageToClipboard(chatMessage: ChatMessage) {
        val clipMan = getActivity()?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val messageText = ClipData.newPlainText("Chat message", chatMessage.text)
        clipMan?.primaryClip = messageText
        val activity = getActivity() as? MainActivity
        if (activity != null) {
            showSnackbar(activity.floatingMenuWrapper, getString(R.string.chat_message_copied), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
        }
    }

    private fun showFlagConfirmationDialog(chatMessage: ChatMessage) {
        val activity = getActivity() as? MainActivity ?: return
        val builder = AlertDialog.Builder(activity)
        builder.setMessage(R.string.chat_flag_confirmation)
                .setPositiveButton(R.string.flag_confirm) { _, _ ->
                    socialRepository.flagMessage(chatMessage)
                            .subscribe(Consumer { _ ->
                                activity.floatingMenuWrapper.notNull {
                                    showSnackbar(it, "Flagged message by " + chatMessage.user, HabiticaSnackbar.SnackbarDisplayType.NORMAL)
                                }
                            }, RxErrorHandler.handleEmptyError())
                }
                .setNegativeButton(R.string.action_cancel) { _, _ -> }
        builder.show()
    }

    private fun showDeleteConfirmationDialog(chatMessage: ChatMessage) {
        val context = context
        if (context != null) {
            AlertDialog.Builder(context)
                    .setTitle(R.string.confirm_delete_tag_title)
                    .setMessage(R.string.confirm_delete_tag_message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { _, _ -> socialRepository.deleteMessage(chatMessage).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()) }
                    .setNegativeButton(android.R.string.no, null).show()
        }
    }
}
