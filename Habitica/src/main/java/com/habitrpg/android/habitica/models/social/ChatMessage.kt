package com.habitrpg.android.habitica.models.social

import android.text.Spanned
import com.habitrpg.android.habitica.models.BaseMainObject
import com.habitrpg.android.habitica.models.user.Backer
import com.habitrpg.android.habitica.models.user.ContributorInfo
import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.Ignore
import io.realm.annotations.PrimaryKey

open class ChatMessage : RealmObject(), BaseMainObject {
    override val realmClass: Class<ChatMessage>
        get() = ChatMessage::class.java
    override val primaryIdentifier: String?
        get() = id
    override val primaryIdentifierName: String
        get() = "id"

    @PrimaryKey
    var id: String = ""

    var text: String? = null

    @Ignore
    var parsedText: Spanned? = null

    var timestamp: Long? = null

    var likes: RealmList<ChatMessageLike>? = null

    var flagCount: Int = 0

    var uuid: String? = null
    var userID: String? = null

    var contributor: ContributorInfo? = null

    var backer: Backer? = null

    var user: String? = null

    var sent: Boolean = false

    var groupId: String? = null

    var isInboxMessage: Boolean = false

    var userStyles: UserStyles? = null

    val isSystemMessage: Boolean
        get() = uuid == "system"

    var likeCount: Int = 0

    var username: String? = null
    val formattedUsername: String?
        get() = if (username != null) "@$username" else null

    var isSeen: Boolean = true

    fun userLikesMessage(userId: String?): Boolean {
        return likes?.any { userId == it.id } ?: false
    }
}
