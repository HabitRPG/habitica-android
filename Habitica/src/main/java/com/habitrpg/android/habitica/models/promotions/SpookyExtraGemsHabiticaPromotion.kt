package com.habitrpg.android.habitica.models.promotions

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import java.util.Date

class SpookyExtraGemsHabiticaPromotion(
    startDate: Date?,
    endDate: Date?,
) : ExtraGemSalePromotion(startDate, endDate) {
    override val identifier: String
        get() = "spooky_extra_gems"
    override val titleRes: Int
        get() = R.drawable.spooky_promo_title

    override val gems4SparkleRes = R.drawable.spooky_4_gems_sparkle
    override val gems20SparkleRes = R.drawable.spooky_20_gems_sparkle
    override val gems42SparkleRes = R.drawable.spooky_42_gems_sparkle
    override val gems84SparkleRes = R.drawable.spooky_84_gems_sparkle

    override val promoMenuLeftRes = R.drawable.spooky_promo_menu_left
    override val promoMenuRightRes = R.drawable.spooky_promo_menu_right

    override val promoBannerLeftRes = R.drawable.spooky_promo_banner_left
    override val promoBannerRightRes = R.drawable.spooky_promo_banner_right

    override val promoInfoLeftRes = R.drawable.spooky_promo_info_left
    override val promoInfoRightRes = R.drawable.spooky_promo_info_right

    override fun pillBackgroundDrawable(context: Context): Drawable =
        ContextCompat.getDrawable(context, R.drawable.spooky_promo_pill_bg) ?: ShapeDrawable()

    override fun buttonDrawable(context: Context): Drawable =
        ContextCompat.getDrawable(context, R.drawable.spooky_promo_button_bg)
            ?: ShapeDrawable()

    override fun configureInfoFragment(fragment: PromoInfoFragment) {
        super.configureInfoFragment(fragment)
        fragment.binding?.promptText?.setText(R.string.spooky_promo_info_prompt)
        fragment.binding?.promoBannerSubtitleView?.setTextColor("#D5C8FF".toColorInt())
    }
}
