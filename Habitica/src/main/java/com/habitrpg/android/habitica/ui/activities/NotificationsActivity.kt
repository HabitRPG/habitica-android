package com.habitrpg.android.habitica.ui.activities

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
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

    override fun getLayoutResId(): Int = R.layout.activity_notifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(toolbar)

        viewModel = ViewModelProviders.of(this)
                .get(NotificationsViewModel::class.java)

        compositeSubscription.add(viewModel.getNotifications().subscribe(Consumer {
            this.setNotifications(it)
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

        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater
        if (notifications.isEmpty()) {
            val no_notifications = inflater?.inflate(R.layout.no_notifications, notification_items, false)
            notification_items.addView(no_notifications)
            return
        }

        val header = inflater?.inflate(R.layout.notifications_header, notification_items, false)
        val badge = header?.findViewById(R.id.notificationsTitleBadge) as? TextView
        badge?.setText(notifications.count().toString())
        notification_items.addView(header)

        //TODO("not implemented")
    }
}
