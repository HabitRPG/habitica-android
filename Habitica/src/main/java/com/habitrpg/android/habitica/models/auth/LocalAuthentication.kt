package com.habitrpg.android.habitica.models.auth

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.shared.habitica.models.AvatarLocalAuthentication
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class LocalAuthentication :
    RealmObject(),
    BaseObject,
    AvatarLocalAuthentication {
    override var username: String? = null
    var email: String? = null

    @SerialName("has_password")
    var hasPassword: Boolean? = false
}
