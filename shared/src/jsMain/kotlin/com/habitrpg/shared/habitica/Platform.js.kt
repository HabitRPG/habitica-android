package com.habitrpg.shared.habitica

actual class Platform actual constructor() {
    actual val platform: String
        get() = "JS!"
}

actual interface HParcelable
actual class HParcel
