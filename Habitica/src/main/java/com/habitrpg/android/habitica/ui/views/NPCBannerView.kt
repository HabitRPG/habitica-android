package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.graphics.drawable.toBitmap
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.NpcBannerBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import kotlin.math.roundToInt

class NPCBannerView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {
    private val binding = NpcBannerBinding.inflate(context.layoutInflater, this)

    var shopSpriteSuffix: String = ""
        set(value) {
            field = if (value.isEmpty() || value.startsWith("_")) {
                value
            } else {
                "_$value"
            }
            if (identifier.isNotEmpty()) {
                setImage()
            }
        }
    var identifier: String = ""
    set(value) {
        field = value
        setImage()
    }

    private fun setImage() {
        DataBindingUtils.loadImage(binding.sceneView, identifier + "_scene" + shopSpriteSuffix)

        binding.backgroundView.scaleType = ImageView.ScaleType.FIT_START

        DataBindingUtils.loadImage(context, identifier + "_background" + shopSpriteSuffix) {
            val aspectRatio = it.intrinsicWidth / it.intrinsicHeight.toFloat()
            val height = context.resources.getDimension(R.dimen.shop_height).toInt()
            val width = (height * aspectRatio).roundToInt()
            val drawable = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(it.toBitmap(), width, height, false))
            drawable.tileModeX = Shader.TileMode.REPEAT
            Observable.just(drawable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        binding.backgroundView.background = it
                    }, RxErrorHandler.handleEmptyError())
        }
    }
}