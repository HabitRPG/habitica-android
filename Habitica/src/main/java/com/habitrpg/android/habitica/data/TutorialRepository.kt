package com.habitrpg.android.habitica.data

import com.habitrpg.android.habitica.models.TutorialStep
import kotlinx.coroutines.flow.Flow

interface TutorialRepository : BaseRepository {

    fun getTutorialStep(key: String): Flow<TutorialStep>
    fun getTutorialSteps(keys: List<String>): Flow<out List<TutorialStep>>
}
