package com.habitrpg.shared.habitica.models.tasks

import android.os.Parcel
import android.os.Parcelable

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

actual open class Days : RealmObject, Parcelable {

    @PrimaryKey
    actual var taskId: String? = null
    actual var m: Boolean = false
    actual var t: Boolean = false
    actual var w: Boolean = false
    actual var th: Boolean = false
    actual var f: Boolean = false
    actual var s: Boolean = false
    actual var su: Boolean = false

    actual fun getForDay(day: Int): Boolean {
        when (day) {
            2 -> return this.m
            3 -> return this.t
            4 -> return this.w
            5 -> return this.th
            6 -> return this.f
            7 -> return this.s
            1 -> return this.su
        }
        return false
    }

    actual override fun describeContents(): Int {
        return 0
    }

    actual override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.taskId)
        dest.writeByte(if (this.m) 1.toByte() else 0.toByte())
        dest.writeByte(if (this.t) 1.toByte() else 0.toByte())
        dest.writeByte(if (this.w) 1.toByte() else 0.toByte())
        dest.writeByte(if (this.th) 1.toByte() else 0.toByte())
        dest.writeByte(if (this.f) 1.toByte() else 0.toByte())
        dest.writeByte(if (this.s) 1.toByte() else 0.toByte())
        dest.writeByte(if (this.su) 1.toByte() else 0.toByte())
    }

    protected actual constructor(`in`: Parcel) {
        this.taskId = `in`.readString()
        this.m = `in`.readByte().toInt() != 0
        this.t = `in`.readByte().toInt() != 0
        this.w = `in`.readByte().toInt() != 0
        this.th = `in`.readByte().toInt() != 0
        this.f = `in`.readByte().toInt() != 0
        this.s = `in`.readByte().toInt() != 0
        this.su = `in`.readByte().toInt() != 0
    }

    constructor() {
        m = true
        t = true
        w = true
        th = true
        f = true
        s = true
        su = true
    }

    companion object {

        @JvmField
        val CREATOR: Parcelable.Creator<Days> = object : Parcelable.Creator<Days> {
            override fun createFromParcel(source: Parcel): Days {
                return Days(source)
            }

            override fun newArray(size: Int): Array<Days?> {
                return arrayOfNulls(size)
            }
        }
    }
}
