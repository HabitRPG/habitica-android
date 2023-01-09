package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.TutorialLocalRepository
import com.habitrpg.android.habitica.models.TutorialStep
import io.realm.Realm
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map

class RealmTutorialLocalRepository(realm: Realm) : RealmBaseLocalRepository(realm), TutorialLocalRepository {

    override fun getTutorialStep(key: String): Flow<TutorialStep> {
        if (realm.isClosed) return emptyFlow()
        return realm.where(TutorialStep::class.java).equalTo("identifier", key)
                .findAll()
                .toFlow()
                .filter { realmObject -> realmObject.isLoaded && realmObject.isValid && realmObject.isNotEmpty() }
                .map { steps -> steps.first() }
            .filterNotNull()
    }

    override fun getTutorialSteps(keys: List<String>): Flow<out List<TutorialStep>> {
        if (realm.isClosed) return emptyFlow()
        return realm.where(TutorialStep::class.java)
                .`in`("identifier", keys.toTypedArray())
                .findAll()
                .toFlow()
                .filter { it.isLoaded }
    }
}
