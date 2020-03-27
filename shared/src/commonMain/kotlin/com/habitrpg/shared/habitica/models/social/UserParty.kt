package com.habitrpg.shared.habitica.models.social

import com.habitrpg.shared.habitica.models.inventory.Quest
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation


open class UserParty : NativeRealmObject() {
    @PrimaryKeyAnnotation
    var userId: String? = null
    @SerializedNameAnnotation("_id")
    var id: String = ""
    var quest: Quest? = null
    @SerializedNameAnnotation("order")
    var partyOrder: String? = null//Order to display ppl
    var orderAscending: String? = null//Order type

}
