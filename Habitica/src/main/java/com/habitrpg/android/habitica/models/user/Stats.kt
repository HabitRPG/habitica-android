package com.habitrpg.android.habitica.models.user

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.BaseObject
import com.habitrpg.shared.habitica.models.AvatarStats
import io.realm.RealmObject
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class Stats : RealmObject(), AvatarStats, BaseObject {
    @SerializedName("con")
    var constitution: Int? = null
    @SerializedName("str")
    var strength: Int? = null
    @SerializedName("per")
    var per: Int? = null
    @SerializedName("int")
    var intelligence: Int? = null
    var training: Training? = null
    override var buffs: Buffs? = null
    override var points: Int? = null
    override var lvl: Int? = null
    @SerializedName("class")
    override var habitClass: String? = null
    override var gp: Double? = null
    override var exp: Double? = null
    override var mp: Double? = null
    override var hp: Double? = null
    override var toNextLevel: Int? = null
        get() = if (field != null) field else 0
        set(value) {
            if (value != 0) {
                field = value
            }
        }
    override var maxHealth: Int? = null
        get() = if (field != null) field else 0
        set(value) {
            if (value != 0) {
                field = value
            }
        }
    override var maxMP: Int? = null
        get() = if (field != null) field else 0
        set(value) {
            if (value != 0) {
                field = value
            }
        }

    fun getTranslatedClassName(context: Context): String {
        return when (habitClass) {
            HEALER -> context.getString(R.string.healer)
            ROGUE -> context.getString(R.string.rogue)
            WARRIOR -> context.getString(R.string.warrior)
            MAGE -> context.getString(R.string.mage)
            else -> context.getString(R.string.warrior)
        }
    }

    fun merge(stats: Stats?) {
        if (stats == null) {
            return
        }
        this.constitution = if (stats.constitution != null) stats.constitution else this.constitution
        this.strength = if (stats.strength != null) stats.strength else this.strength
        this.per = if (stats.per != null) stats.per else this.per
        this.intelligence = if (stats.intelligence != null) stats.intelligence else this.intelligence
        this.training?.merge(stats.training)
        this.buffs?.merge(stats.buffs)
        this.points = if (stats.points != null) stats.points else this.points
        this.lvl = if (stats.lvl != null) stats.lvl else this.lvl
        this.habitClass = if (stats.habitClass != null) stats.habitClass else this.habitClass
        this.gp = if (stats.gp != null) stats.gp else this.gp
        this.exp = if (stats.exp != null) stats.exp else this.exp
        this.hp = if (stats.hp != null) stats.hp else this.hp
        this.mp = if (stats.mp != null) stats.mp else this.mp
        this.toNextLevel = if (stats.toNextLevel != null) stats.toNextLevel else this.toNextLevel
        this.maxHealth = if (stats.maxHealth != null) stats.maxHealth else this.maxHealth
        this.maxMP = if (stats.maxMP != null) stats.maxMP else this.maxMP
    }

    companion object {
        const val WARRIOR = "warrior"
        const val MAGE = "wizard"
        const val HEALER = "healer"
        const val ROGUE = "rogue"

        const val AUTO_ALLOCATE_FLAT = "flat"
        const val AUTO_ALLOCATE_CLASSBASED = "classbased"
        const val AUTO_ALLOCATE_TASKBASED = "taskbased"
    }
}
