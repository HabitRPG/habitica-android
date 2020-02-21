package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation


open class Training : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    internal var stats: Stats? = null
    var con: Float? = null
    var str: Float? = null
    var per: Float? = null
    @SerializedNameAnnotation("int")
    var _int: Float? = null

    fun getCon(): Float? {
        return if (con != null) con else 0f
    }

    fun setCon(con: Float?) {
        this.con = con
    }

    fun getStr(): Float? {
        return if (str != null) str else 0f
    }

    fun setStr(str: Float?) {
        this.str = str
    }

    fun getPer(): Float? {
        return if (per != null) per else 0f
    }

    fun setPer(per: Float?) {
        this.per = per
    }

    fun get_int(): Float? {
        return if (_int != null) _int else 0f
    }

    fun set_int(_int: Float?) {
        this._int = _int
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

