package com.habitrpg.shared.habitica.models

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
class PurchaseValidationResult {
    var ok = false

    @Contextual
    var data: Any? = null
}
