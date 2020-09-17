package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Training : RealmObject() {
    @PrimaryKey
    var userId: String? = null
    var stats: Stats? = null
    var con: Float = 0f
    var str: Float = 0f
    var per: Float = 0f

    @SerializedName("int")
    var _int: Float = 0f

    fun merge(stats: Training?) {
        if (stats == null) {
            return
        }
        con = stats.con
        str = stats.str
        per = stats.per
        _int = stats._int
    }
}