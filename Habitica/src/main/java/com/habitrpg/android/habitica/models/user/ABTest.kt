package com.habitrpg.android.habitica.models.user

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ABTest: RealmObject() {

    @PrimaryKey
    var userID: String? = null

    var name: String = ""
    var group: String = ""
}