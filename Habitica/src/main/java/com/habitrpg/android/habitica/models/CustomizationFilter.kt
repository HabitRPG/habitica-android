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

    override fun equals(other: Any?): Boolean {
        if (other is CustomizationFilter) {
            return onlyPurchased == other.onlyPurchased && ascending == other.ascending && months.size == other.months.size && months.containsAll(other.months)
        }
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = onlyPurchased.hashCode()
        result = 31 * result + ascending.hashCode()
        result = 31 * result + months.hashCode()
        return result
    }
}
