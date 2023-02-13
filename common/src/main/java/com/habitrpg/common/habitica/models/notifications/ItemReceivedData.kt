package com.habitrpg.common.habitica.models.notifications

open class ItemReceivedData : NotificationData {
    var title: String? = null
    var text: String? = null
    var icon: String? = null
    var destination: String? = null
}
