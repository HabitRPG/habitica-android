package com.habitrpg.android.habitica.models.user.auth

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class SocialAuthentication : RealmObject(), BaseObject {
    var emails: RealmList<String> = RealmList()
}
