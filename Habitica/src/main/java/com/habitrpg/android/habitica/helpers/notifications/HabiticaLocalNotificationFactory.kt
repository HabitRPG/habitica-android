package com.habitrpg.android.habitica.helpers.notifications

import android.content.Context

class HabiticaLocalNotificationFactory {
    // use getShape method to get object of type shape
    fun build(notificationType: String?, context: Context?): HabiticaLocalNotification {
        return when {
            PushNotificationManager.PARTY_INVITE_PUSH_NOTIFICATION_KEY.equals(notificationType, true) -> {
                PartyInviteLocalNotification(context!!, notificationType)
            }
            PushNotificationManager.RECEIVED_PRIVATE_MESSAGE_PUSH_NOTIFICATION_KEY.equals(notificationType, true) -> {
                ReceivedPrivateMessageLocalNotification(context!!, notificationType)
            }
            PushNotificationManager.RECEIVED_GEMS_PUSH_NOTIFICATION_KEY.equals(notificationType, true) -> {
                ReceivedGemsGiftLocalNotification(context!!, notificationType)
            }
            PushNotificationManager.RECEIVED_SUBSCRIPTION_GIFT_PUSH_NOTIFICATION_KEY.equals(notificationType, true) -> {
                ReceivedSubscriptionGiftLocalNotification(context!!, notificationType)
            }
            PushNotificationManager.GUILD_INVITE_PUSH_NOTIFICATION_KEY.equals(notificationType, true) -> {
                GuildInviteLocalNotification(context!!, notificationType)
            }
            PushNotificationManager.QUEST_INVITE_PUSH_NOTIFICATION_KEY.equals(notificationType, true) -> {
                QuestInviteLocalNotification(context!!, notificationType)
            }
            PushNotificationManager.QUEST_BEGUN_PUSH_NOTIFICATION_KEY.equals(notificationType, true) -> {
                QuestBegunLocalNotification(context!!, notificationType)
            }
            PushNotificationManager.WON_CHALLENGE_PUSH_NOTIFICATION_KEY.equals(notificationType, true) -> {
                WonChallengeLocalNotification(context!!, notificationType)
            }
            PushNotificationManager.CHANGE_USERNAME_PUSH_NOTIFICATION_KEY.equals(notificationType, true) -> {
                ChangeUsernameLocalNotification(context!!, notificationType)
            }
            PushNotificationManager.GIFT_ONE_GET_ONE_PUSH_NOTIFICATION_KEY.equals(notificationType, true) -> {
                GiftOneGetOneLocalNotification(context!!, notificationType)
            }
            PushNotificationManager.CHAT_MENTION_NOTIFICATION_KEY.equals(notificationType, true) -> {
                ChatMentionNotification(context!!, notificationType)
            }
            PushNotificationManager.GROUP_ACTIVITY_NOTIFICATION_KEY.equals(notificationType, true) -> {
                GroupActivityNotification(context!!, notificationType)
            }
            else -> {
                GenericLocalNotification(context!!, notificationType)
            }
        }
    }
}
