package com.habitrpg.android.habitica.interactors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.MountImageviewBinding
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.theme.HabiticaTheme
import com.habitrpg.android.habitica.ui.views.BackgroundScene
import com.habitrpg.android.habitica.ui.views.SnackbarActivity
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.launchCatching
import com.habitrpg.shared.habitica.models.responses.FeedResponse
import kotlinx.coroutines.MainScope
import javax.inject.Inject

class FeedPetUseCase @Inject
constructor(
    private val inventoryRepository: InventoryRepository
) : UseCase<FeedPetUseCase.RequestValues, FeedResponse?>() {
    override suspend fun run(requestValues: RequestValues): FeedResponse? {
        val feedResponse = inventoryRepository.feedPet(requestValues.pet, requestValues.food)
        (requestValues.context as? SnackbarActivity)?.showSnackbar(content = feedResponse?.message)
        if (feedResponse?.value == -1) {
            val mountWrapper = MountImageviewBinding.inflate(requestValues.context.layoutInflater)

            mountWrapper.mountImageview.setMount(requestValues.pet.key)
            val currentActivity =
                HabiticaBaseApplication.getInstance(requestValues.context)?.currentActivity?.get()
            val dialog = HabiticaAlertDialog(requestValues.context)
            if (currentActivity != null) {
                mountWrapper.backgroundView.setContent {
                    HabiticaTheme {
                        BackgroundScene(Modifier.clip(HabiticaTheme.shapes.large))
                    }
                }
                dialog.window?.let {
                    mountWrapper.root.setViewTreeSavedStateRegistryOwner(currentActivity)
                    it.decorView.setViewTreeSavedStateRegistryOwner(currentActivity)
                    mountWrapper.root.setViewTreeLifecycleOwner(currentActivity)
                    it.decorView.setViewTreeLifecycleOwner(currentActivity)
                }
            }
            dialog.setTitle(
                requestValues.context.getString(
                    R.string.evolved_pet_title,
                    requestValues.pet.text
                )
            )
            dialog.isCelebratory = true
            dialog.setAdditionalContentView(mountWrapper.root)
            dialog.addButton(R.string.onwards, true)
            dialog.addButton(R.string.share, false) { hatchingDialog, _ ->
                val message =
                    requestValues.context.getString(
                        R.string.share_raised,
                        requestValues.pet.text
                    )
                MainScope().launchCatching {
                    ShareMountUseCase().callInteractor(ShareMountUseCase.RequestValues(
                        requestValues.pet.key, message, requestValues.context
                    ))
                }
                hatchingDialog.dismiss()
            }
            dialog.enqueue()
        }
        return feedResponse
    }

    class RequestValues(val pet: Pet, val food: Food, val context: Context) :
        UseCase.RequestValues
}
