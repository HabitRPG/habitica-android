package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.BaseMainObject
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
open class GroupCategory :
    RealmObject(),
    BaseMainObject {
    override val realmClass: Class<out RealmModel>
        get() = Group::class.java
    override val primaryIdentifier: String?
        get() = id
    override val primaryIdentifierName: String
        get() = "id"

    @SerialName("_id")
    @PrimaryKey
    var id: String = ""

    var slug: String? = null
    var name: String? = null
}
