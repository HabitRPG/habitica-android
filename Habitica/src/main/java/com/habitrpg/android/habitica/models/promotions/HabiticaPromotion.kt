package com.habitrpg.android.habitica.models.promotions

import android.content.Context
import android.graphics.drawable.Drawable
import com.habitrpg.android.habitica.databinding.FragmentGemPurchaseBinding
import com.habitrpg.android.habitica.databinding.PurchaseGemViewBinding
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import com.habitrpg.android.habitica.ui.views.promo.PromoMenuView
import java.util.*

enum class PromoType {
    GEMS_AMOUNT,
    GEMS_PRICE,
    SUBSCRIPTION
}

abstract class HabiticaPromotion {
    abstract val identifier: String
    abstract val promoType: PromoType

    abstract val startDate: Date
    abstract val endDate: Date

    abstract fun pillBackgroundDrawable(context: Context): Drawable
    abstract fun backgroundColor(context: Context): Int
    abstract fun promoBackgroundDrawable(context: Context): Drawable

    abstract fun buttonDrawable(context: Context): Drawable

    abstract fun configurePromoMenuView(view: PromoMenuView)
    abstract fun menuOnNavigation(context: Context)

    abstract fun configurePurchaseBanner(binding: FragmentGemPurchaseBinding)

    abstract fun configureGemView(binding: PurchaseGemViewBinding, regularAmount: Int)
    abstract fun configureInfoFragment(fragment: PromoInfoFragment)
}

fun getHabiticaPromotionFromKey(key: String): HabiticaPromotion? {
    return when (key) {
        "fall_extra_gems" -> FallExtraGemsHabiticaPromotion()
        "spooky_extra_gems" -> SpookyExtraGemsHabiticaPromotion()
        else -> null
    }
}

