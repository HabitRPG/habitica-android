package com.habitrpg.android.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.Date

open class RemindersItem : RealmObject, Parcelable {
    @PrimaryKey
    var id: String? = null
    var startDate: Date? = null
    var time: Date? = null

    // Use to store task type before a task is created
    var type: String? = null

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeLong(this.startDate?.time ?: -1)
        dest.writeLong(this.time?.time ?: -1)
    }

    companion object CREATOR : Parcelable.Creator<RemindersItem> {
        override fun createFromParcel(source: Parcel): RemindersItem = RemindersItem(source)

        override fun newArray(size: Int): Array<RemindersItem?> = arrayOfNulls(size)
    }

    constructor(source: Parcel) {
        id = source.readString()
        startDate = Date(source.readLong())
        time = Date(source.readLong())
    }

    constructor()

    override fun equals(other: Any?): Boolean {
        return if (other is RemindersItem) {
            this.id == other.id
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
