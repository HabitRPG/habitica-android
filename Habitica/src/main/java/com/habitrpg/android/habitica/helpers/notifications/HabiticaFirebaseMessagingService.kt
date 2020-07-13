package com.habitrpg.android.habitica.helpers.notifications

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.habitrpg.android.habitica.HabiticaApplication
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.components.UserComponent
import java.util.*
import javax.inject.Inject

class HabiticaFirebaseMessagingService : FirebaseMessagingService() {

    private val userComponent: UserComponent?
    get() = HabiticaBaseApplication.userComponent

    @Inject
    internal lateinit var pushNotificationManager: PushNotificationManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        userComponent?.inject(this)
        pushNotificationManager.displayNotification(remoteMessage)
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        userComponent?.inject(this)
        val refreshedToken = FirebaseInstanceId.getInstance().token
        if (refreshedToken != null) {
            pushNotificationManager.refreshedToken = refreshedToken
        }
    }
}