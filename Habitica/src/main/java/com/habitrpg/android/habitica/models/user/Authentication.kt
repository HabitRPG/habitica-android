package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.auth.LocalAuthentication
import com.habitrpg.android.habitica.models.user.auth.SocialAuthentication
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Authentication : RealmObject(), BaseObject {
    fun findFirstSocialEmail(): String? {
        for (auth in listOf(googleAuthentication, appleAuthentication, facebookAuthentication)) {
            if (auth?.emails?.isNotEmpty() == true) {
                return auth.emails.first()
            }
        }
        return null
    }

    val hasPassword: Boolean
        get() = localAuthentication?.email != null
    @SerializedName("local")
    var localAuthentication: LocalAuthentication? = null
    @SerializedName("google")
    var googleAuthentication: SocialAuthentication? = null
    @SerializedName("apple")
    var appleAuthentication: SocialAuthentication? = null
    @SerializedName("facebook")
    var facebookAuthentication: SocialAuthentication? = null

    val hasGoogleAuth: Boolean
        get() = googleAuthentication?.emails?.isEmpty() == false
    val hasAppleAuth: Boolean
        get() = appleAuthentication?.emails?.isEmpty() == false
    val hasFacebookAuth: Boolean
        get() = facebookAuthentication?.emails?.isEmpty() == false

    var timestamps: AuthenticationTimestamps? = null
}
