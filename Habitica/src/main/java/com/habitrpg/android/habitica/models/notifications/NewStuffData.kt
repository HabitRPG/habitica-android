package com.habitrpg.android.habitica.models.notifications

import io.realm.RealmObject

open class NewStuffData : RealmObject(), GlobalNotificationData {
    var title: String? = null
}
