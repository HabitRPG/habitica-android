package com.habitrpg.android.habitica.ui.fragments.social

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AlertDialog
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.modules.AppModule
import com.habitrpg.android.habitica.prefs.scanner.IntentIntegrator
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.KeyboardUtil
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import kotlinx.android.synthetic.main.fragment_inbox.*
import javax.inject.Inject
import javax.inject.Named

class InboxFragment : BaseMainFragment(), SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {

    @Inject
    lateinit var socialRepository: SocialRepository
    @field:[Inject Named(AppModule.NAMED_USER_ID)]
    lateinit var userId: String

    private var chooseRecipientDialogView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        this.socialRepository.markPrivateMessagesRead(user).subscribe(Consumer { }, RxErrorHandler.handleEmptyError())

        return inflater.inflate(R.layout.fragment_inbox, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inbox_refresh_layout?.setOnRefreshListener(this)

        loadMessages()
    }

    private fun loadMessages() {
        userRepository.getInboxOverviewList().subscribe(Consumer<RealmResults<ChatMessage>> {
            setInboxMessages(it)
        }, RxErrorHandler.handleEmptyError())
    }

    override fun onDestroy() {
        socialRepository.close()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        this.activity?.menuInflater?.inflate(R.menu.inbox, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val id = item?.itemId

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

        val scaneQRCodeButton = chooseRecipientDialogView?.findViewById<View>(R.id.scanQRCodeButton) as Button

        this.activity.notNull { thisActivity ->
            val alert = AlertDialog.Builder(thisActivity)
            .setTitle(getString(R.string.choose_recipient_title))
                .setPositiveButton(getString(R.string.action_continue)) { _, _ ->
                    val uuidEditText = chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as EditText
                    openInboxMessages(uuidEditText.text.toString(), "")
                }
                .setNeutralButton(getString(R.string.action_cancel)) { dialog, _ ->
                    KeyboardUtil.dismissKeyboard(thisActivity)
                    dialog.cancel()
                }
                .create()
            scaneQRCodeButton.setOnClickListener {
                val scanIntegrator = IntentIntegrator(getActivity())
                scanIntegrator.initiateScan(this)
            }
            alert.setView(chooseRecipientDialogView)
            alert.show()
        }

    }

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onRefresh() {
        inbox_refresh_layout.isRefreshing = true
        this.userRepository.retrieveInboxMessages()
                .subscribe(Consumer<List<ChatMessage>> {
                    inbox_refresh_layout.isRefreshing = false
                }, RxErrorHandler.handleEmptyError())
    }

    private fun setInboxMessages(messages: RealmResults<ChatMessage>) {
        if (inbox_messages == null) {
            return
        }

        inbox_messages.removeAllViewsInLayout()

        val inflater = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        if (messages.isNotEmpty()) {
            for (message in messages) {
                val entry = inflater.inflate(R.layout.plain_list_item, inbox_messages, false) as TextView
                entry.text = message.user
                entry.tag = message.uuid
                entry.setOnClickListener(this)
                inbox_messages.addView(entry)
            }
        } else {
            val tv = TextView(context)
            tv.setText(R.string.empty_inbox)
        }
    }

    override fun onClick(v: View) {
        val entry = v as TextView
        val replyToUserName = entry.text.toString()
        openInboxMessages(entry.tag.toString(), replyToUserName)
    }

    private fun openInboxMessages(userID: String, username: String) {
        val inboxMessageListFragment = InboxMessageListFragment()
        inboxMessageListFragment.setReceivingUser(username, userID)
        this.activity?.displayFragment(inboxMessageListFragment)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val scanningResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)

        if (scanningResult != null && scanningResult.contents != null) {
            if (this.chooseRecipientDialogView != null) {
                val uuidEditText = this.chooseRecipientDialogView?.findViewById<View>(R.id.uuidEditText) as EditText
                val qrCodeUrl = scanningResult.contents
                val uri = Uri.parse(qrCodeUrl)
                if (uri == null || uri.pathSegments.size < 3) {
                    return
                }
                val userID = uri.pathSegments[2]
                uuidEditText.setText(userID)
            }
        }
    }

    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.sidebar_inbox)
    }
}
