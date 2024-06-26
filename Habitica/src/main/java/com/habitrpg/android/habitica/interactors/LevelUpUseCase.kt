package com.habitrpg.android.habitica.interactors

import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogLevelup10Binding
import com.habitrpg.android.habitica.helpers.SoundManager
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.common.habitica.views.AvatarView
import kotlinx.coroutines.MainScope
import javax.inject.Inject

class LevelUpUseCase
@Inject
constructor(
    private val soundManager: SoundManager,
    private val checkClassSelectionUseCase: CheckClassSelectionUseCase
) : UseCase<LevelUpUseCase.RequestValues, Stats?>() {
    override suspend fun run(requestValues: RequestValues): Stats? {
        soundManager.loadAndPlayAudio(SoundManager.SOUND_LEVEL_UP)
        val suppressedModals = requestValues.user.preferences?.suppressModals

        if (requestValues.newLevel == 10) {
            val binding = DialogLevelup10Binding.inflate(requestValues.activity.layoutInflater)
            binding.healerIconView.setImageBitmap(HabiticaIconsHelper.imageOfHealerLightBg())
            binding.mageIconView.setImageBitmap(HabiticaIconsHelper.imageOfMageLightBg())
            binding.rogueIconView.setImageBitmap(HabiticaIconsHelper.imageOfRogueLightBg())
            binding.warriorIconView.setImageBitmap(HabiticaIconsHelper.imageOfWarriorLightBg())

            val alert = HabiticaAlertDialog(requestValues.activity)
            alert.setTitle(
                requestValues.activity.getString(
                    R.string.levelup_header,
                    requestValues.newLevel
                )
            )
            alert.setAdditionalContentView(binding.root)
            alert.addButton(R.string.select_class, true) { _, _ ->
                MainScope().launchCatching {
                    showClassSelection(requestValues)
                }
            }
            alert.addButton(R.string.not_now, false)
            alert.isCelebratory = true

            if (!requestValues.activity.isFinishing) {
                alert.enqueue()
            }
        } else {
            if (suppressedModals?.levelUp == true) {
                HabiticaSnackbar.showSnackbar(
                    requestValues.snackbarTargetView,
                    requestValues.activity.getString(
                        R.string.levelup_header,
                        requestValues.newLevel
                    ),
                    HabiticaSnackbar.SnackbarDisplayType.SUCCESS,
                    true
                )
                return requestValues.user.stats
            }
            val customView =
                requestValues.activity.layoutInflater.inflate(R.layout.dialog_levelup, null)
            if (customView != null) {
                val dialogAvatarView = customView.findViewById<AvatarView>(R.id.avatarView)
                dialogAvatarView.setAvatar(requestValues.user)
            }

            val alert = HabiticaAlertDialog(requestValues.activity)
            alert.setTitle(
                requestValues.activity.getString(
                    R.string.levelup_header,
                    requestValues.newLevel
                )
            )
            alert.setAdditionalContentView(customView)
            alert.addButton(R.string.onwards, true) { _, _ ->
                MainScope().launchCatching {
                    showClassSelection(requestValues)
                }
            }
            alert.addButton(R.string.share, false) { _, _ ->
                MainScope().launchCatching {
                    val usecase = ShareAvatarUseCase()
                    usecase.callInteractor(
                        ShareAvatarUseCase.RequestValues(
                            requestValues.activity,
                            requestValues.user,
                            requestValues.activity.getString(
                                R.string.share_levelup,
                                requestValues.newLevel
                            ),
                            "levelup"
                        )
                    )
                }
            }
            alert.isCelebratory = true

            if (!requestValues.activity.isFinishing) {
                alert.enqueue()
            }
        }
        return requestValues.user.stats
    }

    private suspend fun showClassSelection(requestValues: RequestValues) {
        checkClassSelectionUseCase.callInteractor(
            CheckClassSelectionUseCase.RequestValues(
                requestValues.user,
                true,
                null,
                requestValues.activity
            )
        )
    }

    class RequestValues(
        val user: User,
        val level: Int?,
        val activity: BaseActivity,
        val snackbarTargetView: ViewGroup
    ) : UseCase.RequestValues {
        val newLevel: Int = level ?: 0
    }
}
