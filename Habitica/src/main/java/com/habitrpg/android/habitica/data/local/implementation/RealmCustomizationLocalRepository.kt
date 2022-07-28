package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.CustomizationLocalRepository
import com.habitrpg.android.habitica.models.inventory.Customization
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Flowable
import io.realm.Realm
import java.util.Date

class RealmCustomizationLocalRepository(realm: Realm) : RealmContentLocalRepository(realm), CustomizationLocalRepository {

    override fun getCustomizations(type: String, category: String?, onlyAvailable: Boolean): Flowable<out List<Customization>> {
        var query = realm.where(Customization::class.java)
            .equalTo("type", type)
            .equalTo("category", category)
        if (onlyAvailable) {
            val today = Date()
            query = query
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
        return RxJavaBridge.toV3Flowable(
            query
                .sort("customizationSet")
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
                .map { it }
        )
    }
}
