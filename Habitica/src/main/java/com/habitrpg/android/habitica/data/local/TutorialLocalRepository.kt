package com.habitrpg.android.habitica.data.local

import com.habitrpg.shared.habitica.models.TutorialStep
import io.reactivex.Flowable
import io.realm.RealmResults

interface TutorialLocalRepository : BaseLocalRepository {

    fun getTutorialStep(key: String): Flowable<TutorialStep>
    fun getTutorialSteps(keys: List<String>): Flowable<RealmResults<TutorialStep>>
}
