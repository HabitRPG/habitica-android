package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.TutorialLocalRepository;
import com.habitrpg.android.habitica.models.TutorialStep;

import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import rx.Observable;


public class RealmTutorialLocalRepository extends RealmBaseLocalRepository implements TutorialLocalRepository {

    public RealmTutorialLocalRepository(Realm realm) {
        super(realm);
    }

    @Override
    public Observable<TutorialStep> getTutorialStep(String key) {
        return realm.where(TutorialStep.class).equalTo("identifier", key)
                .findAllAsync()
                .asObservable()
                .filter(realmObject -> realmObject.isLoaded() && realmObject.isValid() && !realmObject.isEmpty())
                .map(steps -> steps.first())
                .cast(TutorialStep.class);
    }

    @Override
    public Observable<RealmResults<TutorialStep>> getTutorialSteps(List<String> keys) {
        return realm.where(TutorialStep.class)
                .in("identifier", (String[]) keys.toArray())
                .findAll()
                .asObservable()
                .filter(RealmResults::isLoaded);
    }
}
