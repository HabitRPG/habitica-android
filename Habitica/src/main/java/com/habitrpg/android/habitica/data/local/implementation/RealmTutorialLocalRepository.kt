package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.TutorialLocalRepository
import com.habitrpg.android.habitica.models.TutorialStep
import hu.akarnokd.rxjava3.bridge.RxJavaBridge
import io.reactivex.rxjava3.core.Flowable
import io.realm.Realm

class RealmTutorialLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), TutorialLocalRepository {

    override fun getTutorialStep(key: String): Flowable<TutorialStep> {
        if (realm.isClosed) return Flowable.empty()
        return RxJavaBridge.toV3Flowable(
            realm.where(TutorialStep::class.java).equalTo("identifier", key)
                .findAll()
                .asFlowable()
                .filter { realmObject -> realmObject.isLoaded && realmObject.isValid && realmObject.isNotEmpty() }
                .map { steps ->
                    return@map if (steps.isEmpty()) {
                        val step = TutorialStep()
                        step.identifier = key
                        val list = ArrayList<TutorialStep>()
                        list.add(step)
                        list
                    } else {
                        steps
                    }
                }
                .map { steps -> steps.first() }
        )
    }

    override fun getTutorialSteps(keys: List<String>): Flowable<out List<TutorialStep>> {
        if (realm.isClosed) return Flowable.empty()
        return RxJavaBridge.toV3Flowable(
            realm.where(TutorialStep::class.java)
                .`in`("identifier", keys.toTypedArray())
                .findAll()
                .asFlowable()
                .filter { it.isLoaded }
        )
    }
}
