package com.habitrpg.android.habitica.helpers.notifications

import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.components.UserComponent
import javax.inject.Inject

class HabiticaFirebaseMessagingService : FirebaseMessagingService() {

    private val userComponent: UserComponent?
        get() = HabiticaBaseApplication.userComponent

    @Inject
    internal lateinit var pushNotificationManager: PushNotificationManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        try {
            userComponent?.inject(this)
        } catch (_: java.lang.IllegalStateException) {
        }
        PushNotificationManager.displayNotification(remoteMessage, applicationContext)

        if (remoteMessage.data["identifier"]?.contains(PushNotificationManager.WON_CHALLENGE_PUSH_NOTIFICATION_KEY) == true) {
            if (this::userRepository.isInitialized) {
                // userRepository.retrieveUser(true).subscribe({}, RxErrorHandler.handleEmptyError())
            }
        }
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        userComponent?.inject(this)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            val refreshedToken = task.result
            if (refreshedToken != null && this::pushNotificationManager.isInitialized) {
                pushNotificationManager.refreshedToken = refreshedToken
            }
        }
    }
}
