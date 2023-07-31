package com.habitrpg.android.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import com.habitrpg.android.habitica.models.BaseMainObject
import io.realm.RealmModel
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import java.util.UUID

open class ChecklistItem : RealmObject, BaseMainObject, Parcelable {

    override val realmClass: Class<ChecklistItem>
        get() = ChecklistItem::class.java
    override val primaryIdentifier: String?
        get() = id
    override val primaryIdentifierName: String
        get() = "id"

    @PrimaryKey
    var id: String? = UUID.randomUUID().toString()
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

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(text)
        dest.writeByte(if (completed) 1.toByte() else 0.toByte())
        dest.writeInt(position)
    }

    companion object CREATOR : Parcelable.Creator<ChecklistItem>, RealmModel {
        override fun createFromParcel(source: Parcel): ChecklistItem = ChecklistItem(source)

        override fun newArray(size: Int): Array<ChecklistItem?> = arrayOfNulls(size)
    }

    constructor(source: Parcel) {
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
