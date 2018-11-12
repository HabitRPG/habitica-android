package com.habitrpg.android.habitica.ui.fragments.social

import android.annotation.SuppressLint
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
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
import com.habitrpg.android.habitica.ui.viewmodels.PartyViewModel
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

@SuppressLint("ValidFragment")
class ChatFragment constructor(private val viewModel: PartyViewModel) : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var configManager: RemoteConfigManager

    internal var layoutManager: LinearLayoutManager? = null
    private var chatAdapter: ChatRecyclerViewAdapter? = null
    private var navigatedOnceToFragment = false
    private var isScrolledToTop = true
    private var refreshDisposable: Disposable? = null

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.fragment_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        refreshLayout.setOnRefreshListener(this)

        layoutManager = recyclerView.layoutManager as? LinearLayoutManager

        if (layoutManager == null) {
            layoutManager = LinearLayoutManager(context)
            recyclerView.layoutManager = layoutManager
        }

        chatAdapter = ChatRecyclerViewAdapter(null, true, null, true, configManager.enableUsernameRelease())
        chatAdapter.notNull {adapter ->
            compositeSubscription.add(adapter.getUserLabelClickFlowable().subscribe(Consumer { userId ->
                context.notNull { FullProfileActivity.open(it, userId) }
            }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getDeleteMessageFlowable().subscribe(Consumer { this.showDeleteConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getFlagMessageClickFlowable().subscribe(Consumer { this.showFlagConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getReplyMessageEvents().subscribe(Consumer{ setReplyTo(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getCopyMessageFlowable().subscribe(Consumer { this.copyMessageToClipboard(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getLikeMessageFlowable().subscribe(Consumer { viewModel.likeMessage(it) }, RxErrorHandler.handleEmptyError()))
        }

        chatBarView.sendAction = { sendChatMessage(it) }
        chatBarView.maxChatLength = configManager.maxChatLength()

        recyclerView.adapter = chatAdapter
        recyclerView.itemAnimator = SafeDefaultItemAnimator()

        compositeSubscription.add(viewModel.getChatMessages().firstElement().subscribe(Consumer<RealmResults<ChatMessage>> { this.setChatMessages(it) }, RxErrorHandler.handleEmptyError()))

        viewModel.getUserData().observe(viewLifecycleOwner, Observer {
            chatAdapter?.user = it
            if (it?.flags?.isCommunityGuidelinesAccepted == true) {
                communityGuidelinesView.visibility = View.GONE
            } else {
                communityGuidelinesView.setOnClickListener { _ ->
                    val i = Intent(Intent.ACTION_VIEW)
                    i.data = "https://habitica.com/static/community-guidelines".toUri()
                    context?.startActivity(i)
                    viewModel.updateUser("flags.communityGuidelinesAccepted", true)
                }
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
        viewModel.retrieveGroupChat {
            refreshLayout?.isRefreshing = false
            if (isScrolledToTop) {
                recyclerView.scrollToPosition(0)
            }
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
                        viewModel.flagMessage(chatMessage) {
                                    val activity = activity as? MainActivity
                                    activity?.floatingMenuWrapper.notNull {
                                        showSnackbar(it, "Flagged message by " + chatMessage.user, SnackbarDisplayType.NORMAL)
                                    }
                                }
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
                    .setPositiveButton(android.R.string.yes) { _, _ -> viewModel.deleteMessage(chatMessage) }
                    .setNegativeButton(android.R.string.no, null).show()
        }
    }

    private fun setChatMessages(chatMessages: RealmResults<ChatMessage>) {
        chatAdapter?.updateData(chatMessages)
        recyclerView.scrollToPosition(0)

        viewModel.gotNewMessages = true

        markMessagesAsSeen()
    }

    private fun sendChatMessage(chatText: String) {
        viewModel.postGroupChat(chatText) {
            recyclerView?.scrollToPosition(0)
        }
    }
}
