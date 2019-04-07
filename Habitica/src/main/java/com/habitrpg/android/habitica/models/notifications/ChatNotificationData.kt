package com.habitrpg.android.habitica.models.notifications

import io.realm.RealmObject

open class ChatNotificationData : RealmObject(), GlobalNotificationData {
    var group: NotificationGroup? = null
}
