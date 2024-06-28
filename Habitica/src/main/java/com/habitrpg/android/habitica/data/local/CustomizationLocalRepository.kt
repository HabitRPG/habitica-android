package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.inventory.Customization
import kotlinx.coroutines.flow.Flow

interface CustomizationLocalRepository : ContentLocalRepository {
    fun getCustomizations(
        type: String,
        category: String?,
        onlyAvailable: Boolean
    ): Flow<List<Customization>>
}
