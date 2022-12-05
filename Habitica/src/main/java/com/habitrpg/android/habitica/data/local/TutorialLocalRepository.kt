package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.TutorialStep
import kotlinx.coroutines.flow.Flow

interface TutorialLocalRepository : BaseLocalRepository {

    fun getTutorialStep(key: String): Flow<TutorialStep>
    fun getTutorialSteps(keys: List<String>): Flow<List<TutorialStep>>
}
