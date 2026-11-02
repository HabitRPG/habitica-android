package com.habitrpg.shared.habitica.models.responses

import com.habitrpg.shared.habitica.serialization.FeedResponseSerializer
import kotlinx.serialization.Serializable

// The server answers feeding with just the new pet value
@Serializable(with = FeedResponseSerializer::class)
class FeedResponse {
    var value: Int? = null
    var message: String? = null
}
