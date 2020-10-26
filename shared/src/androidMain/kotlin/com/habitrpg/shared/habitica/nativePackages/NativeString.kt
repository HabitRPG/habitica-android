package com.habitrpg.shared.habitica.nativePackages

import com.habitrpg.shared.habitica.R

actual class NativeString {
    actual companion object {
        actual val healer: Int
            get() = R.string.healer
        actual val warrior: Int
            get() = R.string.warrior
        actual val rogue: Int
            get() = R.string.rogue
        actual val mage: Int
            get() = R.string.mage

    }
}