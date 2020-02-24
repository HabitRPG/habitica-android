package com.habitrpg.shared.habitica.nativePackages

expect class NativeDate() {
    fun before(value: NativeDate): Boolean
    fun after(value: NativeDate): Boolean
}