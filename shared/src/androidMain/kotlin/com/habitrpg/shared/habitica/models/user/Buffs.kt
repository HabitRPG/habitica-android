package com.habitrpg.shared.habitica.models.user

import com.google.gson.annotations.SerializedName

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Buffs: RealmObject {
    constructor() {
        this.snowball = null
        this.streaks = null
        this.seafoam = null
        this.spookySparkles = null
        this.shinySeed = null
    }

    actual constructor(snowball: Boolean, streaks: Boolean, seaform: Boolean, spookySparkles: Boolean, shinySeed: Boolean) {
        this.snowball = snowball
        this.streaks = streaks
        this.seafoam = seaform
        this.spookySparkles = spookySparkles
        this.shinySeed = shinySeed
    }

    @PrimaryKey
    actual var userId: String? = null

    actual var con: Float? = null
    actual var str: Float? = null
    actual var per: Float? = null
    @SerializedName("int")
    actual var _int: Float? = null
    actual var snowball: Boolean? = null
        get(): Boolean? {
            return if (field != null) field else false
        }

    actual var streaks: Boolean? = null
        get(): Boolean? {
            return if (field != null) field else false
        }

    actual var seafoam: Boolean? = null
        get(): Boolean? {
            return if (field != null) field else false
        }

    actual var spookySparkles: Boolean? = null
        get(): Boolean? {
            return if (field != null) field else false
        }

    actual var shinySeed: Boolean? = null
        get(): Boolean? {
            return if (field != null) field else false
        }


    actual fun merge(stats: Buffs?) {
        if (stats == null) {
            return
        }
        this.con = if (stats.con != null) stats.con else this.con
        this.str = if (stats.str != null) stats.str else this.str
        this.per = if (stats.per != null) stats.per else this.per
        this._int = if (stats._int != null) stats._int else this._int
        this.snowball = if (stats.snowball != null) stats.snowball else this.snowball
        this.streaks = if (stats.streaks != null) stats.streaks else this.streaks
        this.seafoam = if (stats.seafoam != null) stats.seafoam else this.seafoam
        this.shinySeed = if (stats.shinySeed != null) stats.shinySeed else this.shinySeed
        this.spookySparkles = if (stats.spookySparkles != null) stats.spookySparkles else this.spookySparkles
    }
}
