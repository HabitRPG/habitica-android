package com.habitrpg.shared.habitica.models.user

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

actual open class AuthenticationTimestamps : RealmObject() {

    @PrimaryKey
    actual var userId: String? = null

    @SerializedName("loggedin")
    actual var lastLoggedIn: Date? = null
    @SerializedName("created")
    actual var createdAt: Date? = null
}
