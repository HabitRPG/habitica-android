package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.TutorialLocalRepository
import com.habitrpg.android.habitica.models.TutorialStep
import io.realm.Realm
import io.realm.RealmResults
import rx.Observable


class RealmTutorialLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), TutorialLocalRepository {

    override fun getTutorialStep(key: String): Observable<TutorialStep> {
        return realm.where(TutorialStep::class.java).equalTo("identifier", key)
                .findAllAsync()
                .asObservable()
                .filter { realmObject -> realmObject.isLoaded && realmObject.isValid && !realmObject.isEmpty() }
                .map { steps -> steps.first() }
                .cast(TutorialStep::class.java)
    }

    override fun getTutorialSteps(keys: List<String>): Observable<RealmResults<TutorialStep>> {
        return realm.where(TutorialStep::class.java)
                .`in`("identifier", keys.toTypedArray())
                .findAll()
                .asObservable()
                .filter({ it.isLoaded })
    }
}
