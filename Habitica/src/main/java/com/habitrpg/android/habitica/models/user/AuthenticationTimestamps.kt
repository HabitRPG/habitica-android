package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import io.realm.annotations.PrimaryKey
import java.util.*

class AuthenticationTimestamps {

    @PrimaryKey
    var userId: String? = null

    @SerializedName("loggedin")
    var lastLoggedIn: Date? = null
    @SerializedName("created")
    var createdAt: Date? = null
}
