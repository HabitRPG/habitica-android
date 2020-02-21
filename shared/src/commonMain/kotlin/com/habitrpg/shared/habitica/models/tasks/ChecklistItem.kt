package com.habitrpg.shared.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import com.habitrpg.shared.habitica.nativePackages.NativeParcel
import com.habitrpg.shared.habitica.nativePackages.NativeParcelable
import com.habitrpg.shared.habitica.nativePackages.NativeRealmObject
import com.habitrpg.shared.habitica.nativePackages.annotations.PrimaryKeyAnnotation

import java.util.UUID

import kotlin.jvm.JvmOverloads

/**
 * Created by viirus on 06/07/15.
 */
open class ChecklistItem : NativeRealmObject, NativeParcelable {

    @PrimaryKeyAnnotation
    var id: String? = null
    var text: String? = null
    var completed: Boolean = false
    var position: Int = 0

    @JvmOverloads
    constructor(id: String? = null, text: String? = null, completed: Boolean = false) {
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
        return if (other is ChecklistItem && this.id != null) {
            this.id == (other as? ChecklistItem)?.id
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
        dest.writeString(text)
        dest.writeByte(if (completed) 1.toByte() else 0.toByte())
        dest.writeInt(position)
    }

    companion object CREATOR : NativeParcelable.Creator<ChecklistItem> {
        override fun createFromParcel(source: NativeParcel): ChecklistItem = ChecklistItem(source)

        override fun newArray(size: Int): Array<ChecklistItem?> = arrayOfNulls(size)
    }

    constructor(source: NativeParcel) {
        id = source.readString()
        text = source.readString()
        completed = source.readByte() == 1.toByte()
        position = source.readInt()
    }
}
