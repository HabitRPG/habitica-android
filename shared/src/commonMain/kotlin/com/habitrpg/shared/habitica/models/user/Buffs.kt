package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation
import kotlin.jvm.JvmOverloads

open class Buffs @JvmOverloads constructor(private var snowball: Boolean? = false, private var streaks: Boolean? = false) : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    var con: Float? = null
    var str: Float? = null
    var per: Float? = null
    @SerializedNameAnnotation("int")
    var _int: Float? = null
    private var seafoam: Boolean? = null
    private var spookySparkles: Boolean? = null
    private var shinySeed: Boolean? = null

    fun getSnowball(): Boolean? {
        return if (snowball != null) snowball else false
    }

    fun setSnowball(snowball: Boolean?) {
        this.snowball = snowball
    }

    fun getSeafoam(): Boolean? {
        return if (seafoam != null) seafoam else false
    }

    fun setSeafoam(seafoam: Boolean?) {
        this.seafoam = seafoam
    }

    fun getSpookySparkles(): Boolean? {
        return if (spookySparkles != null) spookySparkles else false
    }

    fun setSpookySparkles(spookySparkles: Boolean?) {
        this.spookySparkles = spookySparkles
    }

    fun getShinySeed(): Boolean? {
        return if (shinySeed != null) shinySeed else false
    }

    fun setShinySeed(shinySeed: Boolean?) {
        this.shinySeed = shinySeed
    }

    fun getStreaks(): Boolean? {
        return if (streaks != null) streaks else false
    }

    fun setStreaks(streaks: Boolean?) {
        this.streaks = streaks
    }

    fun merge(stats: Buffs?) {
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
