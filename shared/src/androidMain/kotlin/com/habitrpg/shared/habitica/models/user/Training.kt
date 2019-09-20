package com.habitrpg.shared.habitica.models.user

import com.google.gson.annotations.SerializedName

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual class Training : RealmObject() {

    @PrimaryKey
    actual var userId: String? = null

    internal actual var stats: Stats? = null
    actual var con: Float? = null
        get(): Float? {
            return if (con != null) con else java.lang.Float.valueOf(0f)
        }

    actual var str: Float? = null
        get(): Float? {
            return if (str != null) str else java.lang.Float.valueOf(0f)
        }

    actual var per: Float? = null
        get(): Float? {
            return if (per != null) per else java.lang.Float.valueOf(0f)
        }

    @SerializedName("int")
    actual var _int: Float? = null
        get(): Float? {
            return if (_int != null) _int else java.lang.Float.valueOf(0f)
        }

    actual fun merge(stats: Training?) {
        if (stats == null) {
            return
        }
        this.con = if (stats.con != null) stats.con else this.con
        this.str = if (stats.str != null) stats.str else this.str
        this.per = if (stats.per != null) stats.per else this.per
        this._int = if (stats._int != null) stats._int else this._int
    }
}

