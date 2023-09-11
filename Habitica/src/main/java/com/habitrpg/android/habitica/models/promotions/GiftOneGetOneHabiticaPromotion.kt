package com.habitrpg.android.habitica.models.promotions

import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.view.View
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentGemPurchaseBinding
import com.habitrpg.android.habitica.databinding.FragmentSubscriptionBinding
import com.habitrpg.android.habitica.databinding.PurchaseGemViewBinding
import com.habitrpg.android.habitica.extensions.DateUtils
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.SubscriptionFragment
import com.habitrpg.android.habitica.ui.views.promo.PromoMenuView
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.helpers.MainNavigationController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class GiftOneGetOneHabiticaPromotion(startDate: Date?, endDate: Date?) : HabiticaPromotion() {
    override val identifier: String
        get() = "g1g1"
    override val promoType: PromoType
        get() = PromoType.SUBSCRIPTION
    override val startDate: Date = startDate ?: DateUtils.createDate(2020, 11, 17)
    override val endDate: Date = endDate ?: DateUtils.createDate(2021, 0, 7)

    override fun pillBackgroundDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, R.drawable.g1g1_promo_pill_bg) ?: ShapeDrawable()
    }

    override fun backgroundColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.gray_10)
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
        view.background = ContextCompat.getDrawable(context, R.drawable.g1g1_menu_background)
        view.setTitleText(context.getString(R.string.promo_g1g1_prompt))
        view.setSubtitleText(context.getString(R.string.promo_g1g1_description))

        view.setDecoration(
            ContextCompat.getDrawable(context, R.drawable.g1g1_promo_menu_left),
            ContextCompat.getDrawable(context, R.drawable.g1g1_promo_menu_right)
        )

        view.binding.button.backgroundTintList =
            ContextCompat.getColorStateList(context, R.color.content_background)
        view.binding.button.setText(R.string.learn_more)
        if (context.isUsingNightModeResources()) {
            view.binding.button.setTextColor(ContextCompat.getColor(context, R.color.teal_100))
        } else {
            view.binding.button.setTextColor(ContextCompat.getColor(context, R.color.teal_10))
        }
        view.binding.button.setOnClickListener {
            menuOnNavigation(context)
        }
    }

    override fun menuOnNavigation(context: Context) {
        MainNavigationController.navigate(R.id.promoInfoFragment)
    }

    override fun configurePurchaseBanner(binding: FragmentGemPurchaseBinding) {
        val context = binding.root.context
        binding.promoBanner.visibility = View.VISIBLE
        binding.promoBanner.background = promoBackgroundDrawable(context)
        binding.promoBannerLeftImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.g1g1_promo_left_small
            )
        )
        binding.promoBannerRightImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.g1g1_promo_right_small
            )
        )
        binding.promoBannerTitleImage.visibility = View.GONE
        binding.promoBannerDurationView.visibility = View.GONE
        binding.promoBannerTitleText.visibility = View.VISIBLE
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        binding.promoBannerTitleText.text =
            context.getString(R.string.gift_one_get_one_purchase_banner, formatter.format(endDate))
    }

    override fun configurePurchaseBanner(binding: FragmentSubscriptionBinding) {
        val context = binding.root.context
        binding.promoBanner.visibility = View.VISIBLE
        binding.promoBanner.background = promoBackgroundDrawable(context)
        binding.promoBannerLeftImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.g1g1_promo_left_small
            )
        )
        binding.promoBannerRightImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.g1g1_promo_right_small
            )
        )
        binding.promoBannerTitleImage.visibility = View.GONE
        binding.promoBannerDurationView.visibility = View.GONE
        binding.promoBannerTitleText.visibility = View.VISIBLE
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        binding.promoBannerTitleText.text =
            context.getString(R.string.gift_one_get_one_purchase_banner, formatter.format(endDate))
    }

    override fun configureGemView(binding: PurchaseGemViewBinding, regularAmount: Int) {
    }

    override fun configureInfoFragment(fragment: PromoInfoFragment) {
        val context = fragment.context ?: return
        fragment.binding?.promoBanner?.background = promoBackgroundDrawable(context)
        fragment.binding?.promoBannerLeftImage?.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.g1g1_promo_left
            )
        )
        fragment.binding?.promoBannerRightImage?.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.g1g1_promo_right
            )
        )
        fragment.binding?.promoBannerTitleImage?.visibility = View.GONE
        fragment.binding?.promoBannerTitleText?.visibility = View.VISIBLE
        fragment.binding?.promoBannerTitleText?.text = context.getString(R.string.gift_one_get_one)
        fragment.binding?.promoBannerSubtitleView?.setText(R.string.limited_event)
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        fragment.binding?.promoBannerDurationView?.text = context.getString(
            R.string.x_to_y,
            formatter.format(startDate),
            formatter.format(endDate)
        )
        fragment.binding?.promoBannerDurationView?.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.white
            )
        )
        fragment.binding?.promptText?.setText(R.string.g1g1_promo_info_prompt)
        fragment.binding?.promptText?.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.text_teal
            )
        )
        fragment.binding?.promptButton?.background =
            ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_brand_400)
        fragment.binding?.promptButton?.setText(R.string.gift_a_subscription)
        fragment.binding?.promptButton?.setTextColor(ContextCompat.getColor(context, R.color.white))
        fragment.binding?.promptButton?.setOnClickListener {
            fragment.context?.let { context ->
                SubscriptionFragment.showGiftSubscriptionDialog(
                    context,
                    true
                )
            }
        }

        fragment.binding?.instructionDescriptionView?.text =
            context.getString(R.string.g1g1_promo_info_instructions)
        val limitationsFormatter =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG)
        val utcTimeFormatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.LONG)
        utcTimeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        fragment.binding?.limitationsDescriptionView?.text = context.getString(
            R.string.g1g1_promo_info_limitations_fixed,
            limitationsFormatter.format(startDate),
            utcTimeFormatter.format(startDate),
            limitationsFormatter.format(endDate),
            utcTimeFormatter.format(startDate)
        )
    }
}
