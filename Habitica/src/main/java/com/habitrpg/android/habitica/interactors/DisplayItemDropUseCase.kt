package com.habitrpg.android.habitica.interactors

import android.os.Handler
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.habitrpg.android.habitica.executors.PostExecutionThread
import com.habitrpg.android.habitica.executors.ThreadExecutor
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.models.responses.TaskScoringResult
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import io.reactivex.Flowable
import javax.inject.Inject

class DisplayItemDropUseCase @Inject
constructor(private val soundManager: SoundManager, threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) : UseCase<DisplayItemDropUseCase.RequestValues, Void>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(requestValues: RequestValues): Flowable<Void> {
        return Flowable.defer {
            val data = requestValues.data

            if (data?.drop != null) {
                Handler().postDelayed({
                    HabiticaSnackbar.showSnackbar(requestValues.snackbarTargetView,
                            data.drop?.dialog, HabiticaSnackbar.SnackbarDisplayType.DROP)
                    soundManager.loadAndPlayAudio(SoundManager.SoundItemDrop)
                }, 3000L)
            }

            Flowable.empty<Void>()
        }
    }

    class RequestValues(val data: TaskScoringResult?, val context: AppCompatActivity, val snackbarTargetView: ViewGroup) : UseCase.RequestValues
}