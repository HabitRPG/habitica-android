package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.CustomizationLocalRepository;
import com.habitrpg.android.habitica.models.inventory.Customization;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;


public class RealmCustomizationLocalRepository extends RealmContentLocalRepository implements CustomizationLocalRepository {

    public RealmCustomizationLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<RealmResults<Customization>> getCustomizations(String type, String category) {
        return realm.where(Customization.class)
                .equalTo("type", type)
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }
}
