package com.habitrpg.wearos.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass
import java.util.UUID

@JsonClass(generateAdapter = true)
open class ChecklistItem(
    var id: String? = UUID.randomUUID().toString(),
    var text: String? = null,
    var completed: Boolean = false
) : Parcelable {
    var position: Int = 0

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int
    ) {
        dest.writeString(id)
        dest.writeString(text)
        dest.writeByte(if (completed) 1.toByte() else 0.toByte())
        dest.writeInt(position)
    }

    companion object CREATOR : Parcelable.Creator<ChecklistItem> {
        override fun createFromParcel(source: Parcel): ChecklistItem = ChecklistItem(source)

        override fun newArray(size: Int): Array<ChecklistItem?> = arrayOfNulls(size)
    }

    constructor(source: Parcel) : this() {
        id = source.readString()
        text = source.readString()
        completed = source.readByte() == 1.toByte()
        position = source.readInt()
    }

    override fun equals(other: Any?): Boolean {
        return if (other is ChecklistItem) {
            this.id == other.id
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
