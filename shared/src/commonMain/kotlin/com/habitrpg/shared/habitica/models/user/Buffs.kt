package com.habitrpg.shared.habitica.models.user

import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation
import com.habitrpg.shared.habitica.nativePackages.annotations.SerializedNameAnnotation
import kotlin.jvm.JvmOverloads

open class Buffs @JvmOverloads constructor(snowball: Boolean? = false, streaks: Boolean? = false) : NativeRealmObject() {

    @PrimaryKeyAnnotation
    var userId: String? = null

    var con: Float? = null
    var str: Float? = null
    var per: Float? = null
    @SerializedNameAnnotation("int")
    var _int: Float? = null
    var seafoam: Boolean? = null
        get(): Boolean? {
            return if (field != null) field else false
        }
        set(seafoam: Boolean?) {
            field = seafoam
        }
    var spookySparkles: Boolean? = null
        get(): Boolean? {
            return if (field != null) field else false
        }
        set(spookySparkles: Boolean?) {
            field = spookySparkles
        }
    var shinySeed: Boolean? = null
        get(): Boolean? {
            return if (field != null) field else false
        }
        set(shinySeed: Boolean?) {
            field = shinySeed
        }
    var snowball: Boolean? = false
        get() {
            return if (field != null) field else false
        }
        set(snowball) {
            field = snowball
        }
    var streaks: Boolean? = false
        get(): Boolean? {
            return if (field != null) field else false
        }
        set(streaks: Boolean?) {
            field = streaks
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
