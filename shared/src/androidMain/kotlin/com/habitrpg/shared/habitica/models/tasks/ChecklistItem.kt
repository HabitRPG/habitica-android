package com.habitrpg.shared.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable

import java.util.UUID

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by viirus on 06/07/15.
 */
actual open class ChecklistItem: RealmObject, Parcelable {

    @PrimaryKey
    actual var id: String? = null
    actual var text: String? = null
    actual var completed: Boolean = false
    actual var position: Int = 0

    @JvmOverloads
    actual constructor(id: String?, text: String?, completed: Boolean) {
        this.text = text
        if (id?.isNotEmpty() == true) {
            this.id = id
        } else {
            this.id = UUID.randomUUID().toString()
        }
        this.completed = completed
    }

    actual constructor(item: ChecklistItem) {
        this.text = item.text
        this.id = item.id
        this.completed = item.completed
    }

    actual override fun equals(other: Any?): Boolean {
        return if (other?.javaClass == ChecklistItem::class.java && this.id != null) {
            this.id == (other as? ChecklistItem)?.id
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
        dest.writeString(text)
        dest.writeByte(if (completed) 1.toByte() else 0.toByte())
        dest.writeInt(position)
    }

    actual companion object CREATOR : Parcelable.Creator<ChecklistItem> {
        override fun createFromParcel(source: Parcel): ChecklistItem = ChecklistItem(source)

        override fun newArray(size: Int): Array<ChecklistItem?> = arrayOfNulls(size)
    }

    actual constructor(source: Parcel) {
        id = source.readString()
        text = source.readString()
        completed = source.readByte() == 1.toByte()
        position = source.readInt()
    }
}
