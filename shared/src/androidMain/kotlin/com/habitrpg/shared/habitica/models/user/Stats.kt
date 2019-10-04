package com.habitrpg.shared.habitica.models.user

import android.content.Context
import com.google.gson.annotations.SerializedName
import com.habitrpg.shared.habitica.models.HabitRpgClass
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import space.thelen.shared.cluetective.R

actual open class Stats : RealmObject() {

    @PrimaryKey
    actual var userId: String? = null
        set(userId) {
            field = userId
            if (buffs?.isManaged == false) {
                buffs?.userId = userId
            }
            if (training?.isManaged == false) {
                training?.userId = userId
            }
        }

    internal actual var user: User? = null
    @SerializedName("con")
    actual var constitution: Int? = null
    @SerializedName("str")
    actual var strength: Int? = null
    @SerializedName("per")
    actual var per: Int? = null
    @SerializedName("int")
    actual var intelligence: Int? = null
    actual var training: Training? = null
    actual var buffs: Buffs? = null
    actual var points: Int? = null
    actual var lvl: Int? = null
    @SerializedName("class")
    actual var habitClass: String? = null
    actual var gp: Double? = null
    actual var exp: Double? = null
    actual var mp: Double? = null
    actual var hp: Double? = null
    actual var toNextLevel: Int? = null
        get() = if (field != null) field else 0
        set(value) {
            if (value != 0) {
                field = value
            }
        }
    actual var maxHealth: Int? = null
        get() = if (field != null) field else 0
        set(value) {
            if (value != 0) {
                field = value
            }
        }
    actual var maxMP: Int? = null
        get() = if (field != null) field else 0
        set(value) {
            if (value != 0) {
                field = value
            }
        }
    actual val isBuffed: Boolean
        get() {
            return buffs?.str ?: 0f > 0 ||
                    buffs?.con ?: 0f > 0 ||
                    buffs?._int ?: 0f > 0 ||
                    buffs?.per ?: 0f > 0
        }

    actual fun getTranslatedClassName(context: Context): String {
        return when (habitClass) {
            HEALER -> context.getString(R.string.healer)
            ROGUE -> context.getString(R.string.rogue)
            WARRIOR -> context.getString(R.string.warrior)
            MAGE -> context.getString(R.string.mage)
            else -> context.getString(R.string.warrior)
        }
    }

    actual fun merge(stats: Stats?) {
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

    actual fun setHabitClass(habitRpgClass: HabitRpgClass) {
        habitClass = habitRpgClass.toString()
    }

    companion object {
        const val STRENGTH = "str"
        const val INTELLIGENCE = "int"
        const val CONSTITUTION = "con"
        const val PERCEPTION = "per"


        const val WARRIOR = "warrior"
        const val MAGE = "wizard"
        const val HEALER = "healer"
        const val ROGUE = "rogue"

        const val AUTO_ALLOCATE_FLAT = "flat"
        const val AUTO_ALLOCATE_CLASSBASED = "classbased"
        const val AUTO_ALLOCATE_TASKBASED = "taskbased"
    }
}
