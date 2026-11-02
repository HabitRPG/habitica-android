package com.habitrpg.android.habitica.models.responses

import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.Preferences
import com.habitrpg.android.habitica.models.user.Purchases
import kotlinx.serialization.Serializable

@Serializable
class UnlockResponse {
    var preferences: Preferences? = null
    var purchased: Purchases? = null
    var items: Items? = null
}
