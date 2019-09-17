package com.habitrpg.shared.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable

import java.util.UUID

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by viirus on 06/07/15.
 */
actual open class ChecklistItem : RealmObject, Parcelable {

    @PrimaryKey
    var id: String? = null
    var text: String? = null
    var completed: Boolean = false
    var position: Int = 0

    @JvmOverloads constructor(id: String? = null, text: String? = null, completed: Boolean = false) {
        this.text = text
        if (id?.isNotEmpty() == true) {
            this.id = id
        } else {
            this.id = UUID.randomUUID().toString()
        }
        this.completed = completed
    }

    constructor(item: ChecklistItem) {
        this.text = item.text
        this.id = item.id
        this.completed = item.completed
    }

    override fun equals(other: Any?): Boolean {
        return if (other?.javaClass == ChecklistItem::class.java && this.id != null) {
            this.id == (other as? ChecklistItem)?.id
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(text)
        dest.writeByte(if (completed) 1.toByte() else 0.toByte())
        dest.writeInt(position)
    }

    companion object CREATOR : Parcelable.Creator<ChecklistItem> {
        override fun createFromParcel(source: Parcel): ChecklistItem = ChecklistItem(source)

        override fun newArray(size: Int): Array<ChecklistItem?> = arrayOfNulls(size)
    }

    constructor(source: Parcel) {
        id = source.readString()
        text = source.readString()
        completed = source.readByte() == 1.toByte()
        position = source.readInt()
    }
}
