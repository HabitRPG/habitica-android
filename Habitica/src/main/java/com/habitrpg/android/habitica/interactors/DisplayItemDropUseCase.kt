package com.habitrpg.android.habitica.interactors

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.executors.PostExecutionThread
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.common.habitica.models.responses.TaskScoringResult
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DisplayItemDropUseCase @Inject
constructor(private val soundManager: SoundManager, postExecutionThread: PostExecutionThread) : UseCase<DisplayItemDropUseCase.RequestValues, Void>(postExecutionThread) {

    override fun buildUseCaseObservable(requestValues: RequestValues): Flowable<Void> {
        return Flowable.defer {
            val data = requestValues.data
            val snackbarText = StringBuilder(data?.drop?.dialog ?: "")

            if ((data?.questItemsFound ?: 0) > 0 && requestValues.showQuestItems) {
                if (snackbarText.isNotEmpty())
                    snackbarText.append('\n')
                snackbarText.append(requestValues.context.getString(R.string.quest_items_found, data!!.questItemsFound))
            }

            if (snackbarText.isNotEmpty()) {
                MainScope().launch(context = Dispatchers.Main) {
                    delay(3000L)
                    HabiticaSnackbar.showSnackbar(
                        requestValues.snackbarTargetView,
                        snackbarText, HabiticaSnackbar.SnackbarDisplayType.DROP, true
                    )
                    soundManager.loadAndPlayAudio(SoundManager.SoundItemDrop)
                }
            }

            Flowable.empty()
        }
    }

    class RequestValues(
        val data: TaskScoringResult?,
        val context: AppCompatActivity,
        val snackbarTargetView: ViewGroup,
        val showQuestItems: Boolean
    ) : UseCase.RequestValues
}
