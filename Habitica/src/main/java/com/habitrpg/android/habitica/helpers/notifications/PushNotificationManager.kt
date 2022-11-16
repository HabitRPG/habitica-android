package com.habitrpg.android.habitica.helpers.notifications

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.helpers.AmplitudeManager
import com.habitrpg.android.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.models.user.User
import kotlinx.coroutines.MainScope

class PushNotificationManager(
    var apiClient: ApiClient,
    private val sharedPreferences: SharedPreferences,
    private val context: Context
) {

    var refreshedToken: String = ""
        set(value) {
            if (value.isEmpty()) {
                return
            }

            field = value
            sharedPreferences.edit {
                putString(DEVICE_TOKEN_PREFERENCE_KEY, value)
            }
        }
    private var user: User? = null

    fun setUser(user: User) {
        this.user = user
    }

    fun addPushDeviceUsingStoredToken() {
        if (refreshedToken.isNotBlank()) {
            addRefreshToken()
        } else {
            FirebaseMessaging.getInstance().token.addOnCompleteListener {
                refreshedToken = it.result
                addRefreshToken()
            }
        }
    }

    private fun addRefreshToken() {
        if (this.refreshedToken.isEmpty() || this.user == null || this.userHasPushDevice()) {
            return
        }
        val pushDeviceData = HashMap<String, String>()
        pushDeviceData["regId"] = this.refreshedToken
        pushDeviceData["type"] = "android"
        MainScope().launchCatching {
            apiClient.addPushDevice(pushDeviceData)
        }
    }

    fun removePushDeviceUsingStoredToken() {
        if (this.refreshedToken.isEmpty() || !userHasPushDevice()) {
            return
        }
        MainScope().launchCatching {
            apiClient.deletePushDevice(refreshedToken)
        }
    }

    private fun userHasPushDevice(): Boolean {
        for (pushDevice in this.user?.pushDevices ?: emptyList()) {
            if (pushDevice.regId == this.refreshedToken) {
                return true
            }
        }
        return this.user?.pushDevices == null
    }

    private fun userIsSubscribedToNotificationType(type: String?): Boolean {
        val key = when {
            type == PARTY_INVITE_PUSH_NOTIFICATION_KEY -> "preference_push_invited_to_party"
            type?.contains(RECEIVED_PRIVATE_MESSAGE_PUSH_NOTIFICATION_KEY) == true -> "preference_push_received_a_private_message"
            type?.contains(RECEIVED_GEMS_PUSH_NOTIFICATION_KEY) == true -> "preference_push_gifted_gems"
            type?.contains(RECEIVED_SUBSCRIPTION_GIFT_PUSH_NOTIFICATION_KEY) == true -> "preference_push_gifted_subscription"
            type?.contains(GUILD_INVITE_PUSH_NOTIFICATION_KEY) == true -> "preference_push_invited_to_guild"
            type?.contains(QUEST_INVITE_PUSH_NOTIFICATION_KEY) == true -> "preference_push_invited_to_quest"
            type?.contains(QUEST_BEGUN_PUSH_NOTIFICATION_KEY) == true -> "preference_push_your_quest_has_begun"
            type?.contains(WON_CHALLENGE_PUSH_NOTIFICATION_KEY) == true -> "preference_push_you_won_challenge"
            else -> return true
        }

        return sharedPreferences.getBoolean(key, true)
    }

    companion object {
        const val PARTY_INVITE_PUSH_NOTIFICATION_KEY = "invitedParty"
        const val RECEIVED_PRIVATE_MESSAGE_PUSH_NOTIFICATION_KEY = "newPM"
        const val RECEIVED_GEMS_PUSH_NOTIFICATION_KEY = "giftedGems"
        const val RECEIVED_SUBSCRIPTION_GIFT_PUSH_NOTIFICATION_KEY = "giftedSubscription"
        const val GUILD_INVITE_PUSH_NOTIFICATION_KEY = "invitedGuild"
        const val QUEST_INVITE_PUSH_NOTIFICATION_KEY = "questInvitation"
        const val QUEST_BEGUN_PUSH_NOTIFICATION_KEY = "questStarted"
        const val WON_CHALLENGE_PUSH_NOTIFICATION_KEY = "wonChallenge"
        const val CHANGE_USERNAME_PUSH_NOTIFICATION_KEY = "changeUsername"
        const val GIFT_ONE_GET_ONE_PUSH_NOTIFICATION_KEY = "gift1get1"
        const val CHAT_MENTION_NOTIFICATION_KEY = "chatMention"
        const val GROUP_ACTIVITY_NOTIFICATION_KEY = "groupActivity"
        const val G1G1_PROMO_KEY = "g1g1Promo"
        private const val DEVICE_TOKEN_PREFERENCE_KEY = "device-token-preference"

        fun displayNotification(remoteMessage: RemoteMessage, context: Context, pushNotificationManager: PushNotificationManager? = null) {
            val remoteMessageIdentifier = remoteMessage.data["identifier"]

            val notificationFactory = HabiticaLocalNotificationFactory()
            val notification = notificationFactory.build(remoteMessageIdentifier,
                context
            )
            if (pushNotificationManager?.userIsSubscribedToNotificationType(remoteMessageIdentifier) != false) {
                if (remoteMessage.data.containsKey("sendAnalytics")) {
                    val additionalData = HashMap<String, Any>()
                    additionalData["identifier"] = remoteMessageIdentifier ?: ""
                    AmplitudeManager.sendEvent(
                        "receive notification",
                        AmplitudeManager.EVENT_CATEGORY_BEHAVIOUR,
                        AmplitudeManager.EVENT_HITTYPE_EVENT,
                        additionalData
                    )
                }
                notification.setExtras(remoteMessage.data)
                notification.notifyLocally(remoteMessage.data["title"], remoteMessage.data["body"], remoteMessage.data)
            }
        }
    }
}
