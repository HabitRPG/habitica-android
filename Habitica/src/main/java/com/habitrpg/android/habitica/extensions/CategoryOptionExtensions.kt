package com.habitrpg.android.habitica.extensions

import android.content.Context
import com.habitrpg.android.habitica.models.social.CategoryOption
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.Group


fun CategoryOption.toFilterGroup(): Group {
    return Group().apply {
        id = key
        name = label
            .split('_')
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercaseChar() }
            }
    }
}

fun List<CategoryOption>.toFilterGroups(): List<Group> =
    map { it.toFilterGroup() }

fun List<Challenge>.filterByCategorySlugs(activeGroupIds: Set<String>): List<Challenge> {
    return this.filter { challenge ->
        challenge.categories.any { category ->
            activeGroupIds.contains(category.slug)
        }
    }
}
