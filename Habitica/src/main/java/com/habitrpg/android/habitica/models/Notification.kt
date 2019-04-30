package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.models.notifications.*

class Notification {
    enum class Type(val type: String) {
        LOGIN_INCENTIVE("LOGIN_INCENTIVE"),
        NEW_STUFF("NEW_STUFF"),
        NEW_CHAT_MESSAGE("NEW_CHAT_MESSAGE"),
        NEW_MYSTERY_ITEMS("NEW_MYSTERY_ITEMS"),
        UNALLOCATED_STATS_POINTS("UNALLOCATED_STATS_POINTS");
    }

    var id: String = ""

    var type: String? = null
    var seen: Boolean? = null

    var data: NotificationData? = null

    fun getDataType(): java.lang.reflect.Type? {
        return when (type) {
            Type.LOGIN_INCENTIVE.type -> LoginIncentiveData::class.java
            Type.NEW_STUFF.type -> NewStuffData::class.java
            Type.NEW_CHAT_MESSAGE.type -> NewChatMessageData::class.java
            Type.UNALLOCATED_STATS_POINTS.type -> UnallocatedPointsData::class.java
            else -> null
        }
    }
}
