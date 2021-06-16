package com.habitrpg.android.habitica.data.local

import com.habitrpg.android.habitica.models.TutorialStep
import io.reactivex.rxjava3.core.Flowable

interface TutorialLocalRepository : BaseLocalRepository {

    fun getTutorialStep(key: String): Flowable<TutorialStep>
    fun getTutorialSteps(keys: List<String>): Flowable<out List<TutorialStep>>
}
