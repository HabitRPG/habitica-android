package com.habitrpg.android.habitica.models.user

import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.shared.habitica.models.AvatarBuffs
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Buffs : RealmObject(), AvatarBuffs, BaseObject {
    override var con: Float? = null
    override var str: Float? = null
    override var per: Float? = null

    @SerializedName("int")
    override var intelligence: Float? = null
    override var seafoam: Boolean? = null
        get() {
            return field ?: false
        }
    override var spookySparkles: Boolean? = null
        get() {
            return field ?: false
        }
    override var shinySeed: Boolean? = null
        get() {
            return field ?: false
        }
    override var snowball: Boolean? = null
        get() {
            return field ?: false
        }
    override var streaks: Boolean? = null
        get() {
            return field ?: false
        }

    fun merge(stats: Buffs?) {
        if (stats == null) {
            return
        }
        con = if (stats.con != null) stats.con else con
        str = if (stats.str != null) stats.str else str
        per = if (stats.per != null) stats.per else per
        intelligence = if (stats.intelligence != null) stats.intelligence else intelligence
        snowball = if (stats.snowball != null) stats.snowball else snowball
        streaks = if (stats.streaks != null) stats.streaks else streaks
        seafoam = if (stats.seafoam != null) stats.seafoam else seafoam
        shinySeed = if (stats.shinySeed != null) stats.shinySeed else shinySeed
        spookySparkles = if (stats.spookySparkles != null) stats.spookySparkles else spookySparkles
    }
}
