package com.habitrpg.android.habitica.ui.fragments.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
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
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.Companion.showSnackbar
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.tavern_chat_new_entry_item.*
import rx.functions.Action0
import rx.functions.Action1
import javax.inject.Inject

class ChatListFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    var seenGroupId: String = ""
    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository
    private var isTavern: Boolean = false
    internal var layoutManager: LinearLayoutManager? = null
    private var groupId: String = ""
    private var user: User? = null
    private var userId: String? = null
    private var chatAdapter: ChatRecyclerViewAdapter? = null
    private var navigatedOnceToFragment = false
    private var gotNewMessages = false

    fun configure(groupId: String, user: User?, isTavern: Boolean) {
        this.groupId = groupId
        this.user = user
        if (this.user != null) {
            this.userId = this.user?.id
        }
        this.isTavern = isTavern
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey("groupId")) {
                this.groupId = savedInstanceState.getString("groupId")
            }

            if (savedInstanceState.containsKey("isTavern")) {
                this.isTavern = savedInstanceState.getBoolean("isTavern")
            }

            if (savedInstanceState.containsKey("userId")) {
                this.userId = savedInstanceState.getString("userId")
                if (this.userId != null) {
                    userRepository.getUser(userId!!).subscribe(Action1 { habitRPGUser -> this.user = habitRPGUser }, RxErrorHandler.handleEmptyError())
                }
            }

        }
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onDestroy() {
        socialRepository.close()
        userRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLayout.setOnRefreshListener(this)

        layoutManager = recyclerView.layoutManager as LinearLayoutManager?

        if (layoutManager == null) {
            layoutManager = LinearLayoutManager(context)
            recyclerView.layoutManager = layoutManager
        }

        chatAdapter = ChatRecyclerViewAdapter(null, true, user, true)
        compositeSubscription.add(chatAdapter?.userLabelClickEvents?.subscribe(Action1 { userId -> FullProfileActivity.open(context, userId) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.deleteMessageEvents?.subscribe(Action1 { this.showDeleteConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.flagMessageEvents?.subscribe(Action1 { this.showFlagConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.copyMessageAsTodoEvents?.subscribe(Action1{ this.copyMessageAsTodo(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.copyMessageEvents?.subscribe(Action1 { this.copyMessageToClipboard(it) }, RxErrorHandler.handleEmptyError()))
        compositeSubscription.add(chatAdapter?.likeMessageEvents?.flatMap<ChatMessage>( { socialRepository.likeMessage(it) })?.subscribe(Action1 { }, RxErrorHandler.handleEmptyError()))

        chatBarView.sendAction = { sendChatMessage(it) }

        recyclerView.adapter = chatAdapter
        recyclerView.itemAnimator = SafeDefaultItemAnimator()

        socialRepository.getGroupChat(groupId).first().subscribe(Action1<RealmResults<ChatMessage>> { this.setChatMessages(it) }, RxErrorHandler.handleEmptyError())

        if (user?.flags?.isCommunityGuidelinesAccepted == true) {
            communityGuidelinesView.visibility = View.GONE
        } else {
            communityGuidelinesView.setOnClickListener {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://habitica.com/static/community-guidelines")
                context?.startActivity(i)
                userRepository.updateUser(user, "flags.communityGuidelinesAccepted", true).subscribe(Action1 { }, RxErrorHandler.handleEmptyError())
            }
        }
        onRefresh()
    }


    override fun onRefresh() {
        refreshLayout.isRefreshing = true
        socialRepository.retrieveGroupChat(groupId).subscribe(Action1 {}, RxErrorHandler.handleEmptyError(), Action0 { refreshLayout?.isRefreshing = false })
    }

    fun setNavigatedToFragment(groupId: String) {
        seenGroupId = groupId
        navigatedOnceToFragment = true

        markMessagesAsSeen()
    }

    private fun markMessagesAsSeen() {
        if (!isTavern && seenGroupId.isNotEmpty() && gotNewMessages && navigatedOnceToFragment) {
            gotNewMessages = false
            socialRepository.markMessagesSeen(seenGroupId)
        }
    }

    private fun copyMessageToClipboard(chatMessage: ChatMessage) {
        val clipMan = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val messageText = ClipData.newPlainText("Chat message", chatMessage.text)
        clipMan.primaryClip = messageText
        val activity = activity as MainActivity?
        if (activity != null) {
            showSnackbar(activity.getFloatingMenuWrapper(), getString(R.string.chat_message_copied), SnackbarDisplayType.NORMAL)
        }
    }

    private fun showFlagConfirmationDialog(chatMessage: ChatMessage) {
        val context = context
        if (context != null) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.chat_flag_confirmation)
                    .setPositiveButton(R.string.flag_confirm) { _, _ ->
                        socialRepository.flagMessage(chatMessage)
                                .subscribe(Action1 {
                                    val activity = activity as MainActivity?
                                    showSnackbar(activity!!.getFloatingMenuWrapper(), "Flagged message by " + chatMessage.user, SnackbarDisplayType.NORMAL)
                                }, RxErrorHandler.handleEmptyError())
                    }
                    .setNegativeButton(R.string.action_cancel) { _, _ -> }
            builder.show()
        }
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
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(context?.getString(R.string.chat_message), chatMessage.text)
        clipboard.primaryClip = clip
        val activity = activity as MainActivity?
        if (activity != null) {
            HabiticaSnackbar.showSnackbar(activity.getFloatingMenuWrapper(), getString(R.string.chat_message_copied), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("userId", this.userId)
        outState.putString("groupId", this.groupId)
        outState.putBoolean("isTavern", this.isTavern)
        super.onSaveInstanceState(outState)
    }

    private fun setChatMessages(chatMessages: RealmResults<ChatMessage>) {
        chatAdapter?.updateData(chatMessages)
        recyclerView.scrollToPosition(0)

        gotNewMessages = true

        markMessagesAsSeen()
    }

    private fun sendChatMessage(chatText: String) {
        socialRepository.postGroupChat(groupId, chatText).subscribe(Action1 {
            recyclerView?.scrollToPosition(0)
        }, RxErrorHandler.handleEmptyError())
    }
}
