package com.habitrpg.shared.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.*

actual open class RemindersItem : RealmObject, Parcelable {
    @PrimaryKey
    actual var id: String? = null
    actual var startDate: Date? = null
    actual var time: Date? = null

    //Use to store task type before a task is created
    actual var type: String? = null

    actual override fun equals(other: Any?): Boolean {
        return if (other?.javaClass == RemindersItem::class.java) {
            this.id == (other as? RemindersItem)?.id
        } else super.equals(other)
    }

    actual override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    actual override fun describeContents(): Int {
        return 0
    }

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeLong(this.startDate?.time ?: -1)
        dest.writeLong(this.time?.time ?: -1)

    }

    actual companion object CREATOR : Parcelable.Creator<RemindersItem> {
        actual override fun createFromParcel(source: Parcel): RemindersItem = RemindersItem(source)

        actual override fun newArray(size: Int): Array<RemindersItem?> = arrayOfNulls(size)
    }

    actual constructor(source: Parcel) {
        id = source.readString()
        startDate = Date(source.readLong())
        time = Date(source.readLong())
    }

    actual constructor()
}

