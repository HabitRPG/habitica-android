package com.habitrpg.android.habitica.interactors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.findViewTreeCompositionContext
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.databinding.PetImageviewBinding
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.views.BackgroundScene
import com.habitrpg.common.habitica.extensionsCommon.dpToPx
import com.habitrpg.common.habitica.extensionsCommon.layoutInflater
import com.habitrpg.common.habitica.extensionsCommon.loadImage
import com.habitrpg.common.habitica.theme.HabiticaTheme
import kotlinx.coroutines.delay

class SharePetUseCase : UseCase<SharePetUseCase.RequestValues, Unit>() {
    class RequestValues(val petKey: String, val message: String, val context: Context) :
        UseCase.RequestValues

    override suspend fun run(requestValues: RequestValues) {
        val petWrapper = PetImageviewBinding.inflate(requestValues.context.layoutInflater)
        petWrapper.petImageview.loadImage("stable_Pet-" + requestValues.petKey)
        petWrapper.root.visibility = View.INVISIBLE
        val currentActivity =
            HabiticaBaseApplication.getInstance(requestValues.context)?.currentActivity?.get()
        val containerView = (currentActivity?.window?.decorView as? ViewGroup)
        containerView?.addView(petWrapper.root)
        currentActivity?.let {
            petWrapper.backgroundView.setContent {
                HabiticaTheme {
                    BackgroundScene(Modifier.clip(HabiticaTheme.shapes.large))
                }
            }
            petWrapper.backgroundView.setParentCompositionContext(currentActivity.toolbar?.findViewTreeCompositionContext())
            petWrapper.root.setViewTreeSavedStateRegistryOwner(currentActivity)
            petWrapper.root.setViewTreeLifecycleOwner(currentActivity)
        }
        val width =
            if (petWrapper.root.width > 0) petWrapper.root.width else 300.dpToPx(requestValues.context)
        val height = 124.dpToPx(requestValues.context)
        val sharedImage =
            Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888,
            )
        val canvas = Canvas(sharedImage)
        var attempts = 0
        while (petWrapper.petImageview.bitmap == null && attempts < 200) {
            delay(100)
            attempts++
        }
        petWrapper.root.doOnNextLayout {
            petWrapper.root.draw(canvas)
            (
                (requestValues.context as? BaseActivity) ?: HabiticaBaseApplication.getInstance(
                    requestValues.context,
                )?.currentActivity?.get()
            )?.shareContent("pet", requestValues.message, sharedImage)
            containerView?.removeView(petWrapper.root)
        }
        val m = FrameLayout.LayoutParams(width, height)
        petWrapper.root.layoutParams = m
    }
}
