package com.habitrpg.android.habitica.models.social

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.inventory.Quest
import com.habitrpg.android.habitica.models.user.User

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Group : RealmObject() {

    @SerializedName("_id")
    @PrimaryKey
    var id: String = ""

    var balance: Double = 0.toDouble()

    var description: String? = null

    var leaderID: String? = null

    var leaderName: String? = null

    var name: String? = null

    var memberCount: Int = 0

    var isMember: Boolean = false

    var type: String? = null

    var logo: String? = null

    var quest: Quest? = null

    var privacy: String? = null

    var chat: RealmList<ChatMessage>? = null

    var members: RealmList<User>? = null

    var challengeCount: Int = 0

    var leaderMessage: String? = null

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val group = o as Group?

        return if (id != null) id == group!!.id else group!!.id == null

    }

    override fun hashCode(): Int {
        return if (id != null) id!!.hashCode() else 0
    }

    companion object {
        val TAVERN_ID = "00000000-0000-4000-A000-000000000000"
    }

    val hasActiveQuest: Boolean
    get() {
        return quest?.active ?: false
    }
}
