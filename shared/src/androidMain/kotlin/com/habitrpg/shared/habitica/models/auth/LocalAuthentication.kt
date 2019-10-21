package com.habitrpg.shared.habitica.models.auth


import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by admin on 18/11/15.
 */
actual open class LocalAuthentication : RealmObject() {
    @PrimaryKey
    actual var userID: String? = null
    actual var username: String? = null
    actual var email: String? = null
}
