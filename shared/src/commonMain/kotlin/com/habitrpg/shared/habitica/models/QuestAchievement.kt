package com.habitrpg.shared.habitica.models

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.IgnoreAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class QuestAchievement: NativeRealmObject() {
    @PrimaryKeyAnnotation
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

    @IgnoreAnnotation
    var title: String? = null
}
