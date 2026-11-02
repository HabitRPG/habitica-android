package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.user.Authentication
import com.habitrpg.android.habitica.models.user.ContributorInfo
import com.habitrpg.android.habitica.models.user.Profile
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class FindUsernameResult {
    @SerialName("_id")
    var id: String? = null

    var contributor: ContributorInfo? = null

    @SerialName("auth")
    var authentication: Authentication? = null
    var profile: Profile? = null

    val username: String?
        get() = authentication?.localAuthentication?.username
    val formattedUsername: String?
        get() = if (username != null) "@$username" else null

    override fun toString(): String = "@${authentication?.localAuthentication?.username ?: ""}"
}
