package com.habitrpg.android.habitica.ui.views

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer

class NPCBannerView(context: Context?, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val backgroundView: ImageView by bindView(R.id.backgroundView)
    private val sceneView: SimpleDraweeView by bindView(R.id.sceneView)

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

    init {
        context?.layoutInflater?.inflate(R.layout.npc_banner, this)
    }

    private fun setImage() {
        DataBindingUtils.loadImage(sceneView, identifier + "_scene" + shopSpriteSuffix)

        backgroundView.scaleType = ImageView.ScaleType.FIT_START

        DataBindingUtils.loadImage(identifier + "_background" + shopSpriteSuffix) {
            val aspectRatio = it.width / it.height.toFloat()
            val height = context.resources.getDimension(R.dimen.shop_height).toInt()
            val width = Math.round(height * aspectRatio)
            val drawable = BitmapDrawable(context.resources, Bitmap.createScaledBitmap(it, width, height, false))
            drawable.tileModeX = Shader.TileMode.REPEAT
            Observable.just(drawable)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(Consumer {
                        backgroundView.background = it
                    }, RxErrorHandler.handleEmptyError())
        }
    }
}