package com.habitrpg.android.habitica.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

open class WorldStateEvent: RealmObject(), BaseObject {
    @PrimaryKey
    @SerializedName("event")
    var eventKey: String? = null
    var start: Date? = null
    var end: Date? = null
    var promo: String? = null
    var npcImageSuffix: String? = null

    override val realmClass: Class<out RealmModel>
        get() = WorldStateEvent::class.java
    override val primaryIdentifier: String?
        get() = eventKey
    override val primaryIdentifierName: String
        get() = "eventKey"
}