package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class UserAchievement : RealmObject(), BaseObject {

    @PrimaryKey
    var combinedKey: String? = null
    var userId: String? = null
        set(value) {
            field = value
            combinedKey = field + key
        }
    var key: String? = null
        set(value) {
            field = value
            combinedKey = userId + field
        }
    var earned: Boolean = false
}

