package com.habitrpg.android.habitica.models

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class TeamPlan : RealmObject(), BaseObject {
    @PrimaryKey
    var id: String = ""

    var userID: String? = null
    var summary: String = ""

    @SerializedName("leader")
    var leaderID: String? = null

    // var managers: RealmList<String> = RealmList()
    var isActive: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (other is TeamPlan) {
            return this.id == other.id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
