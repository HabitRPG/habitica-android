package com.habitrpg.shared.habitica.models

import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

actual open class QuestAchievement: RealmObject() {
    @PrimaryKey
    actual var combinedKey: String? = null

    actual var questKey: String? = null
    set(value) {
        field = value
        combinedKey = userID + questKey
    }
    actual var userID: String? = null
        set(value) {
            field = value
            combinedKey = userID + questKey
        }
    actual var count: Int = 0

    @Ignore
    actual var title: String? = null
}
