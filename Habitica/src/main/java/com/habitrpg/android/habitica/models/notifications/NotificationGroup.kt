package com.habitrpg.android.habitica.models.notifications

import io.realm.RealmObject

open class NotificationGroup : RealmObject() {
    var id: String = ""

    var name: String? = null
}
