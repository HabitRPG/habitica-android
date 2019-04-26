package com.habitrpg.android.habitica.ui.views.login


import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.bindView
import java.util.*

class LoginBackgroundView(context: Context, attrs: AttributeSet?) : RelativeLayout(context, attrs) {
    private val random: Random = Random()

    private val leftCloudView: ImageView by bindView(R.id.left_cloud_view)
    private val rightCloudView: ImageView by bindView(R.id.right_cloud_view)

    private var starViews: MutableList<StarView>? = null
    private var viewWidth: Int = 0
    private val viewHeight: Int
    private var didLayoutStars = false
    private var params = FrameLayout.LayoutParams(0, 0)

    private val blinkDuration: Int
        get() = random.nextInt(30) * 800 + 4

    private val starParams: LayoutParams
        get() {
            val params = LayoutParams(STAR_SIZE, STAR_SIZE)
            params.leftMargin = random.nextInt(viewWidth)
            params.topMargin = random.nextInt(viewHeight)
            return params
        }

    init {
        val metrics = DisplayMetrics()
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager.defaultDisplay.getMetrics(metrics)
        viewHeight = (metrics.heightPixels * SIZE_FACTOR).toInt()
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        starViews = ArrayList()
        generateStars()
        animateClouds()
    }

    private fun animateClouds() {
        val leftAnimator = ObjectAnimator.ofFloat(leftCloudView, View.TRANSLATION_Y, 10.0f).setDuration(5000)
        leftAnimator.repeatCount = ValueAnimator.INFINITE
        leftAnimator.repeatMode = ValueAnimator.REVERSE
        leftAnimator.start()
        val rightAnimator = ObjectAnimator.ofFloat(rightCloudView, View.TRANSLATION_Y, -10.0f).setDuration(8000)
        rightAnimator.repeatCount = ValueAnimator.INFINITE
        rightAnimator.repeatMode = ValueAnimator.REVERSE
        rightAnimator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        this.viewWidth = MeasureSpec.getSize(widthMeasureSpec)
        this.setMeasuredDimension(viewWidth, viewHeight)
        params.width = viewWidth
        params.height = viewHeight
        this.layoutParams = params
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        updateStarLayoutParams()
    }

    private fun generateStars() {
        generateStars(1, 12, 25)
    }

    private fun generateStars(largeCount: Int, mediumCount: Int, smallCount: Int) {
        removeStarViews()
        for (x in 0 until largeCount) {
            generateStar(2)
        }
        for (x in 0 until mediumCount) {
            generateStar(1)
        }
        for (x in 0 until smallCount) {
            generateStar(0)
        }
        requestLayout()
    }

    private fun removeStarViews() {
        if (starViews?.size ?: 0 > 0) {
            starViews?.forEach { this.removeView(it) }
            starViews?.clear()
        }
    }

    private fun generateStar(size: Int) {
        val starView = StarView(context)
        starView.setStarSize(size)
        if (random.nextInt(10) > 2) {
            starView.setBlinkDurations(Arrays.asList(blinkDuration, blinkDuration, blinkDuration))
        }
        starViews?.add(starView)
        if (viewWidth > 0 && viewHeight > 0) {
            this.addView(starView, 0, starParams)
        } else {
            this.addView(starView, 0)
        }
    }

    private fun updateStarLayoutParams() {
        if (viewWidth <= 0 ||viewHeight <= 0 || didLayoutStars || starViews?.size == 0) {
            return
        }
        for (view in starViews ?: emptyList<StarView>()) {
            view.layoutParams = starParams
        }
        didLayoutStars = true
    }

    companion object {

        private const val SIZE_FACTOR = 1.5f
        private const val STAR_SIZE = 30
    }
}
