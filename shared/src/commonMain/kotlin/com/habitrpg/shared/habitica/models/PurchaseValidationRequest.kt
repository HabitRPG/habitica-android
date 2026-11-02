package com.habitrpg.shared.habitica.models

import kotlinx.serialization.Serializable

@Serializable
class PurchaseValidationRequest {
    var sku: String? = null
    var transaction: Transaction? = null
    var gift: IAPGift? = null
    var deferredSku: String? = null
}
