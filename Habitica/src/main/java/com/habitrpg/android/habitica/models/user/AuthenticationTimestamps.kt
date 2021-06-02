package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class AuthenticationTimestamps : RealmObject(), BaseObject {

    @PrimaryKey
    var userId: String? = null

    @SerializedName("loggedin")
    var lastLoggedIn: Date? = null
    @SerializedName("created")
    var createdAt: Date? = null
}
