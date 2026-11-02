@file:UseSerializers(DateSerializer::class)

package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.shared.habitica.serialization.DateSerializer
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import java.util.Date

@RealmClass(embedded = true)
@Serializable
open class UserParty :
    RealmObject(),
    BaseObject {
    @SerialName("_id")
    var id: String = ""
    var quest: Quest? = null
    var seeking: Date? = null

    @SerialName("order")
    var partyOrder: String? = null // Order to display ppl
    var orderAscending: String? = null // Order type
}
