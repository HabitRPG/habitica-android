package com.habitrpg.shared.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class ABTest: RealmObject() {

    @PrimaryKey
    actual var userID: String? = null

    actual var name: String = ""
    actual var group: String = ""
}