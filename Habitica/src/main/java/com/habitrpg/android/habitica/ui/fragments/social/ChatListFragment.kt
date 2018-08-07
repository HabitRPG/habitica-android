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
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RemoteConfigManager
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.Companion.showSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.tavern_chat_new_entry_item.*
import javax.inject.Inject

class ChatListFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var configManager: RemoteConfigManager

    private var isTavern: Boolean = false
    internal var layoutManager: LinearLayoutManager? = null
    internal var groupId: String? = null
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
                    userRepository.getUser().subscribe(Consumer { habitRPGUser -> this.user = habitRPGUser }, RxErrorHandler.handleEmptyError())
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

        layoutManager = recyclerView.layoutManager as? LinearLayoutManager

        if (layoutManager == null) {
            layoutManager = LinearLayoutManager(context)
            recyclerView.layoutManager = layoutManager
        }

        chatAdapter = ChatRecyclerViewAdapter(null, true, user, true)
        chatAdapter.notNull {
            compositeSubscription.add(it.getUserLabelClickFlowable().subscribe(Consumer { userId ->
                context.notNull { FullProfileActivity.open(it, userId) }
            }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(it.getDeleteMessageFlowable().subscribe(Consumer { this.showDeleteConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(it.getFlagMessageClickFlowable().subscribe(Consumer { this.showFlagConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(it.getReplyMessageEvents().subscribe(Consumer{ chatEditText.setText(it, TextView.BufferType.EDITABLE) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(it.getCopyMessageFlowable().subscribe(Consumer { this.copyMessageToClipboard(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(it.getLikeMessageFlowable().flatMap<ChatMessage> { socialRepository.likeMessage(it) }.subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        }

        chatBarView.sendAction = { sendChatMessage(it) }
        chatBarView.maxChatLength = configManager.maxChatLength()

        recyclerView.adapter = chatAdapter
        recyclerView.itemAnimator = SafeDefaultItemAnimator()

        groupId.notNull { id ->
            socialRepository.getGroupChat(id).firstElement()
                    .subscribe(Consumer<RealmResults<ChatMessage>> { this.setChatMessages(it) }, RxErrorHandler.handleEmptyError())
        }

        if (user?.flags?.isCommunityGuidelinesAccepted == true) {
            communityGuidelinesView.visibility = View.GONE
        } else {
            communityGuidelinesView.setOnClickListener {
                val i = Intent(Intent.ACTION_VIEW)
                i.data = Uri.parse("https://habitica.com/static/community-guidelines")
                context?.startActivity(i)
                userRepository.updateUser(user, "flags.communityGuidelinesAccepted", true).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())
            }
        }
        onRefresh()
    }


    override fun onRefresh() {
        refreshLayout.isRefreshing = true
        groupId.notNull {id ->
            socialRepository.retrieveGroupChat(id)
                    .doOnEvent { _, _ -> refreshLayout?.isRefreshing = false }.subscribe(Consumer {}, RxErrorHandler.handleEmptyError())
        }
    }

    fun setNavigatedToFragment() {
        navigatedOnceToFragment = true

        markMessagesAsSeen()
    }

    private fun markMessagesAsSeen() {
        if (!isTavern && groupId?.isNotEmpty() == true && gotNewMessages && navigatedOnceToFragment) {
            gotNewMessages = false
            groupId.notNull {id ->
                socialRepository.markMessagesSeen(id)
            }
        }
    }

    private fun copyMessageToClipboard(chatMessage: ChatMessage) {
        val clipMan = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val messageText = ClipData.newPlainText("Chat message", chatMessage.text)
        clipMan?.primaryClip = messageText
        val activity = activity as? MainActivity
        if (activity != null) {
            showSnackbar(activity.floatingMenuWrapper, getString(R.string.chat_message_copied), SnackbarDisplayType.NORMAL)
        }
    }

    private fun showFlagConfirmationDialog(chatMessage: ChatMessage) {
        val context = context
        if (context != null) {
            val builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.chat_flag_confirmation)
                    .setPositiveButton(R.string.flag_confirm) { _, _ ->
                        socialRepository.flagMessage(chatMessage)
                                .subscribe(Consumer {
                                    val activity = activity as? MainActivity
                                    activity?.floatingMenuWrapper.notNull {
                                        showSnackbar(it, "Flagged message by " + chatMessage.user, SnackbarDisplayType.NORMAL)
                                    }
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
                    .setPositiveButton(android.R.string.yes) { _, _ -> socialRepository.deleteMessage(chatMessage).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()) }
                    .setNegativeButton(android.R.string.no, null).show()
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
        groupId.notNull {id ->
            socialRepository.postGroupChat(id, chatText).subscribe(Consumer {
                recyclerView?.scrollToPosition(0)
            }, RxErrorHandler.handleEmptyError())
        }
    }
}
