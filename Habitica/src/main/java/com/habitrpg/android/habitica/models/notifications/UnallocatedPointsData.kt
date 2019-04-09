package com.habitrpg.android.habitica.models.notifications

import io.realm.RealmObject

open class UnallocatedPointsData : RealmObject(), GlobalNotificationData {
    var points: Int? = null
}
