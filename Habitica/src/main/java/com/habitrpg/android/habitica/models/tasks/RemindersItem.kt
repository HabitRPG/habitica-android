package com.habitrpg.android.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable

import java.util.Date

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass(embedded = true)
open class RemindersItem : RealmObject, Parcelable {
    var id: String? = null
    var startDate: Date? = null
    var time: Date? = null

    //Use to store task type before a task is created
    var type: String? = null

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeLong(this.startDate?.time ?: -1)
        dest.writeLong(this.time?.time ?: -1)

    }

    companion object CREATOR: Parcelable.Creator<RemindersItem> {
        override fun createFromParcel(source: Parcel): RemindersItem = RemindersItem(source)

        override fun newArray(size: Int): Array<RemindersItem?> = arrayOfNulls(size)
    }

    constructor(source: Parcel) {
        id = source.readString()
        startDate = Date(source.readLong())
        time = Date(source.readLong())
    }

    constructor()
}
