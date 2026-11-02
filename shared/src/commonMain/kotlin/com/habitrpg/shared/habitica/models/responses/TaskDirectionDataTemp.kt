package com.habitrpg.shared.habitica.models.responses

import com.habitrpg.shared.habitica.HParcel
import com.habitrpg.shared.habitica.HParcelable
import kotlinx.serialization.Serializable

@Serializable
class TaskDirectionDataTemp {
    var drop: TaskDirectionDataDrop? = null
    var quest: TaskDirectionDataQuest? = null
    var crit: Float? = null
}

@Serializable
class TaskDirectionDataQuest {
    var progressDelta: Double = 0.0
    var collection: Int = 0
}

@Serializable
data class TaskDirectionDataDrop(
    var value: Int = 0,
    var key: String? = null,
    var type: String? = null,
    var dialog: String? = null,
) : HParcelable {
    override fun writeToParcel(
        dest: HParcel,
        flags: Int,
    ) {
        dest.writeInt(value)
        dest.writeString(key)
        dest.writeString(type)
        dest.writeString(dialog)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : HParcelable.Creator<TaskDirectionDataDrop> {
        override fun createFromParcel(source: HParcel): TaskDirectionDataDrop =
            TaskDirectionDataDrop(
                value = source.readInt(),
                key = source.readString(),
                type = source.readString(),
                dialog = source.readString(),
            )

        override fun newArray(size: Int): Array<TaskDirectionDataDrop?> = arrayOfNulls(size)
    }
}
