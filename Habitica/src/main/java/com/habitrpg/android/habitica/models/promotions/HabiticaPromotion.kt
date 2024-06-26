package com.habitrpg.android.habitica.models.promotions

import android.content.Context
import android.graphics.drawable.Drawable
import com.habitrpg.android.habitica.databinding.FragmentGemPurchaseBinding
import com.habitrpg.android.habitica.databinding.FragmentSubscriptionBinding
import com.habitrpg.android.habitica.databinding.PurchaseGemViewBinding
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import com.habitrpg.android.habitica.ui.views.promo.PromoMenuView
import java.util.Date

enum class PromoType {
    GEMS_AMOUNT,
    GEMS_PRICE,
    SUBSCRIPTION,
    SURVEY
}

abstract class HabiticaPromotion {
    val isActive: Boolean
        get() {
            val now = Date()
            return startDate.before(now) && endDate.after(now)
        }
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

    abstract fun configurePurchaseBanner(binding: FragmentSubscriptionBinding)

    abstract fun configureGemView(
        binding: PurchaseGemViewBinding,
        regularAmount: Int
    )

    abstract fun configureInfoFragment(fragment: PromoInfoFragment)
}

fun getHabiticaPromotionFromKey(
    key: String,
    startDate: Date?,
    endDate: Date?
): HabiticaPromotion? {
    return when (key) {
        "fall_extra_gems", "fall2020", "testFall2020" ->
            FallExtraGemsHabiticaPromotion(
                startDate,
                endDate
            )

        "spooky_extra_gems", "fall2020SecondPromo", "spooky2020" ->
            SpookyExtraGemsHabiticaPromotion(
                startDate,
                endDate
            )

        "g1g1" -> GiftOneGetOneHabiticaPromotion(startDate, endDate)
        "survey2021" -> Survey2021Promotion()
        else -> null
    }
}

interface HabiticaWebPromotion {
    var url: String?
}
