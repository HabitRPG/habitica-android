package com.habitrpg.shared.habitica.models.responses

import com.habitrpg.shared.habitica.HParcel
import com.habitrpg.shared.habitica.HParcelable
import com.habitrpg.shared.habitica.HParcelize
import com.habitrpg.shared.habitica.getClassLoader
import com.habitrpg.shared.habitica.models.AvatarStats
import kotlin.jvm.JvmField

data class TaskScoringResult(
    var hasDied: Boolean = false,
    var drop: TaskDirectionDataDrop? = null,
    var experienceDelta: Double = 0.0,
    var healthDelta: Double = 0.0,
    var goldDelta: Double = 0.0,
    var manaDelta: Double = 0.0,
    var hasLeveledUp: Boolean = false,
    var level: Int = 0,
    var questDamage: Double? = null,
    var questItemsFound: Int? = null,
) : HParcelable {
    constructor(data: TaskDirectionData, stats: AvatarStats?) : this(
        data.hp <= 0.0,
        data._tmp?.drop,
        if (data.lvl > (stats?.lvl ?: 0)) {
            (stats?.toNextLevel ?: 0).toDouble() - (stats?.exp ?: 0.0) + data.exp
        } else {
            data.exp - (stats?.exp ?: 0.0)
        },
        data.hp - (stats?.hp ?: 0.0),
        data.gp - (stats?.gp ?: 0.0),
        data.mp - (stats?.mp ?: 0.0),
        data.lvl > (stats?.lvl ?: 0),
        data.lvl,
        data._tmp?.quest?.progressDelta,
        data._tmp?.quest?.collection,
    )

    constructor(source: HParcel) : this(
        hasDied = source.readByte() != 0.toByte(),
        drop = source.readParcelable(getClassLoader(TaskDirectionDataDrop.CREATOR::class)),
        experienceDelta = source.readDouble(),
        healthDelta = source.readDouble(),
        goldDelta = source.readDouble(),
        manaDelta = source.readDouble(),
        hasLeveledUp = source.readByte() != 0.toByte(),
        level = source.readInt(),
        questDamage = source.readValue(getClassLoader(Double::class)) as? Double,
        questItemsFound = source.readValue(getClassLoader(Int::class)) as? Int,
    )

    override fun writeToParcel(dest: HParcel, flags: Int) {
        dest.writeByte(if (hasDied) 1.toByte() else 0.toByte())
        dest.writeParcelable(drop, flags)
        dest.writeDouble(experienceDelta)
        dest.writeDouble(healthDelta)
        dest.writeDouble(goldDelta)
        dest.writeDouble(manaDelta)
        dest.writeByte(if (hasLeveledUp) 1.toByte() else 0.toByte())
        dest.writeInt(level)
        dest.writeValue(questDamage)
        dest.writeValue(questItemsFound)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        final val CREATOR: HParcelable.Creator<TaskScoringResult> = object : HParcelable.Creator<TaskScoringResult> {
            override fun createFromParcel(source: HParcel): TaskScoringResult {
                return TaskScoringResult(source)
            }

            override fun newArray(size: Int): Array<TaskScoringResult?> {
                return arrayOfNulls(size)
            }
        }
    }
}
