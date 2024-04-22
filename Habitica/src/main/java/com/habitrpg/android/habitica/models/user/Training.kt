package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.BaseObject
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Training : RealmObject(), BaseObject {
    var con: Float = 0f
    var str: Float = 0f
    var per: Float = 0f

    @SerializedName("int")
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
