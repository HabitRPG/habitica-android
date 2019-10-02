package com.habitrpg.android.habitica.data.local

import com.habitrpg.shared.habitica.models.inventory.Customization

import io.reactivex.Flowable
import io.realm.RealmResults

interface CustomizationLocalRepository : ContentLocalRepository {
    fun getCustomizations(type: String, category: String?, onlyAvailable: Boolean): Flowable<RealmResults<Customization>>
}
