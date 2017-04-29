package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.inventory.Customization;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public interface CustomizationRepository extends ContentRepository {
    Observable<RealmResults<Customization>> getCustomizations(String type, String category);
}
