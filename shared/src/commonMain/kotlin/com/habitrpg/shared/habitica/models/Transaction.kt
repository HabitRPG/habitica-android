package com.habitrpg.shared.habitica.models

import kotlinx.serialization.Serializable

@Serializable
class Transaction {
    var receipt: String? = null
    var signature: String? = null
}
