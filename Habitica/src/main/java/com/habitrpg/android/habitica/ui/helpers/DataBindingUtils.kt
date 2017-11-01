package com.habitrpg.android.habitica.ui.helpers

import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.support.v4.content.ContextCompat
import android.support.v4.content.res.ResourcesCompat
import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import android.widget.TextView

import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R

object DataBindingUtils {

    fun loadImage(view: SimpleDraweeView?, imageName: String?) {
        if (view != null && imageName != null && view.visibility == View.VISIBLE) {
            view.setImageURI("https://habitica-assets.s3.amazonaws.com/mobileApp/images/$imageName.png")
        }
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
