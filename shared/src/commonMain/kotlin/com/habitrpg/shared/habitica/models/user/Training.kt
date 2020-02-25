package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation


open class Training : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    internal var stats: Stats? = null
    var con: Float? = null
        get() {
            return if (field != null) field else 0f
        }
    var str: Float? = null
        get() {
            return if (field != null) field else 0f
        }
    var per: Float? = null
        get() {
            return if (field != null) field else 0f
        }
    @SerializedNameAnnotation("int")
    var _int: Float? = null
        get() {
            return if (field != null) field else 0f
        }

    fun merge(stats: Training?) {
        if (stats == null) {
            return
        }
        this.con = if (stats.con != null) stats.con else this.con
        this.str = if (stats.str != null) stats.str else this.str
        this.per = if (stats.per != null) stats.per else this.per
        this._int = if (stats._int != null) stats._int else this._int
    }
}

