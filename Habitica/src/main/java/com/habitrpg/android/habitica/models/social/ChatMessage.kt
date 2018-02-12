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

    var sent: String? = null

    var groupId: String? = null

    var isInboxMessage: Boolean = false

    val contributorColor: Int
        get() {
            var rColor = android.R.color.black

            val level = contributor?.level
            if (level != null) {
                if (ContributorInfo.CONTRIBUTOR_COLOR_DICT.get(level, -1) > 0) {
                    rColor = ContributorInfo.CONTRIBUTOR_COLOR_DICT[level]
                }
            }

            if (backer?.npc != null) {
                rColor = android.R.color.black
            }

            return rColor
        }

    val contributorForegroundColor: Int
        get() {
            var rColor = android.R.color.white

            if (backer?.npc != null) {
                rColor = R.color.contributor_npc_font
            }

            return rColor
        }

    val isSystemMessage: Boolean
        get() = uuid == "system"

    val likeCount: Int
        get() = likes?.size ?: 0

    fun getAgoString(res: Resources): String {
        val diff = Date().time - timestamp!!

        val diffMinutes = diff / (60 * 1000) % 60
        val diffHours = diff / (60 * 60 * 1000) % 24
        val diffDays = diff / (24 * 60 * 60 * 1000)

        if (diffDays != 0L) {
            return if (diffDays == 1L) {
                res.getString(R.string.ago_1day)
            } else res.getString(R.string.ago_days, diffDays)
        }

        if (diffHours != 0L) {
            return if (diffHours == 1L) {
                res.getString(R.string.ago_1hour)
            } else res.getString(R.string.ago_hours, diffHours)
        }

        return if (diffMinutes == 1L) {
            res.getString(R.string.ago_1Minute)
        } else res.getString(R.string.ago_minutes, diffMinutes)
    }

    fun userLikesMessage(userId: String?): Boolean {
        return likes?.any { userId == it.id } ?: false
    }
}

