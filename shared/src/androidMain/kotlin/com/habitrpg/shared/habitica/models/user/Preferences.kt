package com.habitrpg.shared.habitica.models.user

import java.util.*

actual fun nativeHasTaskBasedAllocation(allocationMode: String?, automaticAllocation: Boolean): Boolean {
    return allocationMode?.toLowerCase(Locale.ROOT) == "taskbased" && automaticAllocation
}