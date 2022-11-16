package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.inventory.Customization
import kotlinx.coroutines.flow.Flow

interface CustomizationRepository : BaseRepository {
    fun getCustomizations(type: String, category: String?, onlyAvailable: Boolean): Flow<List<Customization>>
}
