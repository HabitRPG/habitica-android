package com.habitrpg.android.habitica.models.promotions

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentGemPurchaseBinding
import com.habitrpg.android.habitica.databinding.FragmentSubscriptionBinding
import com.habitrpg.android.habitica.databinding.PurchaseGemViewBinding
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import com.habitrpg.android.habitica.ui.fragments.PromoWebFragmentArgs
import com.habitrpg.android.habitica.ui.views.promo.PromoMenuView
import java.util.Date

class Survey2021Promotion : HabiticaPromotion(), HabiticaWebPromotion {
    override val identifier: String
        get() = "survey2021"
    override val promoType: PromoType
        get() = PromoType.SURVEY
    override val startDate: Date = Date()
    override val endDate: Date = Date()

    override var url: String? = null

    override fun pillBackgroundDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, R.drawable.g1g1_promo_pill_bg) ?: ShapeDrawable()
    }

    override fun backgroundColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.blue_1)
    }

    override fun promoBackgroundDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, R.drawable.g1g1_promo_background)
            ?: ShapeDrawable()
    }

    override fun buttonDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_content)
            ?: ShapeDrawable()
    }

    override fun configurePromoMenuView(view: PromoMenuView) {
        val context = view.context
        view.canClose = true
        view.binding.closeButton.setColorFilter(ContextCompat.getColor(context, R.color.blue_100))
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_1))
        view.setTitleText(context.getString(R.string.survey_title))
        view.setSubtitleText(context.getString(R.string.survey_menu_description))

        view.setDecoration(
            ContextCompat.getDrawable(context, R.drawable.survey_art_left),
            ContextCompat.getDrawable(context, R.drawable.survey_art_right)
        )

        view.binding.button.backgroundTintList = ContextCompat.getColorStateList(context, R.color.white)
        view.binding.button.setText(R.string.go_to_survey)
        view.binding.button.setTextColor(ContextCompat.getColor(context, R.color.blue_10))
        view.binding.button.setOnClickListener {
            menuOnNavigation(context)
        }
    }

    override fun menuOnNavigation(context: Context) {
        MainNavigationController.navigate(R.id.promoWebFragment, PromoWebFragmentArgs.Builder(url ?: "").build().toBundle())
    }

    override fun configurePurchaseBanner(binding: FragmentGemPurchaseBinding) {
    }

    override fun configurePurchaseBanner(binding: FragmentSubscriptionBinding) {
    }

    override fun configureGemView(binding: PurchaseGemViewBinding, regularAmount: Int) {
    }

    override fun configureInfoFragment(fragment: PromoInfoFragment) {
    }
}
