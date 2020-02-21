package com.habitrpg.shared.habitica.nativePackages

expect abstract class NativeContext {
    fun getString(value: String): String
}