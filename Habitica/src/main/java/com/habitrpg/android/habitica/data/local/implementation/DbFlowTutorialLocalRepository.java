package com.habitrpg.android.habitica.data.local.implementation;

import com.habitrpg.android.habitica.data.local.TutorialLocalRepository;
import com.habitrpg.android.habitica.models.TutorialStep;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.language.Select;

import java.util.List;

import rx.Observable;

public class DbFlowTutorialLocalRepository implements TutorialLocalRepository {
    @Override
    public void close() {

    }

    @Override
    public Observable<TutorialStep> getTutorialStep(String key) {
        return Observable.defer(() -> Observable.just(new Select().from(TutorialStep.class).where(Condition.column("key").eq(key)).querySingle()));
    }

    @Override
    public Observable<List<TutorialStep>> getTutorialSteps(List<String> keys) {
        return Observable.defer(() -> Observable.just(new Select().from(TutorialStep.class)
                .where(Condition.column("key").in(keys))
                .queryList()));
    }
}
