package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.data.local.TutorialLocalRepository
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import kotlinx.coroutines.flow.Flow

class TutorialRepositoryImpl(
    localRepository: TutorialLocalRepository,
    apiClient: ApiClient,
    authenticationHandler: AuthenticationHandler,
) : BaseRepositoryImpl<TutorialLocalRepository>(localRepository, apiClient, authenticationHandler),
    TutorialRepository {
    override fun getTutorialStep(key: String): Flow<TutorialStep> =
        localRepository.getTutorialStep(key)

    override fun getTutorialSteps(keys: List<String>): Flow<List<TutorialStep>> =
        localRepository.getTutorialSteps(keys)
}
