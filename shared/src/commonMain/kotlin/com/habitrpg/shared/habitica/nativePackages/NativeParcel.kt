package com.habitrpg.shared.habitica.nativePackages

expect class NativeParcel {
    fun writeString(value: String?)
    fun writeByte(value: Byte)
    fun writeInt(value: Int)
    fun writeLong(value: Long)

    fun readString(): String
    fun readByte(): Byte
    fun readInt(): Int
    fun readLong(): Long
}