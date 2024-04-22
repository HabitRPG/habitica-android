package com.habitrpg.android.habitica.helpers.notifications

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HabiticaFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    internal lateinit var pushNotificationManager: PushNotificationManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        PushNotificationManager.displayNotification(remoteMessage, applicationContext)
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val refreshedToken = task.result
            if (refreshedToken != null && this::pushNotificationManager.isInitialized) {
                pushNotificationManager.refreshedToken = refreshedToken
            }
        }
    }
}
