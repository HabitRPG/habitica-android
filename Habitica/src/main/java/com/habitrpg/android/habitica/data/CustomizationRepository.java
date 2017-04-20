package com.habitrpg.android.habitica.data;

import com.habitrpg.android.habitica.models.inventory.Customization;

import java.util.List;

import rx.Observable;

public interface CustomizationRepository extends ContentRepository {
    Observable<List<Customization>> getCustomizations(String type, String category);
}
