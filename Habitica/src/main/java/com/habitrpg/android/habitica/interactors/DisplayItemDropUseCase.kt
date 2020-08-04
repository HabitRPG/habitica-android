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
import java.lang.StringBuilder
import javax.inject.Inject

class DisplayItemDropUseCase @Inject
constructor(private val soundManager: SoundManager, threadExecutor: ThreadExecutor, postExecutionThread: PostExecutionThread) : UseCase<DisplayItemDropUseCase.RequestValues, Void>(threadExecutor, postExecutionThread) {

    override fun buildUseCaseObservable(requestValues: RequestValues): Flowable<Void> {
        return Flowable.defer {
            val data = requestValues.data
            var snackbarText = StringBuilder(data?.drop?.dialog ?: "")

            if (data?.questItemsFound ?: 0 > 0 && requestValues.showQuestItems) {
                if (snackbarText.isNotEmpty())
                    snackbarText.append('\n')
                snackbarText.append("+${data!!.questItemsFound} quest items")
            }

            if (snackbarText.isNotEmpty()) {
                Handler().postDelayed({
                    HabiticaSnackbar.showSnackbar(requestValues.snackbarTargetView,
                            snackbarText, HabiticaSnackbar.SnackbarDisplayType.DROP)
                    soundManager.loadAndPlayAudio(SoundManager.SoundItemDrop)
                }, 3000L)
            }

            Flowable.empty<Void>()
        }
    }

    class RequestValues(val data: TaskScoringResult?, val context: AppCompatActivity, val snackbarTargetView: ViewGroup, val showQuestItems: Boolean) : UseCase.RequestValues
}