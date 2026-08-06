package com.habitrpg.android.habitica.data.local.implementation

import com.habitrpg.android.habitica.data.local.TutorialLocalRepository
import com.habitrpg.android.habitica.models.TutorialStep
import io.realm.Realm
import kotlinx.coroutines.flow.Flow

class RealmTutorialLocalRepository(
    realm: Realm,
) : RealmBaseLocalRepository(realm),
    TutorialLocalRepository {
    override fun getTutorialStep(key: String): Flow<TutorialStep> = safeFindOne {
        it.where(TutorialStep::class.java).equalTo("identifier", key)
    }

    override fun getTutorialSteps(keys: List<String>): Flow<List<TutorialStep>> = safeFindAll {
        it.where(TutorialStep::class.java).`in`("identifier", keys.toTypedArray())
    }
}
