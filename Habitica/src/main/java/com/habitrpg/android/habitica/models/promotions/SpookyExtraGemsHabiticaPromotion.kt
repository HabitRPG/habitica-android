package com.habitrpg.android.habitica.models.promotions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
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
import com.habitrpg.android.habitica.ui.views.promo.PromoMenuView
import com.habitrpg.common.habitica.helpers.MainNavigationController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class SpookyExtraGemsHabiticaPromotion(startDate: Date?, endDate: Date?) : HabiticaPromotion() {
    override val identifier: String
        get() = "spooky_extra_gems"
    override val promoType: PromoType
        get() = PromoType.GEMS_AMOUNT
    override val startDate: Date = startDate ?: DateUtils.createDate(2020, 9, 29)
    override val endDate: Date = endDate ?: DateUtils.createDate(2020, 10, 2)

    override fun pillBackgroundDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, R.drawable.spooky_promo_pill_bg)
            ?: ShapeDrawable()
    }

    override fun backgroundColor(context: Context): Int {
        return ContextCompat.getColor(context, R.color.gray_10)
    }

    override fun promoBackgroundDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_gray_10)
            ?: ShapeDrawable()
    }

    override fun buttonDrawable(context: Context): Drawable {
        return ContextCompat.getDrawable(context, R.drawable.spooky_promo_button_bg)
            ?: ShapeDrawable()
    }

    override fun configurePromoMenuView(view: PromoMenuView) {
        val context = view.context
        view.setBackgroundColor(backgroundColor(context))
        view.setTitleImage(ContextCompat.getDrawable(context, R.drawable.spooky_promo_title))
        view.setTitleText(null)
        view.setSubtitleImage(
            ContextCompat.getDrawable(
                context,
                R.drawable.spooky_promo_menu_description
            )
        )
        view.setSubtitleText(null)

        view.setDecoration(
            ContextCompat.getDrawable(context, R.drawable.spooky_promo_menu_left),
            ContextCompat.getDrawable(context, R.drawable.spooky_promo_menu_right)
        )

        view.binding.button.backgroundTintList =
            ContextCompat.getColorStateList(context, R.color.gray_1)
        view.binding.button.setText(R.string.learn_more)
        view.binding.button.setTextColor(ContextCompat.getColor(context, R.color.white))
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
                R.drawable.spooky_promo_banner_left
            )
        )
        binding.promoBannerRightImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.spooky_promo_banner_right
            )
        )
        binding.promoBannerTitleImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.spooky_promo_title
            )
        )
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        binding.promoBannerDurationView.text =
            context.getString(
                R.string.x_to_y,
                formatter.format(startDate),
                formatter.format(endDate)
            )
        binding.promoBannerDurationView.setTextColor(ContextCompat.getColor(context, R.color.white))
    }

    override fun configurePurchaseBanner(binding: FragmentSubscriptionBinding) {
    }

    @SuppressLint("SetTextI18n")
    override fun configureGemView(
        binding: PurchaseGemViewBinding,
        regularAmount: Int
    ) {
        val context = binding.root.context
        binding.root.background = promoBackgroundDrawable(context)
        binding.purchaseButton.background = buttonDrawable(context)
        binding.purchaseButton.setTextColor(ContextCompat.getColor(context, R.color.white))
        binding.gemAmount.setTextColor(Color.parseColor("#FEE2B6"))
        binding.gemLabel.setTextColor(Color.parseColor("#FEE2B6"))
        binding.footerTextView.visibility = View.VISIBLE
        binding.footerTextView.text = context.getString(R.string.usually_x_gems, regularAmount)
        when (regularAmount) {
            4 -> {
                binding.gemAmount.text = "5"
                binding.gemImage.setImageResource(R.drawable.spooky_gems_4)
            }

            21 -> {
                binding.gemAmount.text = "30"
                binding.gemImage.setImageResource(R.drawable.spooky_gems_21)
            }

            42 -> {
                binding.gemAmount.text = "60"
                binding.gemImage.setImageResource(R.drawable.spooky_gems_42)
            }

            84 -> {
                binding.gemAmount.text = "125"
                binding.gemImage.setImageResource(R.drawable.spooky_gems_84)
            }

            else -> regularAmount.toString()
        }
    }

    override fun configureInfoFragment(fragment: PromoInfoFragment) {
        val context = fragment.context ?: return
        fragment.binding?.promoBanner?.background = promoBackgroundDrawable(context)
        fragment.binding?.promoBannerLeftImage?.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.spooky_promo_info_left
            )
        )
        fragment.binding?.promoBannerRightImage?.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.spooky_promo_info_right
            )
        )
        fragment.binding?.promoBannerTitleImage?.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                R.drawable.spooky_promo_title
            )
        )
        fragment.binding?.promoBannerSubtitleView?.setText(R.string.limited_event)
        fragment.binding?.promoBannerDurationView?.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.white
            )
        )
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        fragment.binding?.promoBannerDurationView?.text =
            context.getString(
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
        fragment.binding?.promptText?.setText(R.string.spooky_promo_info_prompt)
        fragment.binding?.promptText?.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.orange_50
            )
        )
        fragment.binding?.promptButton?.background = buttonDrawable(context)
        fragment.binding?.promptButton?.setText(R.string.view_gem_bundles)
        fragment.binding?.promptButton?.setTextColor(ContextCompat.getColor(context, R.color.white))
        fragment.binding?.promptButton?.setOnClickListener { MainNavigationController.navigate(R.id.gemPurchaseActivity) }

        fragment.binding?.instructionDescriptionView?.text =
            context.getString(
                R.string.spooky_promo_info_instructions,
                formatter.format(startDate),
                formatter.format(endDate)
            )
        val limitationsFormatter = SimpleDateFormat.getDateTimeInstance()
        val utcTimeFormatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.LONG)
        utcTimeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        fragment.binding?.limitationsDescriptionView?.text =
            context.getString(
                R.string.gems_promo_info_limitations_fixed,
                limitationsFormatter.format(startDate),
                utcTimeFormatter.format(startDate),
                limitationsFormatter.format(endDate),
                utcTimeFormatter.format(endDate)
            )
    }
}
