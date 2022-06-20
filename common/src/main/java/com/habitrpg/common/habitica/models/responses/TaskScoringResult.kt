package com.habitrpg.common.habitica.models.responses

import android.os.Parcel
import android.os.Parcelable
import com.habitrpg.common.habitica.models.AvatarStats

class TaskScoringResult(): Parcelable {
    constructor(data: TaskDirectionData, stats: AvatarStats?) : this() {
        hasLeveledUp = data.lvl > (stats?.lvl ?: 0)
        healthDelta = data.hp - (stats?.hp ?: 0.0)
        if (hasLeveledUp) {
            experienceDelta = (stats?.toNextLevel ?: 0).toDouble() - (stats?.exp ?: 0.0) + data.exp
        } else {
            experienceDelta = data.exp - (stats?.exp ?: 0.0)
        }
        manaDelta = data.mp - (stats?.mp ?: 0.0)
        goldDelta = data.gp - (stats?.gp ?: 0.0)
        level = data.lvl
        questDamage = data._tmp?.quest?.progressDelta
        questItemsFound = data._tmp?.quest?.collection
        drop = data._tmp?.drop
    }

    var drop: TaskDirectionDataDrop? = null
    var experienceDelta: Double? = null
    var healthDelta: Double? = null
    var goldDelta: Double? = null
    var manaDelta: Double? = null
    var hasLeveledUp: Boolean = false
    var level: Int? = null
    var questDamage: Double? = null
    var questItemsFound: Int? = null

    constructor(parcel: Parcel) : this() {
        experienceDelta = parcel.readValue(Double::class.java.classLoader) as? Double
        healthDelta = parcel.readValue(Double::class.java.classLoader) as? Double
        goldDelta = parcel.readValue(Double::class.java.classLoader) as? Double
        manaDelta = parcel.readValue(Double::class.java.classLoader) as? Double
        hasLeveledUp = parcel.readByte() != 0.toByte()
        level = parcel.readValue(Int::class.java.classLoader) as? Int
        questDamage = parcel.readValue(Double::class.java.classLoader) as? Double
        questItemsFound = parcel.readValue(Int::class.java.classLoader) as? Int
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(experienceDelta)
        parcel.writeValue(healthDelta)
        parcel.writeValue(goldDelta)
        parcel.writeValue(manaDelta)
        parcel.writeByte(if (hasLeveledUp) 1 else 0)
        parcel.writeValue(level)
        parcel.writeValue(questDamage)
        parcel.writeValue(questItemsFound)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskScoringResult> {
        override fun createFromParcel(parcel: Parcel): TaskScoringResult {
            return TaskScoringResult(parcel)
        }

        override fun newArray(size: Int): Array<TaskScoringResult?> {
            return arrayOfNulls(size)
        }
    }
}
