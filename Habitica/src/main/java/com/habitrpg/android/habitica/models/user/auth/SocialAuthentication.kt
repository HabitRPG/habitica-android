package com.habitrpg.android.habitica.models.user.auth

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.utils.SocialAuthenticationSerializer
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable(with = SocialAuthenticationSerializer::class)
open class SocialAuthentication :
    RealmObject(),
    BaseObject {
    var emails: RealmList<String> = RealmList()
}
