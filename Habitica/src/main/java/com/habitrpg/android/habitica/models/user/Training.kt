package com.habitrpg.android.habitica.models.user

import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@RealmClass(embedded = true)
@Serializable
open class Training :
    RealmObject(),
    BaseObject {
    var con: Float = 0f
    var str: Float = 0f
    var per: Float = 0f

    @SerialName("int")
    var intelligence: Float = 0f

    fun merge(stats: Training?) {
        if (stats == null) {
            return
        }
        con = stats.con
        str = stats.str
        per = stats.per
        intelligence = stats.intelligence
    }
}
