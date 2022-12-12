package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject

open class Permissions: RealmObject(), BaseObject {
    var userSupport: Boolean = false
    var fullAccess: Boolean = false

    var moderator: Boolean = false
}
