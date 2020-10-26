package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation

open class Training : NativeRealmObject() {
    @PrimaryKeyAnnotation
    var userId: String? = null
    var stats: Stats? = null
    var con: Float = 0f
    var str: Float = 0f
    var per: Float = 0f

    @SerializedNameAnnotation("int")
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
