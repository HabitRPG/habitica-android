package com.habitrpg.android.habitica.interactors

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.core.view.doOnNextLayout
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.habitrpg.android.habitica.HabiticaBaseApplication
import com.habitrpg.android.habitica.databinding.MountImageviewBinding
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.views.BackgroundScene
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.theme.HabiticaTheme
import kotlinx.coroutines.delay

class ShareMountUseCase : UseCase<ShareMountUseCase.RequestValues, Unit>() {
    class RequestValues(val mountKey: String, val message: String, val context: Context) :
        UseCase.RequestValues

    override suspend fun run(requestValues: RequestValues) {
        val mountWrapper = MountImageviewBinding.inflate(requestValues.context.layoutInflater)
        mountWrapper.root.visibility = View.INVISIBLE
        val width =
            if (mountWrapper.root.width > 0) mountWrapper.root.width else 300.dpToPx(requestValues.context)
        val height = 124.dpToPx(requestValues.context)
        mountWrapper.root.layout(0, 0, width, height)
        mountWrapper.mountImageview.setMount(requestValues.mountKey)
        val currentActivity =
            HabiticaBaseApplication.getInstance(requestValues.context)?.currentActivity?.get()
        // Add the view to the decorView so that it can be layouted
        val containerView = (currentActivity?.window?.decorView as? ViewGroup)
        containerView?.addView(mountWrapper.root)
        if (currentActivity != null) {
            mountWrapper.backgroundView.setContent {
                HabiticaTheme {
                    BackgroundScene(Modifier.clip(HabiticaTheme.shapes.large))
                }
            }
            mountWrapper.root.setViewTreeSavedStateRegistryOwner(currentActivity)
            mountWrapper.root.setViewTreeLifecycleOwner(currentActivity)
        }
        mountWrapper.backgroundView.layout(0, 0, width, height)
        mountWrapper.mountImageview.layout(0, 0, width, height)
        val sharedImage =
            Bitmap.createBitmap(
                width,
                height,
                Bitmap.Config.ARGB_8888
            )
        val canvas = Canvas(sharedImage)
        var attempts = 0
        while (!mountWrapper.mountImageview.hasLoadedImages && attempts < 200) {
            delay(100)
            attempts++
        }
        // Draw it to the canvas once it's layouted
        mountWrapper.root.doOnNextLayout {
            mountWrapper.root.draw(canvas)
            (
                (requestValues.context as? BaseActivity) ?: HabiticaBaseApplication.getInstance(
                    requestValues.context
                )?.currentActivity?.get()
                )?.shareContent("pet", requestValues.message, sharedImage)
            containerView?.removeView(mountWrapper.root)
        }
        // trigger layout
        val m = FrameLayout.LayoutParams(width, height)
        mountWrapper.root.layoutParams = m
    }
}
