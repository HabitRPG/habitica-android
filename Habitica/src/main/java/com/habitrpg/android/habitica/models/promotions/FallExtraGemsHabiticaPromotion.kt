package com.habitrpg.android.habitica.models.promotions

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import java.util.Date

class FallExtraGemsHabiticaPromotion(startDate: Date?, endDate: Date?) : ExtraGemSalePromotion(startDate, endDate) {
    override val identifier: String
        get() = "fall_extra_gems"
    override val titleRes: Int
        get() = R.drawable.fall_promo_title

    override val gems4SparkleRes = R.drawable.fall_4_gems_sparkle
    override val gems20SparkleRes = R.drawable.fall_20_gems_sparkle
    override val gems42SparkleRes = R.drawable.fall_42_gems_sparkle
    override val gems84SparkleRes = R.drawable.fall_84_gems_sparkle

    override fun pillBackgroundDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, R.drawable.fall_promo_pill_bg) ?: ShapeDrawable()
    }

    override fun buttonDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, R.drawable.fall_promo_button_bg)
            ?: ShapeDrawable()
    }

    override fun configureInfoFragment(fragment: PromoInfoFragment) {
        super.configureInfoFragment(fragment)
        fragment.binding?.promptText?.setText(R.string.fall_promo_info_prompt)
        fragment.binding?.promptText?.setTextColor("#F78E2F".toColorInt())
    }
}
