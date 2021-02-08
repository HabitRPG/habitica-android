package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.data.local.TutorialLocalRepository
import com.habitrpg.android.habitica.models.TutorialStep
import io.reactivex.rxjava3.core.Flowable
import io.realm.RealmResults


class TutorialRepositoryImpl(localRepository: TutorialLocalRepository, apiClient: ApiClient, userID: String) : BaseRepositoryImpl<TutorialLocalRepository>(localRepository, apiClient, userID), TutorialRepository {

    override fun getTutorialStep(key: String): Flowable<TutorialStep> =
            localRepository.getTutorialStep(key)

    override fun getTutorialSteps(keys: List<String>): Flowable<RealmResults<TutorialStep>> =
            localRepository.getTutorialSteps(keys)
}
