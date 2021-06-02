package com.habitrpg.android.habitica.models.auth

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class LocalAuthentication : RealmObject(), BaseObject {
    @PrimaryKey
    var userID: String? = null
    var username: String? = null
    var email: String? = null
}