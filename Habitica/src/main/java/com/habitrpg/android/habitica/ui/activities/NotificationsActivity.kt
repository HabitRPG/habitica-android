package com.habitrpg.android.habitica.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.ViewModelProviders
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.notifications.GlobalNotification
import com.habitrpg.android.habitica.ui.viewmodels.NotificationsViewModel
import io.reactivex.functions.Consumer
import kotlinx.android.synthetic.main.activity_notifications.*

class NotificationsActivity : BaseActivity(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {

    lateinit var viewModel: NotificationsViewModel

    lateinit var inflater: LayoutInflater

    override fun getLayoutResId(): Int = R.layout.activity_notifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(toolbar)

        inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        viewModel = ViewModelProviders.of(this)
                .get(NotificationsViewModel::class.java)

        compositeSubscription.add(viewModel.getNotifications().subscribe(Consumer {
            this.setNotifications(it)
            viewModel.markNotificationsAsSeen()
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

    private fun setNotifications(notifications: List<GlobalNotification>) {
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

    private fun displayNotificationsListView(notifications: List<GlobalNotification>) {
        notification_items.addView(
                createNotificationsHeaderView(notifications.count())
        )

        notifications.map {
            val item: View? = when (it.type) {
                //TODO("not implemented")
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
        dismissAllButton?.setOnClickListener({ viewModel.dismissAllNotifications() })

        return header
    }
}
