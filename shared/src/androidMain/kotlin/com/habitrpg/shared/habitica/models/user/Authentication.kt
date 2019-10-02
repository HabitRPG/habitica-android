package com.habitrpg.shared.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.shared.habitica.models.auth.LocalAuthentication

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Authentication : RealmObject() {

    @PrimaryKey
    actual var userId: String? = null
    set(value) {
        field = value
        timestamps?.userId = value
        localAuthentication?.userID = value
    }

    @SerializedName("local")
    actual var localAuthentication: LocalAuthentication? = null

    actual var timestamps: AuthenticationTimestamps? = null

}
