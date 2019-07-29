package com.habitrpg.android.habitica.ui.fragments.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.MainNavDirections
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.social.ChatRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.Companion.showSnackbar
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar.SnackbarDisplayType
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.tavern_chat_new_entry_item.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ChatListFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var configManager: AppConfigManager

    private var isTavern: Boolean = false
    internal var autocompleteContext: String = ""
    set(value) {
        field = value
        if (chatBarView != null) {
            chatBarView.autocompleteContext = value
        }
    }
    internal var layoutManager: LinearLayoutManager? = null
    internal var groupId: String? = null
    private var user: User? = null
    private var userId: String? = null
    private var chatAdapter: ChatRecyclerViewAdapter? = null
    private var navigatedOnceToFragment = false
    private var gotNewMessages = false
    private var isScrolledToTop = true
    private var refreshDisposable: Disposable? = null

    fun configure(groupId: String, user: User?, isTavern: Boolean, autocompleteContext: String) {
        this.groupId = groupId
        this.user = user
        if (this.user != null) {
            this.userId = this.user?.id
        }
        this.isTavern = isTavern
        this.autocompleteContext = autocompleteContext
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
                    compositeSubscription.add(userRepository.getUser().subscribe(Consumer { habitRPGUser -> this.user = habitRPGUser }, RxErrorHandler.handleEmptyError()))
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

    override fun injectFragment(component: UserComponent) {
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
        chatAdapter?.let {adapter ->
            compositeSubscription.add(adapter.getUserLabelClickFlowable().subscribe(Consumer { userId ->
                FullProfileActivity.open(userId)
            }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getDeleteMessageFlowable().subscribe(Consumer { this.showDeleteConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getFlagMessageClickFlowable().subscribe(Consumer { this.showFlagConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getReplyMessageEvents().subscribe(Consumer{ setReplyTo(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getCopyMessageFlowable().subscribe(Consumer { this.copyMessageToClipboard(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getLikeMessageFlowable().flatMap<ChatMessage> { socialRepository.likeMessage(it) }.subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        }

        chatBarView.sendAction = { sendChatMessage(it) }
        chatBarView.maxChatLength = configManager.maxChatLength()
        chatBarView.autocompleteContext = autocompleteContext
        chatBarView.groupID = groupId

        recyclerView.adapter = chatAdapter
        recyclerView.itemAnimator = SafeDefaultItemAnimator()

        groupId?.let { id ->
            socialRepository.getGroupChat(id).firstElement()
                    .subscribe(Consumer<RealmResults<ChatMessage>> { this.setChatMessages(it) }, RxErrorHandler.handleEmptyError())
        }

        communityGuidelinesReviewView.setOnClickListener {
            MainNavigationController.navigate(R.id.guidelinesActivity)
        }
        communityGuidelinesAcceptButton.setOnClickListener {
            userRepository.updateUser(user, "flags.communityGuidelinesAccepted", true).subscribe(Consumer {}, RxErrorHandler.handleEmptyError())
        }

        compositeSubscription.add(userRepository.getUser().subscribe {user ->
            if (user?.flags?.isCommunityGuidelinesAccepted == true) {
                communityGuidelinesView.visibility = View.GONE
                chatBarContent.visibility = View.VISIBLE
            } else {
                chatBarContent.visibility = View.GONE
                communityGuidelinesView.visibility = View.VISIBLE
            }
        })


        recyclerView.addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                isScrolledToTop = layoutManager?.findFirstVisibleItemPosition() == 0
            }
        })

        refresh(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoRefreshing()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            startAutoRefreshing()
        } else {
            stopAutoRefreshing()
        }
    }

    private fun startAutoRefreshing() {
        if (refreshDisposable != null && refreshDisposable?.isDisposed != true) {
            refreshDisposable?.dispose()
        }
        refreshDisposable = Observable.interval(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
            refresh(false)
        }, RxErrorHandler.handleEmptyError())
    }

    private fun stopAutoRefreshing() {
        if (refreshDisposable?.isDisposed != true) {
            refreshDisposable?.dispose()
            refreshDisposable = null
        }
    }

    private fun setReplyTo(username: String?) {
        val previousText = chatEditText.text.toString()
        if (previousText.contains("@$username")) {
            return
        }
        chatEditText.setText("@$username $previousText", TextView.BufferType.EDITABLE)
    }

    override fun onRefresh() {
        refresh(true)
    }

    private fun refresh(isUserInitiated: Boolean) {
         if (isUserInitiated) {
             refreshLayout.isRefreshing = true
         }
        groupId?.let {id ->
            socialRepository.retrieveGroupChat(id)
                    .doOnEvent { _, _ -> refreshLayout?.isRefreshing = false }.subscribe(Consumer {
                        if (isScrolledToTop && recyclerView != null) {
                            recyclerView.scrollToPosition(0)
                        }
                    }, RxErrorHandler.handleEmptyError())
        }
    }

    fun setNavigatedToFragment() {
        navigatedOnceToFragment = true
        markMessagesAsSeen()
    }

    private fun markMessagesAsSeen() {
        if (!isTavern && groupId?.isNotEmpty() == true && gotNewMessages && navigatedOnceToFragment) {
            gotNewMessages = false
            groupId?.let {id ->
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
            showSnackbar(activity.snackbarContainer, getString(R.string.chat_message_copied), SnackbarDisplayType.NORMAL)
        }
    }

    private fun showFlagConfirmationDialog(chatMessage: ChatMessage) {
        val directions = MainNavDirections.actionGlobalReportMessageActivity(chatMessage.text ?: "", chatMessage.user ?: "", chatMessage.id)
        MainNavigationController.navigate(directions)
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
        chatBarView.chatMessages = socialRepository.getUnmanagedCopy(chatMessages)
        recyclerView.scrollToPosition(0)

        gotNewMessages = true

        markMessagesAsSeen()
    }

    private fun sendChatMessage(chatText: String) {
        groupId?.let {id ->
            socialRepository.postGroupChat(id, chatText).subscribe({
                recyclerView?.scrollToPosition(0)
            }, { throwable ->
                chatEditText.setText(chatText)
            RxErrorHandler.reportError(throwable)})
        }
    }
}
