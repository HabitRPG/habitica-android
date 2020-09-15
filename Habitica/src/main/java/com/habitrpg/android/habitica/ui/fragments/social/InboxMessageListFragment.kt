package com.habitrpg.android.habitica.ui.fragments.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.habitrpg.android.habitica.MainNavDirections
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.ui.activities.FullProfileActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.social.InboxAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.KeyboardUtil
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.InboxViewModel
import com.habitrpg.android.habitica.ui.viewmodels.InboxViewModelFactory
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
    lateinit var configManager: AppConfigManager

    private var chatAdapter: InboxAdapter? = null
    private var chatRoomUser: String? = null
    private var replyToUserUUID: String? = null

    private var viewModel: InboxViewModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.hidesToolbar = true
        super.onCreateView(inflater, container, savedInstanceState)

        return inflater.inflate(R.layout.fragment_inbox_message_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swipeRefreshLayout?.setOnRefreshListener(this)

        arguments?.let {
            val args = InboxMessageListFragmentArgs.fromBundle(it)
            setReceivingUser(args.username, args.userID)
        }
        viewModel = ViewModelProviders.of(this, InboxViewModelFactory(replyToUserUUID, chatRoomUser)).get(InboxViewModel::class.java)

        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.getActivity())
        recyclerView.layoutManager = layoutManager

        chatAdapter = InboxAdapter(user)
        viewModel?.messages?.observe(this.viewLifecycleOwner, Observer { chatAdapter?.submitList(it) })
        viewModel?.getMemberData()?.observe(this.viewLifecycleOwner, Observer {
            activity?.binding?.toolbarTitle?.text = it?.profile?.name
        })
        recyclerView.adapter = chatAdapter
        recyclerView.itemAnimator = SafeDefaultItemAnimator()
        chatAdapter?.let { adapter ->
            compositeSubscription.add(adapter.getUserLabelClickFlowable().subscribe(Consumer<String> {
                FullProfileActivity.open(it)
            }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getDeleteMessageFlowable().subscribe(Consumer { this.showDeleteConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getFlagMessageClickFlowable().subscribe(Consumer { this.showFlagConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(adapter.getCopyMessageFlowable().subscribe(Consumer { this.copyMessageToClipboard(it) }, RxErrorHandler.handleEmptyError()))
        }

        chatBarView.sendAction = { sendMessage(it) }
        chatBarView.maxChatLength = configManager.maxChatLength()

        communityGuidelinesView.visibility = View.GONE
    }

    override fun onResume() {
        if (replyToUserUUID?.isNotBlank() != true && chatRoomUser?.isNotBlank() != true) {
            fragmentManager?.popBackStack()
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
        val item = menu.findItem(R.id.open_profile)
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

    private fun refreshConversation() {
        if (viewModel?.memberID?.isNotBlank() != true) { return }
        compositeSubscription.add(this.socialRepository.retrieveInboxMessages(replyToUserUUID ?: "", 0)
                .subscribe(Consumer {}, RxErrorHandler.handleEmptyError(), Action {
                    swipeRefreshLayout?.isRefreshing = false
                    viewModel?.invalidateDataSource()
                }))
    }

    override fun onRefresh() {
        this.swipeRefreshLayout?.isRefreshing = true
        this.refreshConversation()
    }

    private fun sendMessage(chatText: String) {
        viewModel?.memberID?.let {userID ->
            socialRepository.postPrivateMessage(userID, chatText)
                    .delay(200, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                recyclerView?.scrollToPosition(0)
                        viewModel?.invalidateDataSource()
            }, { error ->
                        RxErrorHandler.reportError(error)
                        chatBarView.message = chatText
                    })
            KeyboardUtil.dismissKeyboard(getActivity())
        }
    }

    private fun setReceivingUser(chatRoomUser: String?, replyToUserUUID: String) {
        this.chatRoomUser = chatRoomUser
        this.replyToUserUUID = replyToUserUUID
        activity?.title = chatRoomUser
    }

    private fun copyMessageToClipboard(chatMessage: ChatMessage) {
        val clipMan = getActivity()?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val messageText = ClipData.newPlainText("Chat message", chatMessage.text)
        clipMan?.setPrimaryClip(messageText)
        val activity = getActivity() as? MainActivity
        if (activity != null) {
            showSnackbar(activity.snackbarContainer, getString(R.string.chat_message_copied), HabiticaSnackbar.SnackbarDisplayType.NORMAL)
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

    private fun openProfile() {
        replyToUserUUID?.let { FullProfileActivity.open(it) }
    }
}
