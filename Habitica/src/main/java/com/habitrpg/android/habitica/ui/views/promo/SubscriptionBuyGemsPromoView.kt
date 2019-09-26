package com.habitrpg.android.habitica.ui.views.promo

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.dpToPx
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import kotlinx.android.synthetic.main.promo_subscription_buy_gems.view.*
import java.util.*

class SubscriptionBuyGemsPromoView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    private var didLayoutGold: Boolean = false
    private var goldViews = mutableListOf<AppCompatImageView>()
    private val random: Random = Random()

    private var iconSize = 30.dpToPx(context)

    private val starParams: LayoutParams
        get() {
            val params = LayoutParams(iconSize, iconSize)
            params.leftMargin = if (random.nextBoolean()) {
                -20 + random.nextInt(contentWrapper.left)
            } else {
                contentWrapper.right + random.nextInt(width - contentWrapper.right)
            }
            params.topMargin = -20 + random.nextInt(height + 20)
            return params
        }

    init {
        inflate(R.layout.promo_subscription_buy_gems, true)
        setBackgroundColor(ContextCompat.getColor(context, R.color.blue_50))
        clipToPadding = false
        clipChildren = false
        clipToOutline = false
        generateGold()

        button.setOnClickListener { MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", true))) }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        updateGoldLayoutParams()
    }

    private fun generateGold() {
        removeGoldViews()
        for (x in 0 until 8) {
            generateGoldView()
        }
        requestLayout()
    }

    private fun removeGoldViews() {
        if (goldViews.size > 0) {
            goldViews.forEach { this.removeView(it) }
            goldViews.clear()
        }
    }

    private fun generateGoldView() {
        val goldView = AppCompatImageView(context)
        goldView.scaleType = ImageView.ScaleType.CENTER
        goldView.setImageDrawable(BitmapDrawable(resources, HabiticaIconsHelper.imageOfGoldReward()))
        goldView.rotation = random.nextFloat() * 360
        goldViews.add(goldView)
        if (width > 0 && height > 0) {
            this.addView(goldView, 0, starParams)
        } else {
            this.addView(goldView, 0)
        }
    }

    private fun updateGoldLayoutParams() {
        if (width <= 0 || height <= 0 || didLayoutGold || goldViews.size == 0) {
            return
        }
        for (view in goldViews) {
            view.layoutParams = starParams
        }
        didLayoutGold = true
    }
}