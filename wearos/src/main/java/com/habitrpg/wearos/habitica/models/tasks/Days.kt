package com.habitrpg.wearos.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
open class Days() : Parcelable {
    var m: Boolean = true
    var t: Boolean = true
    var w: Boolean = true
    var th: Boolean = true
    var f: Boolean = true
    var s: Boolean = true
    var su: Boolean = true

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeByte(if (m) 1.toByte() else 0.toByte())
        dest.writeByte(if (t) 1.toByte() else 0.toByte())
        dest.writeByte(if (w) 1.toByte() else 0.toByte())
        dest.writeByte(if (th) 1.toByte() else 0.toByte())
        dest.writeByte(if (f) 1.toByte() else 0.toByte())
        dest.writeByte(if (s) 1.toByte() else 0.toByte())
        dest.writeByte(if (su) 1.toByte() else 0.toByte())
    }

    protected constructor(`in`: Parcel) : this() {
        m = `in`.readByte().toInt() != 0
        t = `in`.readByte().toInt() != 0
        w = `in`.readByte().toInt() != 0
        th = `in`.readByte().toInt() != 0
        f = `in`.readByte().toInt() != 0
        s = `in`.readByte().toInt() != 0
        su = `in`.readByte().toInt() != 0
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Days> {
        override fun createFromParcel(parcel: Parcel): Days {
            return Days(parcel)
        }

        override fun newArray(size: Int): Array<Days?> {
            return arrayOfNulls(size)
        }
    }
}
