package com.habitrpg.android.habitica.interactors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.FrameLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.views.PixelArtView
import kotlinx.coroutines.MainScope
import javax.inject.Inject

class HatchPetUseCase @Inject
constructor(
    private val inventoryRepository: InventoryRepository) : FlowUseCase<HatchPetUseCase.RequestValues, Items?>() {
    override suspend fun run(requestValues: RequestValues): Items? {
        return inventoryRepository.hatchPet(requestValues.egg, requestValues.potion) {
            val petWrapper = View.inflate(requestValues.context, R.layout.pet_imageview, null) as? FrameLayout
            val petImageView = petWrapper?.findViewById(R.id.pet_imageview) as? PixelArtView

            petImageView?.loadImage("stable_Pet-" + requestValues.egg.key + "-" + requestValues.potion.key)
            val potionName = requestValues.potion.text
            val eggName = requestValues.egg.text
            val dialog = HabiticaAlertDialog(requestValues.context)
            dialog.setTitle(requestValues.context.getString(R.string.hatched_pet_title, potionName, eggName))
            dialog.setAdditionalContentView(petWrapper)
            dialog.addButton(R.string.equip, true) { _, _ ->
                MainScope().launchCatching {
                    inventoryRepository.equip("pet", requestValues.egg.key + "-" + requestValues.potion.key)
                }
            }
            dialog.addButton(R.string.share, false) { hatchingDialog, _ ->
                val message = requestValues.context.getString(R.string.share_hatched, potionName, eggName)
                val petImageSideLength = 140
                val sharedImage = Bitmap.createBitmap(petImageSideLength, petImageSideLength, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(sharedImage)
                petImageView?.drawable?.setBounds(0, 0, petImageSideLength, petImageSideLength)
                petImageView?.drawable?.draw(canvas)
                (requestValues.context as? BaseActivity)?.shareContent("hatchedPet", message, sharedImage)
                hatchingDialog.dismiss()
            }
            dialog.setExtraCloseButtonVisibility(View.VISIBLE)
            dialog.enqueue()
        }
    }

    class RequestValues(val potion: HatchingPotion, val egg: Egg, val context: Context) : FlowUseCase.RequestValues
}
