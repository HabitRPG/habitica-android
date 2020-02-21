package com.habitrpg.shared.habitica.nativePackages

expect class NativeSparseIntArray() {
    fun put(k: Int, v: Int)
    fun get(k: Int, default: Int): Int
}
