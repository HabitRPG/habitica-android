package com.habitrpg.shared.habitica.nativePackages

expect abstract class NativeRealmObject() {
    fun isManaged(): Boolean
    fun isValid(): Boolean
}
