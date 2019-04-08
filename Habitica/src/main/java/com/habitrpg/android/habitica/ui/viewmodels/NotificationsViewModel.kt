package com.habitrpg.android.habitica.ui.viewmodels

import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.models.notifications.GlobalNotification
import com.habitrpg.android.habitica.models.notifications.NotificationType
import com.playseeds.android.sdk.inappmessaging.Log
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.realm.RealmList


open class NotificationsViewModel : BaseViewModel() {
    override fun inject(component: AppComponent) {
        component.inject(this)
    }

    fun getNotifications(): Flowable<List<GlobalNotification>> {
        return userRepository.getUser()
                .map { filterSupportedTypes(it.notifications) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNotificationCount(): Flowable<Int> {
        return getNotifications()
                .map { it.count() }
    }

    fun refreshNotifications(): Flowable<RealmList<GlobalNotification>> {
        return userRepository.retrieveUser(false, true).map { it.notifications }
    }

    private fun filterSupportedTypes(notifications: List<GlobalNotification>): List<GlobalNotification> {
        return notifications.filter { NotificationType.contains(it.type) }
    }

    fun dismissNotification(notification: GlobalNotification) {
        Log.d("NotificationsViewModel.dismissNotification " + notification.type + " " + notification.id)
        //TODO("not implemented")
    }

    fun dismissAllNotifications() {
        Log.d("NotificationsViewModel.dismissAllNotifications")
        //TODO("not implemented")
    }

    fun markNotificationsAsSeen() {
        Log.d("NotificationsViewModel.markNotificationsAsSeen")
        //TODO("not implemented")
    }

}