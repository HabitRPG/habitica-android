package com.habitrpg.android.habitica.ui.fragments.social

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.habitrpg.android.habitica.BuildConfig
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.DialogChooseMessageRecipientBinding
import com.habitrpg.android.habitica.databinding.FragmentInboxBinding
import com.habitrpg.android.habitica.extensions.getAgoString
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.InboxConversation
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.common.habitica.views.AvatarView
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import javax.inject.Inject

class InboxOverviewFragment : BaseMainFragment<FragmentInboxBinding>(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    @Inject
    lateinit var socialRepository: SocialRepository
    @Inject
    lateinit var configManager: AppConfigManager
    @Inject
    lateinit var userViewModel: MainUserViewModel

    override var binding: FragmentInboxBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentInboxBinding {
        return FragmentInboxBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.hidesToolbar = true
        compositeSubscription.add(this.socialRepository.markPrivateMessagesRead(null).subscribe({ }, RxErrorHandler.handleEmptyError()))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.inboxRefreshLayout?.setOnRefreshListener(this)

        userViewModel.user.observe(viewLifecycleOwner) {
            binding?.optOutView?.visibility = if (it?.inbox?.optOut == true) View.VISIBLE else View.GONE
        }

        loadMessages()
        retrieveMessages()
    }

    private fun loadMessages() {
        compositeSubscription.add(
            socialRepository.getInboxConversations().subscribe(
                {
                    setInboxMessages(it)
                },
                RxErrorHandler.handleEmptyError()
            )
        )
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.activity?.menuInflater?.inflate(R.menu.inbox, menu)
        val item = menu.findItem(R.id.send_message)
        tintMenuIcon(item)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.send_message -> {
                openNewMessageDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openNewMessageDialog() {
        if (BuildConfig.DEBUG && this.activity == null) {
            error("Assertion failed")
        }
        val binding = DialogChooseMessageRecipientBinding.inflate(layoutInflater)
        this.activity?.let { thisActivity ->
            val alert = HabiticaAlertDialog(thisActivity)
            alert.setTitle(getString(R.string.choose_recipient_title))
            alert.addButton(
                getString(R.string.action_continue),
                true,
                isDestructive = false,
                autoDismiss = false
            ) { _, _ ->
                binding.errorTextView.visibility = View.GONE
                binding.progressCircular.visibility = View.VISIBLE
                val username = binding.uuidEditText.text?.toString() ?: ""
                socialRepository.getMemberWithUsername(username)
                    .subscribe(
                        {
                            alert.dismiss()
                            openInboxMessages("", username)
                            binding.progressCircular.visibility = View.GONE
                        },
                        {
                            binding.errorTextView.visibility = View.VISIBLE
                            binding.progressCircular.visibility = View.GONE
                        }
                    )
            }
            alert.addButton(getString(R.string.action_cancel), false) { _, _ ->
                thisActivity.dismissKeyboard()
            }
            alert.setAdditionalContentView(binding.root)
            binding.uuidEditText.requestFocus()
            alert.show()
        }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun retrieveMessages() {
        compositeSubscription.add(
            this.socialRepository.retrieveInboxConversations()
                .subscribe(
                    {
                        binding?.inboxRefreshLayout?.isRefreshing = false
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
    }

    override fun onRefresh() {
        binding?.inboxRefreshLayout?.isRefreshing = true
        retrieveMessages()
    }

    private fun setInboxMessages(messages: List<InboxConversation>) {
        if (binding?.inboxMessages == null) {
            return
        }

        binding?.inboxMessages?.removeAllViewsInLayout()

        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        if (messages.isNotEmpty()) {
            for (message in messages) {
                val entry = inflater?.inflate(R.layout.item_inbox_overview, binding?.inboxMessages, false)
                val avatarView = entry?.findViewById(R.id.avatar_view) as? AvatarView
                message.userStyles?.let { avatarView?.setAvatar(it) }
                val displayNameTextView = entry?.findViewById(R.id.display_name_textview) as? UsernameLabel
                displayNameTextView?.username = message.user
                displayNameTextView?.tier = message.contributor?.level ?: 0
                val timestampTextView = entry?.findViewById(R.id.timestamp_textview) as? TextView
                timestampTextView?.text = message.timestamp?.getAgoString(resources)
                val usernameTextView = entry?.findViewById(R.id.username_textview) as? TextView
                if (message.username?.isNotEmpty() == true) {
                    usernameTextView?.text = message.formattedUsername
                    usernameTextView?.visibility = View.VISIBLE
                } else {
                    usernameTextView?.visibility = View.GONE
                }
                val messageTextView = entry?.findViewById(R.id.message_textview) as? TextView
                messageTextView?.text = message.text
                entry?.tag = message.uuid
                entry?.setOnClickListener(this)
                binding?.inboxMessages?.addView(entry)
            }
        } else {
            val tv = TextView(context)
            tv.setText(R.string.empty_inbox)
        }
    }

    override fun onClick(v: View) {
        val displaynameView = v.findViewById(R.id.display_name_textview) as? UsernameLabel
        val replyToUserName = displaynameView?.username ?: ""
        openInboxMessages(v.tag.toString(), replyToUserName)
    }

    private fun openInboxMessages(userID: String, username: String) {
        MainNavigationController.navigate(InboxOverviewFragmentDirections.openInboxDetail(userID, username))
    }
}
