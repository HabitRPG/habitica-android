package com.habitrpg.android.habitica.ui.views.promo

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.RelativeLayout
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.common.habitica.extensions.inflate
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.helpers.MainNavigationController

class SubscriptionBuyGemsPromoView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : RelativeLayout(context, attrs, defStyleAttr) {
        init {
            inflate(R.layout.promo_subscription_buy_gems, true)
            setBackgroundColor(context.getThemeColor(R.attr.colorWindowBackground))
            clipToPadding = false
            clipChildren = false
            clipToOutline = false
            findViewById<Button>(R.id.button).setOnClickListener {
                MainNavigationController.navigate(
                    R.id.gemPurchaseActivity,
                    bundleOf(Pair("openSubscription", true)),
                )
            }
        }
    }
