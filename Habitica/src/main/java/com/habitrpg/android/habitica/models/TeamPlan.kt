@file:UseSerializers(RealmListSerializer::class)

package com.habitrpg.android.habitica.models

import com.habitrpg.android.habitica.utils.RealmListSerializer
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
open class TeamPlan :
    RealmObject(),
    BaseObject {
    @PrimaryKey
    var id: String = ""

    var userID: String? = null
    var summary: String = ""

    @SerialName("leader")
    var leaderID: String? = null

    // var managers: RealmList<String> = RealmList()
    var isActive: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (other is TeamPlan) {
            return this.id == other.id
        }
        return super.equals(other)
    }

    override fun hashCode(): Int = id.hashCode()
}
