package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.auth.LocalAuthentication

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Authentication : RealmObject(), BaseObject {

    @PrimaryKey
    var userId: String? = null
    set(value) {
        field = value
        timestamps?.userId = value
        localAuthentication?.userID = value
    }

    @SerializedName("local")
    var localAuthentication: LocalAuthentication? = null

    var hasFacebookAuth = false
    var hasGoogleAuth = false
    var hasAppleAuth = false

    var timestamps: AuthenticationTimestamps? = null

}
