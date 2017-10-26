package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.CustomizationRepository;
import com.habitrpg.android.habitica.data.local.CustomizationLocalRepository;
import com.habitrpg.android.habitica.models.inventory.Customization;

import io.realm.RealmResults;
import rx.Observable;

public class CustomizationRepositoryImpl extends ContentRepositoryImpl<CustomizationLocalRepository> implements CustomizationRepository {

    public CustomizationRepositoryImpl(CustomizationLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<RealmResults<Customization>> getCustomizations(String type, String category, boolean onlyAvailable) {
        return localRepository.getCustomizations(type, category, onlyAvailable);
    }
}
