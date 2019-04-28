package com.habitrpg.android.habitica.ui.activities

import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.Notification
import com.habitrpg.android.habitica.models.notifications.*
import com.habitrpg.android.habitica.ui.viewmodels.NotificationsViewModel
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_notifications.*

class NotificationsActivity : BaseActivity(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    lateinit var viewModel: NotificationsViewModel

    lateinit var inflater: LayoutInflater

    override fun getLayoutResId(): Int = R.layout.activity_notifications

    private var notifications: List<Notification> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(toolbar)

        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        viewModel = ViewModelProviders.of(this)
                .get(NotificationsViewModel::class.java)

        compositeSubscription.add(viewModel.getNotifications().subscribe(Consumer {
            this.setNotifications(it)
            viewModel.markNotificationsAsSeen(it)
        }, RxErrorHandler.handleEmptyError()))

        notifications_refresh_layout?.setOnRefreshListener(this)
    }

    override fun injectActivity(component: AppComponent?) {
        component?.inject(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            onBackPressed()
            return true
        }
        return super.onSupportNavigateUp()
    }

    override fun onRefresh() {
        notifications_refresh_layout.isRefreshing = true

        compositeSubscription.add(viewModel.refreshNotifications().subscribe(Consumer {
            notifications_refresh_layout.isRefreshing = false
        }, RxErrorHandler.handleEmptyError()))
    }

    private fun setNotifications(notifications: List<Notification>) {
        this.notifications = notifications

        if (notification_items == null) {
            return
        }

        notification_items.removeAllViewsInLayout()

        when {
            notifications.isEmpty() -> displayNoNotificationsVew()
            else -> displayNotificationsListView(notifications)
        }
    }

    private fun displayNoNotificationsVew() {
        notification_items.addView(
                inflater.inflate(R.layout.no_notifications, notification_items, false)
        )
    }

    private fun displayNotificationsListView(notifications: List<Notification>) {
        notification_items.addView(
                createNotificationsHeaderView(notifications.count())
        )

        notifications.map {
            val item: View? = when (it.type) {
                Notification.Type.NEW_CHAT_MESSAGE.type -> createNewChatMessageNotification(it)
                Notification.Type.NEW_STUFF.type -> createNewStuffNotification(it)
                Notification.Type.UNALLOCATED_STATS_POINTS.type -> createUnallocatedStatsNotification(it)
                Notification.Type.NEW_MYSTERY_ITEMS.type -> createMysteryItemsNotification(it)
                //TODO rest of the notification types
                else -> null
            }

            notification_items.addView(item)
        }
    }

    private fun createNotificationsHeaderView(notificationCount: Int): View? {
        val header = inflater.inflate(R.layout.notifications_header, notification_items, false)

        val badge = header?.findViewById(R.id.notifications_title_badge) as? TextView
        badge?.text = notificationCount.toString()

        val dismissAllButton = header?.findViewById(R.id.dismiss_all_button) as? Button
        dismissAllButton?.setOnClickListener { viewModel.dismissAllNotifications(notifications) }

        return header
    }

    private fun createNewChatMessageNotification(notification: Notification): View? {
        val data = notification.data as? NewChatMessageData
        val stringId = if (viewModel.isPartyMessage(data)) R.string.new_msg_party else R.string.new_msg_guild

        return createNotificationItem(
                notification,
                fromHtml(getString(stringId, data?.group?.name))
        )
    }

    private fun createNewStuffNotification(notification: Notification): View? {
        val data = notification.data as? NewStuffData
        val text = fromHtml("<b>" + getString(R.string.new_bailey_update) + "</b><br>" + data?.title)

        return createNotificationItem(
                notification,
                text,
                R.drawable.notifications_bailey
        )
    }

    private fun createUnallocatedStatsNotification(notification: Notification): View? {
        val data = notification.data as? UnallocatedPointsData

        return createNotificationItem(
                notification,
                fromHtml(getString(R.string.unallocated_stats_points, data?.points.toString())),
                R.drawable.notification_stat_sparkles
        )
    }

    private fun createMysteryItemsNotification(notification: Notification): View? {
        return createNotificationItem(
                notification,
                fromHtml(getString(R.string.new_subscriber_item)),
                R.drawable.notification_mystery_item
        )
    }

    private fun createNotificationItem(notification: Notification, messageText: CharSequence, imageResourceId: Int? = null): View? {
        val item = inflater.inflate(R.layout.notification_item, notification_items, false)

        val dismissButton = item?.findViewById(R.id.dismiss_button) as? ImageView
        dismissButton?.setOnClickListener { viewModel.dismissNotification(notification) }

        val messageTextView = item?.findViewById(R.id.message_text) as? TextView
        messageTextView?.text = messageText

        if (imageResourceId != null) {
            val notificationImage = item?.findViewById(R.id.notification_image) as? ImageView
            notificationImage?.setImageResource(imageResourceId)
            notificationImage?.visibility = View.VISIBLE
        }

        return item
    }

    private fun fromHtml(text: String): CharSequence {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            Html.fromHtml(text)
        }
    }
}
