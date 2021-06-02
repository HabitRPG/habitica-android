package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class ABTest: RealmObject(), BaseObject {

    @PrimaryKey
    var userID: String? = null

    var name: String = ""
    var group: String = ""
}