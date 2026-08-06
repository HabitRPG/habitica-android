package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.CustomizationLocalRepository
import com.habitrpg.android.habitica.models.inventory.Customization
import io.realm.Realm
import kotlinx.coroutines.flow.Flow
import java.util.Date

class RealmCustomizationLocalRepository(
    realm: Realm,
) : RealmContentLocalRepository(realm),
    CustomizationLocalRepository {
    override fun getCustomizations(
        type: String,
        category: String?,
        onlyAvailable: Boolean,
    ): Flow<List<Customization>> = safeFindAll { realm ->
        var query =
            realm
                .where(Customization::class.java)
                .equalTo("type", type)
                .equalTo("category", category)
        if (onlyAvailable) {
            val today = Date()
            query =
                query
                    .beginGroup()
                    .beginGroup()
                    .lessThanOrEqualTo("availableFrom", today)
                    .greaterThanOrEqualTo("availableUntil", today)
                    .endGroup()
                    .or()
                    .beginGroup()
                    .isNull("availableFrom")
                    .isNull("availableUntil")
                    .endGroup()
                    .endGroup()
        }
        query.sort("customizationSet")
    }
}
