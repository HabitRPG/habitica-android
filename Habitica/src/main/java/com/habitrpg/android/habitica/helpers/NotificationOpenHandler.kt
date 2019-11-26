package com.habitrpg.android.habitica.helpers

import android.content.Intent
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.notifications.PushNotificationManager
import com.habitrpg.android.habitica.models.user.User

class NotificationOpenHandler {

    companion object {

        fun handleOpenedByNotification(identifier: String, intent: Intent, user: User?) {
            when (identifier) {
                PushNotificationManager.PARTY_INVITE_PUSH_NOTIFICATION_KEY -> openPartyScreen()
                PushNotificationManager.QUEST_BEGUN_PUSH_NOTIFICATION_KEY -> openQuestDetailSCreen(user?.party?.id,
                        user?.party?.quest?.key)
                PushNotificationManager.QUEST_INVITE_PUSH_NOTIFICATION_KEY -> openQuestDetailSCreen(user?.party?.id,
                        user?.party?.quest?.key)
                PushNotificationManager.GUILD_INVITE_PUSH_NOTIFICATION_KEY -> openGuildDetailScreen(intent.getStringExtra("groupID"))
                PushNotificationManager.RECEIVED_PRIVATE_MESSAGE_PUSH_NOTIFICATION_KEY -> openPrivateMessageScreen(intent.getStringExtra("replyTo"))
                PushNotificationManager.CHANGE_USERNAME_PUSH_NOTIFICATION_KEY -> openSettingsScreen()
                PushNotificationManager.GIFT_ONE_GET_ONE_PUSH_NOTIFICATION_KEY -> openSubscriptionScreen()
                PushNotificationManager.CHAT_MENTION_NOTIFICATION_KEY -> handleChatMessage(intent.getStringExtra("type"), intent.getStringExtra("groupID"))
                PushNotificationManager.GROUP_ACTIVITY_NOTIFICATION_KEY -> handleChatMessage(intent.getStringExtra("type"), intent.getStringExtra("groupID"))
            }
        }

        private fun openSubscriptionScreen() {
            MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", true)))
        }

        private fun openPrivateMessageScreen(userID: String?) {
            if (userID != null) {
                MainNavigationController.navigate(R.id.inboxMessageListFragment, bundleOf("userID" to userID))
            } else {
                MainNavigationController.navigate(R.id.inboxFragment)
            }
        }

        private fun openPartyScreen() {
            MainNavigationController.navigate(R.id.partyFragment)
        }

        private fun openQuestDetailSCreen(partyId: String?, questKey: String?) {
            if (partyId == null || questKey == null ||partyId.isNotEmpty() || questKey.isNotEmpty()) {
                return
            }
            MainNavigationController.navigate(R.id.questDetailFragment, bundleOf("partyID" to partyId, "questKey" to questKey))
        }

        private fun openGuildDetailScreen(groupID: String) {
            if (groupID.isEmpty()) {
                return
            }
            MainNavigationController.navigate(R.id.guildFragment, bundleOf("groupID" to groupID))

        }

        private fun openSettingsScreen() {
            MainNavigationController.navigate(R.id.prefsActivity)
        }

        private fun handleChatMessage(type: String?, groupID: String) {
            when (type) {
                "party" -> MainNavigationController.navigate(R.id.partyFragment)
                "tavern" -> MainNavigationController.navigate(R.id.tavernFragment)
                "guild" -> MainNavigationController.navigate(R.id.guildFragment, bundleOf("groupID" to groupID))
            }
        }
    }
}