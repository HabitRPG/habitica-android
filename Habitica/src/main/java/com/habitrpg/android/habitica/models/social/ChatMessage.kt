package com.habitrpg.android.habitica.models.social

import android.content.res.Resources

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.user.ContributorInfo

import java.util.Date

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
    }

    var text: String? = null

    @Ignore
    var parsedText: CharSequence? = null

    var timestamp: Long? = null

    var likes: RealmList<ChatMessageLike>? = null

    var flagCount: Int = 0

    var uuid: String? = null

    var contributor: ContributorInfo? = null

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

    fun getAgoString(res: Resources): String {
        val diff = Date().time - (timestamp ?: 0)

        val diffMinutes = diff / (60 * 1000) % 60
        val diffHours = diff / (60 * 60 * 1000) % 24
        val diffDays = diff / (24 * 60 * 60 * 1000)

        return when {
            diffDays != 0L -> if (diffDays == 1L) {
                res.getString(R.string.ago_1day)
            } else res.getString(R.string.ago_days, diffDays)
            diffHours != 0L -> if (diffHours == 1L) {
                res.getString(R.string.ago_1hour)
            } else res.getString(R.string.ago_hours, diffHours)
            diffMinutes == 1L -> res.getString(R.string.ago_1Minute)
            else -> res.getString(R.string.ago_minutes, diffMinutes)
        }
    }

    fun userLikesMessage(userId: String?): Boolean {
        return likes?.any { userId == it.id } ?: false
    }
}

