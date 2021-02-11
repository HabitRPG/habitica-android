package com.habitrpg.android.habitica.ui.fragments.social

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.DataSource
import androidx.paging.PagedList
import com.habitrpg.android.habitica.MainNavDirections
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentInboxMessageListBinding
import com.habitrpg.android.habitica.extensions.addOkButton
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
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.functions.Action
import io.reactivex.rxjava3.functions.Consumer
import java.lang.Exception
import java.util.concurrent.TimeUnit
import javax.inject.Inject

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
    private var chatRoomUser: String? = null
    private var replyToUserUUID: String? = null

    private var viewModel: InboxViewModel? = null
    private var refreshDisposable: Disposable? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showsBackButton = true
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = InboxMessageListFragmentArgs.fromBundle(it)
            setReceivingUser(args.username, args.userID)
        }
        viewModel = ViewModelProvider(this, InboxViewModelFactory(replyToUserUUID, chatRoomUser)).get(InboxViewModel::class.java)

        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this.getActivity())
        layoutManager.reverseLayout = true
        layoutManager.stackFromEnd = false
        binding?.recyclerView?.layoutManager = layoutManager
        val observable = if (replyToUserUUID?.isNotBlank() == true) {
            apiClient.getMember(replyToUserUUID!!)
        } else {
            apiClient.getMemberWithUsername(chatRoomUser ?: "")
        }
        compositeSubscription.add(observable.subscribe( { member ->
            setReceivingUser(member.username, member.id)
            activity?.title = member.displayName
            chatAdapter = InboxAdapter(user, member)
            viewModel?.messages?.observe(this.viewLifecycleOwner, { chatAdapter?.submitList(it) })

            binding?.recyclerView?.adapter = chatAdapter
            binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
            chatAdapter?.let { adapter ->
                compositeSubscription.add(adapter.getUserLabelClickFlowable().subscribe({
                    FullProfileActivity.open(it)
                }, RxErrorHandler.handleEmptyError()))
                compositeSubscription.add(adapter.getDeleteMessageFlowable().subscribe({ this.showDeleteConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
                compositeSubscription.add(adapter.getFlagMessageClickFlowable().subscribe({ this.showFlagConfirmationDialog(it) }, RxErrorHandler.handleEmptyError()))
                compositeSubscription.add(adapter.getCopyMessageFlowable().subscribe({ this.copyMessageToClipboard(it) }, RxErrorHandler.handleEmptyError()))
            }
        }, RxErrorHandler.handleEmptyError()))

        binding?.chatBarView?.sendAction = { sendMessage(it) }
        binding?.chatBarView?.maxChatLength = configManager.maxChatLength()

        binding?.chatBarView?.hasAcceptedGuidelines = true
    }

    override fun onResume() {
        if (replyToUserUUID?.isNotBlank() != true && chatRoomUser?.isNotBlank() != true) {
            parentFragmentManager.popBackStack()
        }
        startAutoRefreshing()
        super.onResume()
    }

    override fun onAttach(context: Context) {
        view?.invalidate()
        view?.forceLayout()

        super.onAttach(context)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        stopAutoRefreshing()
    }

    override fun onPause() {
        super.onPause()
        stopAutoRefreshing()
    }


    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.activity?.menuInflater?.inflate(R.menu.inbox_chat, menu)
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

    private fun startAutoRefreshing() {
        if (refreshDisposable != null && refreshDisposable?.isDisposed != true) {
            refreshDisposable?.dispose()
        }
        refreshDisposable = Observable.interval(30, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    refreshConversation()
                }, RxErrorHandler.handleEmptyError())
        refreshConversation()
    }

    private fun stopAutoRefreshing() {
        if (refreshDisposable?.isDisposed != true) {
            refreshDisposable?.dispose()
            refreshDisposable = null
        }
    }

    private fun refreshConversation() {
        if (viewModel?.memberID?.isNotBlank() != true) { return }
        compositeSubscription.add(this.socialRepository.retrieveInboxMessages(replyToUserUUID ?: "", 0)
                .subscribe({}, RxErrorHandler.handleEmptyError(), {
                    viewModel?.invalidateDataSource()
                }))
    }

    private fun sendMessage(chatText: String) {
        viewModel?.memberID?.let {userID ->
            socialRepository.postPrivateMessage(userID, chatText)
                    .delay(200, TimeUnit.MILLISECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        binding?.recyclerView?.scrollToPosition(0)
                        viewModel?.invalidateDataSource()
            }, { error ->
                        RxErrorHandler.reportError(error)
                        if (binding != null) {
                            val alert = HabiticaAlertDialog(binding!!.chatBarView.context)
                            alert.setTitle("You cannot reply to this conversation")
                            alert.setMessage("This user is unable to receive your private message")
                            alert.addOkButton()
                            alert.show()
                        }
                        binding?.chatBarView?.message = chatText
                    })
            KeyboardUtil.dismissKeyboard(getActivity())
        }
    }

    private fun setReceivingUser(chatRoomUser: String?, replyToUserUUID: String?) {
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
                    .setPositiveButton(R.string.yes) { _, _ -> socialRepository.deleteMessage(chatMessage).subscribe({ }, RxErrorHandler.handleEmptyError()) }
                    .setNegativeButton(R.string.no, null).show()
        }
    }

    private fun openProfile() {
        replyToUserUUID?.let { FullProfileActivity.open(it) }
    }
}
