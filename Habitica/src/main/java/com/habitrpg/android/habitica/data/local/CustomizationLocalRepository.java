package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.inventory.Customization;

import io.realm.RealmResults;
import rx.Observable;

public interface CustomizationLocalRepository extends ContentLocalRepository {
    Observable<RealmResults<Customization>> getCustomizations(String type, String category, boolean onlyAvailable);
}
