package com.habitrpg.shared.habitica

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

actual class Platform actual constructor() {
    actual val platform: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

actual typealias HParcelable = Parcelable
actual typealias HParcelize = Parcelize