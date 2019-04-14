package com.habitrpg.android.habitica.models.notifications

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.lang.reflect.Type

enum class NotificationType(val type: String) {
    NEW_STUFF("NEW_STUFF"),
    NEW_CHAT_MESSAGE("NEW_CHAT_MESSAGE"),
    NEW_MYSTERY_ITEMS("NEW_MYSTERY_ITEMS"),
    UNALLOCATED_STATS_POINTS("UNALLOCATED_STATS_POINTS");

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

    var newStuffData: NewStuffData? = null
    var newChatMessageData: NewChatMessageData? = null
    var unallocatedPointsData: UnallocatedPointsData? = null

    // Workaround for Realms lack of polymorphism
    fun getData(): GlobalNotificationData? {
        return when (type) {
            NotificationType.NEW_STUFF.type -> newStuffData
            NotificationType.NEW_CHAT_MESSAGE.type -> newChatMessageData
            NotificationType.UNALLOCATED_STATS_POINTS.type -> unallocatedPointsData
            else -> null
        }
    }

    fun setData(data: GlobalNotificationData?) {
        when (type) {
            NotificationType.NEW_STUFF.type -> newStuffData = data as NewStuffData?
            NotificationType.NEW_CHAT_MESSAGE.type -> newChatMessageData = data as NewChatMessageData?
            NotificationType.UNALLOCATED_STATS_POINTS.type -> unallocatedPointsData = data as UnallocatedPointsData?
        }
    }

    fun getDataType(): Type? {
        return when (type) {
            NotificationType.NEW_STUFF.type -> NewStuffData::class.java
            NotificationType.NEW_CHAT_MESSAGE.type -> NewChatMessageData::class.java
            NotificationType.UNALLOCATED_STATS_POINTS.type -> UnallocatedPointsData::class.java
            else -> null
        }
    }
}
