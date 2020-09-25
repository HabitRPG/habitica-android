package com.habitrpg.android.habitica.models.user

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Inbox : RealmObject() {

    @PrimaryKey
    var userId: String? = null

    internal var user: User? = null
    var optOut: Boolean = false
    var blocks: RealmList<String> = RealmList()
    var newMessages: Int = 0
}