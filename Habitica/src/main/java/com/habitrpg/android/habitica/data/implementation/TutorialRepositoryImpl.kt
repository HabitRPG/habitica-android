package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.data.local.TutorialLocalRepository
import com.habitrpg.android.habitica.models.TutorialStep

import io.realm.RealmResults
import rx.Observable


class TutorialRepositoryImpl(localRepository: TutorialLocalRepository, apiClient: ApiClient) : BaseRepositoryImpl<TutorialLocalRepository>(localRepository, apiClient), TutorialRepository {

    override fun getTutorialStep(key: String): Observable<TutorialStep> =
            localRepository.getTutorialStep(key)

    override fun getTutorialSteps(keys: List<String>): Observable<RealmResults<TutorialStep>> =
            localRepository.getTutorialSteps(keys)
}
