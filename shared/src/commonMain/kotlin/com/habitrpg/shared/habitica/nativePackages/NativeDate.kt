package com.habitrpg.shared.habitica.nativePackages

expect class NativeDate constructor(val value: Long) {
    fun before(value: NativeDate): Boolean
    fun after(value: NativeDate): Boolean

    fun getTime(): Long
    fun setTime(value: Long)
}