package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.data.local.TutorialLocalRepository
import com.habitrpg.android.habitica.models.TutorialStep
import kotlinx.coroutines.flow.Flow

class TutorialRepositoryImpl(
    localRepository: TutorialLocalRepository,
    apiClient: ApiClient,
    userID: String
) : BaseRepositoryImpl<TutorialLocalRepository>(localRepository, apiClient, userID), TutorialRepository {

    override fun getTutorialStep(key: String): Flow<TutorialStep> =
        localRepository.getTutorialStep(key)

    override fun getTutorialSteps(keys: List<String>): Flow<List<TutorialStep>> =
        localRepository.getTutorialSteps(keys)
}
