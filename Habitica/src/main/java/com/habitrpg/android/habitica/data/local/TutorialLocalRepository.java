package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.TutorialStep;

import java.util.List;

import rx.Observable;

public interface TutorialLocalRepository extends BaseLocalRepository {

    Observable<TutorialStep> getTutorialStep(String key);
    Observable<List<TutorialStep>> getTutorialSteps(List<String> keys);
}
