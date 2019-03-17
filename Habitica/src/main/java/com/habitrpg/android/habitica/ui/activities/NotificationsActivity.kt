package com.habitrpg.android.habitica.ui.activities

import android.os.Bundle
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.notifications.GlobalNotification
import com.habitrpg.android.habitica.models.user.User
import io.reactivex.functions.Consumer
import io.realm.RealmList
import kotlinx.android.synthetic.main.activity_notifications.*
import javax.inject.Inject

class NotificationsActivity : BaseActivity(), androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener {
    @Inject
    lateinit var userRepository: UserRepository

    override fun getLayoutResId(): Int = R.layout.activity_notifications

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupToolbar(toolbar)

        compositeSubscription.add(userRepository.getUser().subscribe(Consumer<User> {
            this.setNotifications(it.notifications)
        }, RxErrorHandler.handleEmptyError()))

        notifications_refresh_layout?.setOnRefreshListener(this)
    }

    override fun onDestroy() {
        userRepository.close()
        super.onDestroy()
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

        compositeSubscription.add(userRepository.retrieveUser(false, true).subscribe(Consumer<User> {
            notifications_refresh_layout.isRefreshing = false
        }, RxErrorHandler.handleEmptyError()))
    }

    private fun setNotifications(notifications: RealmList<GlobalNotification>) {
        //TODO("not implemented")
    }
}
