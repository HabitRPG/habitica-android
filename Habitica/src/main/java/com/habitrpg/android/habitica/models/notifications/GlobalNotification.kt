package com.habitrpg.android.habitica.models.notifications

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

enum class NotificationType(val type: String) {
    NEW_CHAT_MESSAGE("NEW_CHAT_MESSAGE");

    companion object {
        fun contains(type: String?): Boolean {
            return NotificationType.values().map { it.type }.contains(type)
        }
    }
}

/**
 * Represents Habitica "Global notifications", i.e. the notifications about chat messages
 * (in Guilds and Party), Party & Quest invitations, unallocated stat points etc.
 *
 * These are different from other kind of notifications, such as Push notifications and
 * Popup notifications.
 */
open class GlobalNotification : RealmObject() {

    @PrimaryKey
    var id: String = ""

    var type: String? = null
    var seen: Boolean? = null

    var newChatMessageData: ChatNotificationData? = null

    // Workaround for Realms lack of polymorphism
    fun getData(): GlobalNotificationData? {
        return when(type) {
            NotificationType.NEW_CHAT_MESSAGE.type -> newChatMessageData
            else -> null
        }
    }
}
