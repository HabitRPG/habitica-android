package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import java.util.*

@RealmClass(embedded = true)
open class AuthenticationTimestamps : RealmObject(), BaseObject {
    @SerializedName("loggedin")
    var lastLoggedIn: Date? = null
    @SerializedName("created")
    var createdAt: Date? = null
}
