package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.inventory.Customization;

import java.util.List;

import rx.Observable;

public interface CustomizationLocalRepository extends BaseLocalRepository {
    Observable<List<Customization>> getCustomizations(String type, String category);
}
