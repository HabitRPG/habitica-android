package com.habitrpg.android.habitica.ui.viewmodels

import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.notifications.GlobalNotification
import com.habitrpg.android.habitica.models.notifications.NewChatMessageData
import com.habitrpg.android.habitica.models.notifications.NotificationType
import com.habitrpg.android.habitica.models.social.UserParty
import com.playseeds.android.sdk.inappmessaging.Log
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.realm.RealmList


open class NotificationsViewModel : BaseViewModel() {
    var party: UserParty? = null

    override fun inject(component: AppComponent) {
        component.inject(this)
    }

    fun getNotifications(): Flowable<List<GlobalNotification>> {
        return userRepository.getUser()
                .doOnEach { party = it.value?.party }
                .map { filterSupportedTypes(it.notifications) }
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun getNotificationCount(): Flowable<Int> {
        return getNotifications()
                .map { it.count() }
                .distinctUntilChanged()
    }

    fun allNotificationsSeen(): Flowable<Boolean> {
        return getNotifications()
                .map { it.all { notification -> notification.seen == true } }
                .distinctUntilChanged()
    }

    fun refreshNotifications(): Flowable<RealmList<GlobalNotification>> {
        return userRepository.retrieveUser(withTasks = false, forced = true).map { it.notifications }
    }

    private fun filterSupportedTypes(notifications: List<GlobalNotification>): List<GlobalNotification> {
        return notifications.filter { NotificationType.contains(it.type) }
    }

    fun isPartyMessage(data: NewChatMessageData?): Boolean {
        if (party == null || data?.group?.id == null) {
            return false
        }

        return party?.id == data.group?.id
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
