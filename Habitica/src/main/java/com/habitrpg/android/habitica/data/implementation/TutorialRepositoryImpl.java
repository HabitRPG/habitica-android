package com.habitrpg.android.habitica.data.implementation;

import com.habitrpg.android.habitica.data.ApiClient;
import com.habitrpg.android.habitica.data.TutorialRepository;
import com.habitrpg.android.habitica.data.local.TutorialLocalRepository;
import com.habitrpg.android.habitica.models.TutorialStep;

import java.util.List;

import rx.Observable;


public class TutorialRepositoryImpl extends BaseRepositoryImpl<TutorialLocalRepository> implements TutorialRepository {
    public TutorialRepositoryImpl(TutorialLocalRepository localRepository, ApiClient apiClient) {
        super(localRepository, apiClient);
    }

    @Override
    public Observable<TutorialStep> getTutorialStep(String key) {
        return localRepository.getTutorialStep(key);
    }

    @Override
    public Observable<List<TutorialStep>> getTutorialSteps(List<String> keys) {
        return localRepository.getTutorialSteps(keys);
    }
}
