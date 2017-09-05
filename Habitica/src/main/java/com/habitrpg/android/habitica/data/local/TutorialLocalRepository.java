package com.habitrpg.android.habitica.data.local;

import com.habitrpg.android.habitica.models.TutorialStep;

import java.util.List;

import io.realm.RealmResults;
import rx.Observable;

public interface TutorialLocalRepository extends BaseLocalRepository {

    Observable<TutorialStep> getTutorialStep(String key);
    Observable<RealmResults<TutorialStep>> getTutorialSteps(List<String> keys);
}
