package com.habitrpg.android.habitica.models.social

import com.habitrpg.android.habitica.models.user.Backer
import com.habitrpg.shared.habitica.models.user.ContributorInfo
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

open class ChatMessage : RealmObject() {

    @PrimaryKey
    var id: String = ""
    set(value) {
        field = value
        likes?.forEach { it.messageId = value }
        userStyles?.id = id
        contributor?.userId = id
        backer?.id = id
    }

    var text: String? = null

    @Ignore
    var parsedText: CharSequence? = null

    var timestamp: Long? = null

    var likes: RealmList<ChatMessageLike>? = null

    var flagCount: Int = 0

    var uuid: String? = null
    var userID: String? = null

    var contributor: com.habitrpg.shared.habitica.models.user.ContributorInfo? = null

    var backer: Backer? = null

    var user: String? = null

    var sent: Boolean = false

    var groupId: String? = null

    var isInboxMessage: Boolean = false

    var userStyles: UserStyles? = null

    val isSystemMessage: Boolean
        get() = uuid == "system"

    val likeCount: Int
        get() = likes?.size ?: 0

    var username: String? = null
    val formattedUsername: String?
        get() = if (username != null) "@$username" else null

    fun userLikesMessage(userId: String?): Boolean {
        return likes?.any { userId == it.id } ?: false
    }
}

