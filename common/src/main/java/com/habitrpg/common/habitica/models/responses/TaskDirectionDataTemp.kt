package com.habitrpg.common.habitica.models.responses

import android.os.Parcel
import android.os.Parcelable

class TaskDirectionDataTemp {

    var drop: TaskDirectionDataDrop? = null
    var quest: TaskDirectionDataQuest? = null
    var crit: Float? = null
}

class TaskDirectionDataQuest {
    var progressDelta: Double = 0.0
    var collection: Int = 0
}

class TaskDirectionDataDrop() : Parcelable {
    var value: Int = 0
    var key: String? = null
    var type: String? = null
    var dialog: String? = null

    constructor(parcel: Parcel) : this() {
        value = parcel.readInt()
        key = parcel.readString()
        type = parcel.readString()
        dialog = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(value)
        parcel.writeString(key)
        parcel.writeString(type)
        parcel.writeString(dialog)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<TaskDirectionDataDrop> {
        override fun createFromParcel(parcel: Parcel): TaskDirectionDataDrop {
            return TaskDirectionDataDrop(parcel)
        }

        override fun newArray(size: Int): Array<TaskDirectionDataDrop?> {
            return arrayOfNulls(size)
        }
    }
}
