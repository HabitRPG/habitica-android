package com.habitrpg.wearos.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class RemindersItem() : Parcelable {
    var id: String? = null
    var startDate: String? = null
    var time: String? = null

    // Use to store task type before a task is created
    var type: String? = null

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeString(id)
        dest.writeString(startDate)
        dest.writeString(time)
    }

    companion object CREATOR : Parcelable.Creator<RemindersItem> {
        override fun createFromParcel(source: Parcel): RemindersItem = RemindersItem(source)

        override fun newArray(size: Int): Array<RemindersItem?> = arrayOfNulls(size)
    }

    constructor(source: Parcel) : this() {
        id = source.readString()
        startDate = source.readString()
        time = source.readString()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is RemindersItem) {
            this.id == other.id
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
