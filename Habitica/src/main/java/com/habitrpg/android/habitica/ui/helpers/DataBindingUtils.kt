package com.habitrpg.android.habitica.ui.helpers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.TextView
import com.facebook.common.executors.CallerThreadExecutor
import com.facebook.common.references.CloseableReference
import com.facebook.datasource.DataSource
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber
import com.facebook.imagepipeline.image.CloseableImage
import com.facebook.imagepipeline.request.ImageRequestBuilder
import com.habitrpg.android.habitica.R

object DataBindingUtils {

    fun loadImage(view: SimpleDraweeView?, imageName: String) {
        loadImage(view, imageName, "png")
    }

    fun loadImage(view: SimpleDraweeView?, imageName: String?, imageFormat: String = "png") {
        if (view != null && imageName != null && view.visibility == View.VISIBLE) {
            view.setImageURI("https://habitica-assets.s3.amazonaws.com/mobileApp/images/$imageName.$imageFormat")
        }
    }

    fun loadImage(imageName: String, imageResult: (Bitmap) -> Unit) {
        loadImage(imageName, "png", imageResult)
    }

    fun loadImage(imageName: String, imageFormat: String = "png", imageResult: (Bitmap) -> Unit) {
        val imageRequest = ImageRequestBuilder
                .newBuilderWithSource(Uri.parse("https://habitica-assets.s3.amazonaws.com/mobileApp/images/$imageName.$imageFormat"))
                .build()

        val imagePipeline = Fresco.getImagePipeline()
        val dataSource = imagePipeline.fetchDecodedImage(imageRequest, this)

        dataSource.subscribe(object : BaseBitmapDataSubscriber() {
            override fun onFailureImpl(dataSource: DataSource<CloseableReference<CloseableImage>>?) {
                dataSource?.close()
            }

            public override fun onNewResultImpl(bitmap: Bitmap?) {
                if (dataSource.isFinished && bitmap != null) {
                    imageResult(bitmap)
                    dataSource.close()
                }
            }
        }, CallerThreadExecutor.getInstance())
    }

    fun setForegroundTintColor(view: TextView, color: Int) {
        var color = color
        if (color > 0) {
            color = ContextCompat.getColor(view.context, color)
        }
        view.setTextColor(color)
    }

    fun setRoundedBackground(view: View, color: Int) {
        val drawable = ResourcesCompat.getDrawable(view.resources, R.drawable.layout_rounded_bg, null)
        drawable?.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
        if (Build.VERSION.SDK_INT < 16) {
            view.setBackgroundDrawable(drawable)
        } else {
            view.background = drawable
        }
    }

    fun setRoundedBackgroundInt(view: View, color: Int) {
        if (color != 0) {
            setRoundedBackground(view, ContextCompat.getColor(view.context, color))
        }
    }

    class LayoutWeightAnimation(internal var view: View, internal var targetWeight: Float) : Animation() {
        private var initializeWeight: Float = 0.toFloat()

        private var layoutParams: LinearLayout.LayoutParams = view.layoutParams as LinearLayout.LayoutParams

        init {
            initializeWeight = layoutParams.weight
        }

        override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
            layoutParams.weight = initializeWeight + (targetWeight - initializeWeight) * interpolatedTime

            view.requestLayout()
        }

        override fun willChangeBounds(): Boolean = true
    }
}
