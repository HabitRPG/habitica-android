package com.habitrpg.android.habitica.models.responses

import com.habitrpg.android.habitica.models.social.ChatMessage
import kotlinx.serialization.Serializable

@Serializable
class PostChatMessageResult {
    var message: ChatMessage? = null
}
