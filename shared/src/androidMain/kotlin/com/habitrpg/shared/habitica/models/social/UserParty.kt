package com.habitrpg.shared.habitica.models.social

import com.google.gson.annotations.SerializedName
import com.habitrpg.shared.habitica.models.inventory.Quest

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class UserParty : RealmObject() {
    @PrimaryKey
    actual var userId: String? = null
    @SerializedName("_id")
    actual var id: String = ""
    actual var quest: Quest? = null
    @SerializedName("order")
    actual var partyOrder: String? = null//Order to display ppl
    actual var orderAscending: String? = null//Order type

}
