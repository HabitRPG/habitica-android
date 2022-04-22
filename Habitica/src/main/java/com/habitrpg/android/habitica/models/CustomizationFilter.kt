package com.habitrpg.android.habitica.models

data class CustomizationFilter(
    var onlyPurchased: Boolean = false,
    var ascending: Boolean = false,
    var months: MutableList<String> = mutableListOf()
) {
    val isFiltering: Boolean
    get() {
        return onlyPurchased || months.isNotEmpty()
    }
}
