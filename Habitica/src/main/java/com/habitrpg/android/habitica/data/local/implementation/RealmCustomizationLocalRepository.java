package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.CustomizationLocalRepository;
import com.habitrpg.android.habitica.models.inventory.Customization;

import java.util.Date;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import rx.Observable;


public class RealmCustomizationLocalRepository extends RealmContentLocalRepository implements CustomizationLocalRepository {

    public RealmCustomizationLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<RealmResults<Customization>> getCustomizations(String type, String category, boolean onlyAvailable) {
        RealmQuery<Customization> query = realm.where(Customization.class)
                .equalTo("type", type)
                .equalTo("category", category);
        if (onlyAvailable) {
            Date today = new Date();
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
                    .or()
                    .equalTo("purchased", true)
                    .endGroup();
        }
        return query
                .findAllSorted("customizationSet")
                .asObservable()
                .filter(RealmResults::isLoaded);
    }
}
