package com.habitrpg.android.habitica.ui.fragments.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.events.commands.SendNewInboxMessageCommand
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

import org.greenrobot.eventbus.Subscribe

import javax.inject.Inject

import butterknife.ButterKnife

import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.showSnackbar
import kotlinx.android.synthetic.main.fragment_inbox_message_list.*
import kotlinx.android.synthetic.main.tavern_chat_new_entry_item.*
import rx.functions.Action0
import rx.functions.Action1

class InboxMessageListFragment : BaseMainFragment(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var socialRepository: SocialRepository

    internal var chatAdapter: ChatRecyclerViewAdapter? = null
    internal var chatRoomUser: String? = null
    internal var replyToUserUUID: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        hideToolbar()
        disableToolbarScrolling()
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.fragment_inbox_message_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ButterKnife.bind(this, view)
        swipeRefreshLayout?.setOnRefreshListener(this)

        val layoutManager = LinearLayoutManager(this.getActivity())
        //layoutManager.setReverseLayout(true);
        //layoutManager.setStackFromEnd(false);
        recyclerView?.layoutManager = layoutManager

        chatAdapter = ChatRecyclerViewAdapter(null, true, user, false)
        chatAdapter?.setSendingUser(this.user)
        recyclerView?.adapter = chatAdapter
        recyclerView?.itemAnimator = SafeDefaultItemAnimator()
        compositeSubscription.add(chatAdapter?.userLabelClickEvents?.subscribe(Action1<String> { FullProfileActivity.open(context, it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.deleteMessageEvents?.subscribe(Action1<ChatMessage> { this.showDeleteConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.flagMessageEvents?.subscribe(Action1<ChatMessage> { this.showFlagConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.copyMessageAsTodoEvents?.subscribe(Action1<ChatMessage> { this.copyMessageAsTodo(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.copyMessageEvents?.subscribe(Action1<ChatMessage> { this.copyMessageToClipboard(it) }, RxErrorHandler.handleEmptyError()))

        loadMessages()

        communityGuidelinesView.visibility = View.GONE

        view.invalidate()
        view.forceLayout()
    }

    private fun loadMessages() {
        if (user != null && user!!.isManaged) {
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
        this.swipeRefreshLayout!!.isRefreshing = true
        this.userRepository!!.retrieveUser(true)
                .subscribe(Action1<User> {
                    user = it
                }, RxErrorHandler.handleEmptyError(), Action0 {
                    swipeRefreshLayout?.isRefreshing = false
                })
    }

    override fun onRefresh() {
        this.refreshUserInbox()
    }

    @Subscribe
    fun onEvent(cmd: SendNewInboxMessageCommand) {
        socialRepository.postPrivateMessage(cmd.userToSendTo, cmd.message)
                .subscribe(Action1 { this.refreshUserInbox() }, RxErrorHandler.handleEmptyError())
        UiUtils.dismissKeyboard(getActivity())
    }

    fun setReceivingUser(chatRoomUser: String, replyToUserUUID: String) {
        this.chatRoomUser = chatRoomUser
        this.replyToUserUUID = replyToUserUUID
    }

    fun onChatMessageTextChanged() {
        val chatText = chatEditText.text
        setSendButtonEnabled(chatText.length > 0)
    }

    private fun setSendButtonEnabled(enabled: Boolean) {
        val tintColor: Int
        if (enabled) {
            tintColor = ContextCompat.getColor(context!!, R.color.brand_400)
        } else {
            tintColor = ContextCompat.getColor(context!!, R.color.md_grey_400)
        }
        sendButton.isEnabled = enabled
        sendButton.setColorFilter(tintColor)
    }

    fun sendChatMessage() {
        val chatText = chatEditText.text.toString()
        if (chatText.length > 0) {
            chatEditText.setText(null)
            socialRepository.postPrivateMessage(replyToUserUUID, chatText).subscribe(Action1 {
                recyclerView?.scrollToPosition(0)
            }, RxErrorHandler.handleEmptyError())
        }
    }


    fun copyMessageToClipboard(chatMessage: ChatMessage) {
        val clipMan = getActivity()!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val messageText = ClipData.newPlainText("Chat message", chatMessage.text)
        clipMan.primaryClip = messageText
        val activity = getActivity() as MainActivity?
        showSnackbar(activity!!.getFloatingMenuWrapper(), getString(R.string.chat_message_copied), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
    }

    fun showFlagConfirmationDialog(chatMessage: ChatMessage) {
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
