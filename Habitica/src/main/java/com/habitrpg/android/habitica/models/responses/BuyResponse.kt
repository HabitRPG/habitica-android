package com.habitrpg.android.habitica.models.responses

import com.habitrpg.android.habitica.models.user.Items
import kotlinx.serialization.Serializable

@Serializable
class BuyResponse {
    var items: Items? = null
    var armoire: Map<String, String?> = emptyMap()
    var lvl: Int? = null
    var gp: Double? = null
    var exp: Double? = null
    var mp: Double? = null
    var hp: Double? = null
}
