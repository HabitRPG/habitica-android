package com.habitrpg.android.habitica.helpers

import android.content.Intent
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.common.habitica.helpers.MainNavigationController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

class NotificationOpenHandler {

    companion object {

        fun handleOpenedByNotification(identifier: String, intent: Intent) {
            MainScope().launch(context = Dispatchers.Main) {
                when (identifier) {
                    PushNotificationManager.PARTY_INVITE_PUSH_NOTIFICATION_KEY -> openNoPartyScreen()
                    PushNotificationManager.QUEST_BEGUN_PUSH_NOTIFICATION_KEY -> openPartyScreen()
                    PushNotificationManager.QUEST_INVITE_PUSH_NOTIFICATION_KEY -> openPartyScreen()
                    PushNotificationManager.GUILD_INVITE_PUSH_NOTIFICATION_KEY -> openGuildDetailScreen(intent.getStringExtra("groupID"))
                    PushNotificationManager.RECEIVED_PRIVATE_MESSAGE_PUSH_NOTIFICATION_KEY -> openPrivateMessageScreen(intent.getStringExtra("replyToUUID"), intent.getStringExtra("replyToUsername"))
                    PushNotificationManager.CHANGE_USERNAME_PUSH_NOTIFICATION_KEY -> openSettingsScreen()
                    PushNotificationManager.GIFT_ONE_GET_ONE_PUSH_NOTIFICATION_KEY -> openSubscriptionScreen()
                    PushNotificationManager.CHAT_MENTION_NOTIFICATION_KEY -> handleChatMessage(intent.getStringExtra("type"), intent.getStringExtra("groupID"))
                    PushNotificationManager.GROUP_ACTIVITY_NOTIFICATION_KEY -> handleChatMessage(intent.getStringExtra("type"), intent.getStringExtra("groupID"))
                    PushNotificationManager.G1G1_PROMO_KEY -> openGiftOneGetOneInfoScreen()
                    else -> {
                        intent.getStringExtra("openURL")?.let {
                            MainNavigationController.navigate(it)
                        }
                    }
                }
            }
        }

        private fun openSubscriptionScreen() {
            MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", true)))
        }

        private fun openPrivateMessageScreen(userID: String?, userName: String?) {
            if (userID != null && userName != null) {
                MainNavigationController.navigate(R.id.inboxMessageListFragment, bundleOf("userID" to userID, "username" to userName))
            } else {
                MainNavigationController.navigate(R.id.inboxFragment)
            }
        }

        private fun openPartyScreen(isChatNotification: Boolean = false) {
            val tabToOpen = if (isChatNotification) 1 else 0
            MainNavigationController.navigate(R.id.partyFragment, bundleOf("tabToOpen" to tabToOpen))
        }

        private fun openNoPartyScreen() {
            MainNavigationController.navigate(R.id.noPartyFragment)
        }

        private fun openGiftOneGetOneInfoScreen() {
            MainNavigationController.navigate(R.id.subscriptionPurchaseActivity)
        }

        private fun openQuestDetailSCreen() {
            MainNavigationController.navigate(R.id.questDetailFragment)
        }

        private fun openGuildDetailScreen(groupID: String?) {
            MainNavigationController.navigate(R.id.guildFragment, bundleOf("groupID" to groupID))
        }

        private fun openSettingsScreen() {
            MainNavigationController.navigate(R.id.prefsActivity)
        }

        private fun handleChatMessage(type: String?, groupID: String?) {
            when (type) {
                "party" -> openPartyScreen()
                "guild" -> openGuildDetailScreen(groupID)
            }
        }
    }
}
