package com.habitrpg.android.habitica.ui.fragments.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.UiUtils
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.showSnackbar
import kotlinx.android.synthetic.main.fragment_inbox_message_list.*
import kotlinx.android.synthetic.main.tavern_chat_new_entry_item.*
import rx.functions.Action0
import rx.functions.Action1
import javax.inject.Inject

class InboxMessageListFragment : BaseMainFragment(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var socialRepository: SocialRepository

    private var chatAdapter: ChatRecyclerViewAdapter? = null
    private var chatRoomUser: String? = null
    private var replyToUserUUID: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        hideToolbar()
        disableToolbarScrolling()
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.fragment_inbox_message_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout?.setOnRefreshListener(this)

        val layoutManager = LinearLayoutManager(this.getActivity())
        recyclerView.layoutManager = layoutManager

        chatAdapter = ChatRecyclerViewAdapter(null, true, user, false)
        chatAdapter?.setSendingUser(this.user)
        recyclerView.adapter = chatAdapter
        recyclerView.itemAnimator = SafeDefaultItemAnimator()
        compositeSubscription.add(chatAdapter?.userLabelClickEvents?.subscribe(Action1<String> { FullProfileActivity.open(context, it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.deleteMessageEvents?.subscribe(Action1<ChatMessage> { this.showDeleteConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.flagMessageEvents?.subscribe(Action1<ChatMessage> { this.showFlagConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.copyMessageAsTodoEvents?.subscribe(Action1<ChatMessage> { this.copyMessageAsTodo(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.copyMessageEvents?.subscribe(Action1<ChatMessage> { this.copyMessageToClipboard(it) }, RxErrorHandler.handleEmptyError()))

        chatBarView.sendAction = { sendMessage(it) }

        loadMessages()

        communityGuidelinesView.visibility = View.GONE

        view.invalidate()
        view.forceLayout()
    }

    private fun loadMessages() {
        if (user?.isManaged == true) {
            userRepository.getInboxMessages(replyToUserUUID)
                    .first()
                    .subscribe(Action1 { this.chatAdapter?.updateData(it) }, RxErrorHandler.handleEmptyError())
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
        this.userRepository.retrieveUser(true)
                .subscribe(Action1<User> {
                    user = it
                }, RxErrorHandler.handleEmptyError(), Action0 {
                    swipeRefreshLayout?.isRefreshing = false
                })
    }

    override fun onRefresh() {
        this.refreshUserInbox()
    }

    private fun sendMessage(chatText: String) {
        socialRepository.postPrivateMessage(replyToUserUUID, chatText)
                .subscribe(Action1 { this.refreshUserInbox() }, RxErrorHandler.handleEmptyError())
        UiUtils.dismissKeyboard(getActivity())
    }

    fun setReceivingUser(chatRoomUser: String, replyToUserUUID: String) {
        this.chatRoomUser = chatRoomUser
        this.replyToUserUUID = replyToUserUUID
    }

    private fun copyMessageToClipboard(chatMessage: ChatMessage) {
        val clipMan = getActivity()?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val messageText = ClipData.newPlainText("Chat message", chatMessage.text)
        clipMan.primaryClip = messageText
        val activity = getActivity() as MainActivity?
        if (activity != null) {
            showSnackbar(activity.getFloatingMenuWrapper(), getString(R.string.chat_message_copied), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
        }
    }

    private fun showFlagConfirmationDialog(chatMessage: ChatMessage) {
        val builder = AlertDialog.Builder(getActivity()!!)
        builder.setMessage(R.string.chat_flag_confirmation)
                .setPositiveButton(R.string.flag_confirm) { _, _ ->
                    socialRepository.flagMessage(chatMessage)
                            .subscribe(Action1 {
                                val activity = getActivity() as MainActivity?
                                showSnackbar(activity!!.getFloatingMenuWrapper(), "Flagged message by " + chatMessage.user, HabiticaSnackbar.SnackbarDisplayType.NORMAL)
                            }, RxErrorHandler.handleEmptyError())
                }
                .setNegativeButton(R.string.action_cancel) { dialog, id -> }
        builder.show()
    }

    private fun showDeleteConfirmationDialog(chatMessage: ChatMessage) {
        val context = context
        if (context != null) {
            AlertDialog.Builder(context)
                    .setTitle(R.string.confirm_delete_tag_title)
                    .setMessage(R.string.confirm_delete_tag_message)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes) { _, _ -> socialRepository.deleteMessage(chatMessage).subscribe(Action1 { }, RxErrorHandler.handleEmptyError()) }
                    .setNegativeButton(android.R.string.no, null).show()
        }
    }

    private fun copyMessageAsTodo(chatMessage: ChatMessage) {

    }
}
