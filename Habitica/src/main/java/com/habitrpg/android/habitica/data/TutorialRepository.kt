package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.TutorialStep
import io.reactivex.rxjava3.core.Flowable
import io.realm.RealmResults

interface TutorialRepository : BaseRepository {

    fun getTutorialStep(key: String): Flowable<TutorialStep>
    fun getTutorialSteps(keys: List<String>): Flowable<RealmResults<TutorialStep>>

}
