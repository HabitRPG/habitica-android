package com.habitrpg.shared.habitica.models.tasks

import com.habitrpg.shared.habitica.nativePackages.NativeDate
import com.habitrpg.shared.habitica.nativePackages.NativeParcel
import com.habitrpg.shared.habitica.nativePackages.NativeParcelable
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

open class RemindersItem : NativeRealmObject, NativeParcelable {
    @PrimaryKeyAnnotation
    var id: String? = null
    var startDate: NativeDate? = null
    var time: NativeDate? = null

    //Use to store task type before a task is created
    var type: String? = null

    override fun equals(other: Any?): Boolean {
        return if (other is RemindersItem) {
            this.id == (other as? RemindersItem)?.id
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: NativeParcel, flags: Int) {
        dest.writeString(id)
        dest.writeLong(this.startDate?.getTime() ?: -1)
        dest.writeLong(this.time?.getTime() ?: -1)
    }

    companion object CREATOR: NativeParcelable.Creator<RemindersItem> {
        override fun createFromParcel(source: NativeParcel): RemindersItem = RemindersItem(source)

        override fun newArray(size: Int): Array<RemindersItem?> = arrayOfNulls(size)
    }

    constructor(source: NativeParcel) {
        id = source.readString()
        startDate = NativeDate(source.readLong())
        time = NativeDate(source.readLong())
    }

    constructor()
}
