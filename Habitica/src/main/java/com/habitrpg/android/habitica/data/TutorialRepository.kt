package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.TutorialStep

import io.realm.RealmResults
import rx.Observable

interface TutorialRepository : BaseRepository {

    fun getTutorialStep(key: String): Observable<TutorialStep>
    fun getTutorialSteps(keys: List<String>): Observable<RealmResults<TutorialStep>>

}
