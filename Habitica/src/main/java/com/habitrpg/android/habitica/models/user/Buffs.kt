package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class Buffs : RealmObject() {
    @PrimaryKey
    var userId: String? = null
    var con: Float? = null
    var str: Float? = null
    var per: Float? = null

    @SerializedName("int")
    var _int: Float? = null
    var seafoam: Boolean? = null
        get() { return field ?: false }
    var spookySparkles: Boolean? = null
        get() { return field ?: false }
    var shinySeed: Boolean? = null
        get() { return field ?: false }
    var snowball: Boolean? = null
        get() { return field ?: false }
    var streaks: Boolean? = null
        get() { return field ?: false }


    fun merge(stats: Buffs?) {
        if (stats == null) {
            return
        }
        con = if (stats.con != null) stats.con else con
        str = if (stats.str != null) stats.str else str
        per = if (stats.per != null) stats.per else per
        _int = if (stats._int != null) stats._int else _int
        snowball = if (stats.snowball != null) stats.snowball else snowball
        streaks = if (stats.streaks != null) stats.streaks else streaks
        seafoam = if (stats.seafoam != null) stats.seafoam else seafoam
        shinySeed = if (stats.shinySeed != null) stats.shinySeed else shinySeed
        spookySparkles = if (stats.spookySparkles != null) stats.spookySparkles else spookySparkles
    }
}