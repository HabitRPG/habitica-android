package com.habitrpg.android.habitica.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

open class QuestAchievement : RealmObject(), BaseObject {
    @PrimaryKey
    var combinedKey: String? = null

    var questKey: String? = null
        set(value) {
            field = value
            combinedKey = userID + questKey
        }
    var userID: String? = null
        set(value) {
            field = value
            combinedKey = userID + questKey
        }
    var count: Int = 0

    @Ignore
    var title: String? = null
}
