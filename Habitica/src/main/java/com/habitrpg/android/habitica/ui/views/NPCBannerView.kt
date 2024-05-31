package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.graphics.drawable.toBitmap
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.NpcBannerBinding
import com.habitrpg.common.habitica.extensions.DataBindingUtils
import com.habitrpg.common.habitica.extensions.layoutInflater
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class NPCBannerView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    private val binding = NpcBannerBinding.inflate(context.layoutInflater, this)

    var shopSpriteSuffix: String? = null
        set(value) {

            field =
                if (value.isNullOrEmpty()) {
                    ""
                } else if (value.startsWith("_")) {
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
        binding.backgroundView.scaleType = ImageView.ScaleType.FIT_START
        binding.sceneView.scaleType = ImageView.ScaleType.MATRIX
        val height = context.resources.getDimension(R.dimen.shop_height).toInt()

        DataBindingUtils.loadImage(context, identifier + "_background" + shopSpriteSuffix) {
            val aspectRatio = it.intrinsicWidth / it.intrinsicHeight.toFloat()
            val width = (height * aspectRatio).roundToInt()
            val drawable = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(it.toBitmap(), width, height, false))
            drawable.tileModeX = Shader.TileMode.REPEAT
            MainScope().launch {
                binding.backgroundView.background = drawable
            }
        }

        DataBindingUtils.loadImage(context, identifier + "_scene" + shopSpriteSuffix) {
            val aspectRatio = it.intrinsicWidth / it.intrinsicHeight.toFloat()
            val width = (height * aspectRatio).roundToInt()
            val drawable = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(it.toBitmap(), width, height, false))
            drawable.tileModeX = Shader.TileMode.CLAMP
            MainScope().launch {
                binding.sceneView.background = drawable
            }
        }
    }
}
