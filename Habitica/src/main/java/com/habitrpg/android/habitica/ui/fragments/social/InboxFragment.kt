package com.habitrpg.android.habitica.ui.fragments.social

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.dismissKeyboard
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_inbox.*
import javax.inject.Inject
import javax.inject.Named

class InboxFragment : BaseMainFragment(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    @Inject
    lateinit var socialRepository: SocialRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String
    @Inject
    lateinit var configManager: AppConfigManager

    private var chooseRecipientDialogView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        this.hidesToolbar = true
        super.onCreateView(inflater, container, savedInstanceState)

        compositeSubscription.add(this.socialRepository.markPrivateMessagesRead(user).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))

        return inflater.inflate(R.layout.fragment_inbox, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inbox_refresh_layout?.setOnRefreshListener(this)

        loadMessages()
    }

    private fun loadMessages() {
        compositeSubscription.add(socialRepository.getInboxOverviewList().subscribe(Consumer<RealmResults<ChatMessage>> {
            setInboxMessages(it)
        }, RxErrorHandler.handleEmptyError()))
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.send_message -> {
                openNewMessageDialog()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openNewMessageDialog() {
        assert(this.activity != null)
        this.chooseRecipientDialogView = this.activity?.layoutInflater?.inflate(R.layout.dialog_choose_message_recipient, null)

        this.activity?.let { thisActivity ->
            val alert = HabiticaAlertDialog(thisActivity)
            alert.setTitle(getString(R.string.choose_recipient_title))
            alert.addButton(getString(R.string.action_continue), true) { _, _ ->
                    val uuidEditText = chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as? EditText
                    openInboxMessages(uuidEditText?.text?.toString() ?: "", "")
                }
            alert.addButton(getString(R.string.action_cancel), false) { dialog, _ ->
                    thisActivity.dismissKeyboard()
                }
            alert.setAdditionalContentView(chooseRecipientDialogView)
            alert.show()
        }

    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onRefresh() {
        inbox_refresh_layout.isRefreshing = true
        compositeSubscription.add(this.socialRepository.retrieveInboxMessages()
                .subscribe(Consumer<List<ChatMessage>> {
                    inbox_refresh_layout.isRefreshing = false
                }, RxErrorHandler.handleEmptyError()))
    }

    private fun setInboxMessages(messages: RealmResults<ChatMessage>) {
        if (inbox_messages == null) {
            return
        }

        inbox_messages.removeAllViewsInLayout()

        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        if (messages.isNotEmpty()) {
            for (message in messages) {
                val entry = inflater?.inflate(R.layout.item_inbox_overview, inbox_messages, false)
                val avatarView = entry?.findViewById(R.id.avatar_view) as? AvatarView
                //message.userStyles?.let { avatarView?.setAvatar(it) }
                avatarView?.visibility = View.GONE
                val displayNameTextView = entry?.findViewById(R.id.display_name_textview) as? UsernameLabel
                displayNameTextView?.username = message.user
                displayNameTextView?.tier = message.contributor?.level ?: 0
                val timestampTextView = entry?.findViewById(R.id.timestamp_textview) as? TextView
                timestampTextView?.text = message.getAgoString(resources)
                val usernameTextView = entry?.findViewById(R.id.username_textview) as? TextView
                if (message.username != null) {
                    usernameTextView?.text = message.formattedUsername
                    usernameTextView?.visibility = View.VISIBLE
                } else {
                    usernameTextView?.visibility = View.GONE
                }
                val messageTextView = entry?.findViewById(R.id.message_textview) as? TextView
                messageTextView?.text = message.text
                entry?.tag = message.uuid
                entry?.setOnClickListener(this)
                inbox_messages.addView(entry)
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
        MainNavigationController.navigate(InboxFragmentDirections.openInboxDetail(userID, username))
    }

}
