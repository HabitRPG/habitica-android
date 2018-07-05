package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.inventory.Customization;

import io.reactivex.Flowable;
import io.realm.RealmResults;

public interface CustomizationRepository extends ContentRepository {
    Flowable<RealmResults<Customization>> getCustomizations(String type, String category, boolean onlyAvailable);
}
