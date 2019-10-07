package com.habitrpg.shared.habitica.models

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Achievement : RealmObject() {
    @PrimaryKey
    actual var key: String? = null
    actual var type: String? = null
    actual var title: String? = null
    actual var text: String? = null
    actual var icon: String? = null
    actual var category: String? = null
    actual var earned: Boolean = false
    actual var index: Int = 0
    actual var optionalCount: Int? = null
}
