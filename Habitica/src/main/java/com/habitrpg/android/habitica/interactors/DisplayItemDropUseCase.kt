package com.habitrpg.android.habitica.interactors

import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.shared.habitica.models.responses.TaskScoringResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

class DisplayItemDropUseCase
    @Inject
    constructor(private val soundManager: SoundManager) :
    UseCase<DisplayItemDropUseCase.RequestValues, Unit>() {
        override suspend fun run(requestValues: RequestValues) {
            val data = requestValues.data
            val snackBarText = StringBuilder(data?.drop?.dialog ?: "")

            if ((data?.questItemsFound ?: 0) > 0 && requestValues.showQuestItems) {
                if (snackBarText.isNotEmpty()) {
                    snackBarText.append('\n')
                }
                snackBarText.append(
                    requestValues.context.getString(
                        R.string.quest_items_found,
                        data!!.questItemsFound,
                    ),
                )
            }

            if (snackBarText.isNotEmpty()) {
                MainScope().launch(context = Dispatchers.Main) {
                    delay(3000L)
                    HabiticaSnackbar.showSnackbar(
                        requestValues.snackBarTargetView,
                        snackBarText,
                        HabiticaSnackbar.SnackbarDisplayType.DROP,
                        true,
                    )
                    soundManager.loadAndPlayAudio(SoundManager.SOUND_ITEM_DROP)
                }
            }
            return
        }

        class RequestValues(
            val data: TaskScoringResult?,
            val context: AppCompatActivity,
            val snackBarTargetView: ViewGroup,
            val showQuestItems: Boolean,
        ) : UseCase.RequestValues
    }
