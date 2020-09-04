package com.habitrpg.android.habitica.ui.views.promo

import android.content.Context
import android.util.AttributeSet
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import kotlinx.android.synthetic.main.promo_subscription_buy_gems.view.*

class SubscriptionBuyGemsPromoView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    init {
        inflate(R.layout.promo_subscription_buy_gems, true)
        setBackgroundColor(ContextCompat.getColor(context, R.color.window_background))
        clipToPadding = false
        clipChildren = false
        clipToOutline = false
        button.setOnClickListener { MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", true))) }
    }
}