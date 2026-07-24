package com.habitrpg.android.habitica.models.promotions

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.Orientation
import androidx.core.graphics.toColorInt
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import java.util.Date

class FlashExtraGemsHabiticaPromotion(startDate: Date?, endDate: Date?) : ExtraGemSalePromotion(startDate, endDate) {
    override val identifier: String
        get() = "flash_extra_gems"
    override val titleRes: Int
        get() = R.drawable.flash_promo_title

    override val gems4SparkleRes = R.drawable.flash_4_gems_sparkle
    override val gems20SparkleRes = R.drawable.flash_20_gems_sparkle
    override val gems42SparkleRes = R.drawable.flash_42_gems_sparkle
    override val gems84SparkleRes = R.drawable.flash_84_gems_sparkle

    override fun pillBackgroundDrawable(context: Context): Drawable {
        return GradientDrawable(Orientation.TL_BR,
            intArrayOf(
                "#FF6165".toColorInt(),
                "#FF944C".toColorInt(),
                "#FFBE5D".toColorInt(),
                "#24CC8F".toColorInt(),
                "#50B5E9".toColorInt()
            )).apply {
                cornerRadius = 10f

            }
    }

    override fun buttonDrawable(context: Context): Drawable {
        return GradientDrawable(Orientation.TL_BR,
            intArrayOf(
                "#FF6165".toColorInt(),
                "#FF944C".toColorInt(),
                "#FFBE5D".toColorInt(),
                "#24CC8F".toColorInt(),
                "#50B5E9".toColorInt()
            )).apply { cornerRadius = 12f }
    }

    override fun configureInfoFragment(fragment: PromoInfoFragment) {
        super.configureInfoFragment(fragment)
        fragment.binding?.promptText?.setText(R.string.fall_promo_info_prompt)
        fragment.binding?.promptText?.setTextColor("#F78E2F".toColorInt())
    }
}
