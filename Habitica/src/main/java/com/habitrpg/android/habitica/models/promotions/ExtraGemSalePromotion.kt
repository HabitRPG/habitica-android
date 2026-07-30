package com.habitrpg.android.habitica.models.promotions

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentGemPurchaseBinding
import com.habitrpg.android.habitica.databinding.FragmentSubscriptionBinding
import com.habitrpg.android.habitica.databinding.PurchaseGemViewBinding
import com.habitrpg.android.habitica.extensions.DateUtils
import com.habitrpg.android.habitica.ui.activities.BaseActivity
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.PromoInfoFragment
import com.habitrpg.android.habitica.ui.helpers.ToolbarColorHelper
import com.habitrpg.android.habitica.ui.views.promo.PromoMenuView
import com.habitrpg.common.habitica.helpers.MainNavigationController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

abstract class ExtraGemSalePromotion(
    startDate: Date?,
    endDate: Date?,
) : HabiticaPromotion() {
    override val promoType = PromoType.GEMS_AMOUNT
    override val startDate = startDate ?: DateUtils.createDate(2020, 8, 22)
    override val endDate = endDate ?: DateUtils.createDate(2020, 8, 30)

    abstract val titleRes: Int

    open val amountTextColor = "#FEDEAD".toColorInt()

    override fun screenBackgroundColor(context: Context): Int = ContextCompat.getColor(context, R.color.gray_1)

    override fun backgroundColor(context: Context): Int = ContextCompat.getColor(context, R.color.gray_10)

    override fun promoBackgroundDrawable(context: Context): Drawable =
        ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_gray_10)
            ?: ShapeDrawable()

    open val promoMenuLeftRes = R.drawable.gem_promo_banner_left
    open val promoMenuRightRes = R.drawable.gem_promo_banner_right

    override fun configurePromoMenuView(view: PromoMenuView) {
        val context = view.context
        view.setBackgroundColor(backgroundColor(context))
        view.setTitleImage(ContextCompat.getDrawable(context, titleRes))
        view.setTitleText(null)
        view.setSubtitleText(context.getString(R.string.gem_promo_menu_description))

        view.setDecoration(
            ContextCompat.getDrawable(context, promoMenuLeftRes),
            ContextCompat.getDrawable(context, promoMenuRightRes),
        )

        view.binding.button.backgroundTintList =
            ContextCompat.getColorStateList(context, R.color.gray_1)
        view.binding.button.setText(R.string.view_offer)
        view.binding.button.setTextColor(ContextCompat.getColor(context, R.color.white))
        view.binding.button.setOnClickListener {
            menuOnNavigation(context)
        }
    }

    override fun menuOnNavigation(context: Context) {
        MainNavigationController.navigate(R.id.promoInfoFragment)
    }

    open val promoBannerLeftRes = R.drawable.gem_promo_banner_left
    open val promoBannerRightRes = R.drawable.gem_promo_banner_right

    override fun configurePurchaseBanner(binding: FragmentGemPurchaseBinding) {
        val context = binding.root.context
        binding.promoBanner.visibility = View.VISIBLE
        binding.promoBanner.background = promoBackgroundDrawable(context)
        binding.promoBannerLeftImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                promoBannerLeftRes,
            ),
        )
        binding.promoBannerRightImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                promoBannerRightRes,
            ),
        )
        binding.promoBannerTitleImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                titleRes,
            ),
        )
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        binding.promoBannerDurationView.text =
            context.getString(
                R.string.x_to_y,
                formatter.format(startDate),
                formatter.format(endDate),
            )
        binding.promoBannerDurationView.setTextColor("#FEE2B6".toColorInt())
    }

    override fun configurePurchaseBanner(binding: FragmentSubscriptionBinding) {
    }

    abstract val gems4SparkleRes: Int
    abstract val gems20SparkleRes: Int
    abstract val gems42SparkleRes: Int
    abstract val gems84SparkleRes: Int

    @SuppressLint("SetTextI18n")
    override fun configureGemView(
        binding: PurchaseGemViewBinding,
        regularAmount: Int,
    ) {
        val context = binding.root.context
        binding.root.background = promoBackgroundDrawable(context)
        binding.purchaseButton.background = buttonDrawable(context)
        binding.purchaseButton.setTextColor(ContextCompat.getColor(context, R.color.black))
        binding.gemAmount.setTextColor(amountTextColor)
        binding.gemLabel.setTextColor(amountTextColor)
        binding.footerTextView.visibility = View.VISIBLE
        binding.footerTextView.text = context.getString(R.string.usually_x_gems, regularAmount)
        binding.gemImage.setBackgroundResource(R.drawable.circle_gray_50)
        when (regularAmount) {
            4 -> {
                binding.gemAmount.text = "5"
                binding.decoImage.setImageResource(gems4SparkleRes)
            }

            21 -> {
                binding.gemAmount.text = "30"
                binding.decoImage.setImageResource(gems20SparkleRes)
            }

            42 -> {
                binding.gemAmount.text = "60"
                binding.decoImage.setImageResource(gems42SparkleRes)
            }

            84 -> {
                binding.gemAmount.text = "125"
                binding.decoImage.setImageResource(gems84SparkleRes)
            }

            else -> {
                regularAmount.toString()
            }
        }
    }

    open val promoInfoLeftRes = R.drawable.gem_promo_info_left
    open val promoInfoRightRes = R.drawable.gem_promo_info_right

    override fun configureInfoFragment(fragment: PromoInfoFragment) {
        val context = fragment.context ?: return
        val binding = fragment.binding ?: return
        binding.root.setBackgroundColor(screenBackgroundColor(context))
        (fragment.activity as? BaseActivity)?.let { activity ->
            ToolbarColorHelper.colorizeToolbar(
                activity.findViewById(R.id.toolbar),
                activity,
                backgroundColor = screenBackgroundColor(context),
            )
            activity.findViewById<View>(R.id.appbar)?.setBackgroundColor(screenBackgroundColor(context))
        }
        binding.promoBanner.background = promoBackgroundDrawable(context)
        binding.promoBannerLeftImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                promoInfoLeftRes,
            ),
        )
        binding.promoBannerRightImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                promoInfoRightRes,
            ),
        )
        binding.promoBannerTitleImage.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                titleRes,
            ),
        )
        binding.promoBannerSubtitleView.setText(R.string.limited_event)
        binding.promoBannerDurationView.setTextColor("#FEDEAD".toColorInt())
        val formatter = SimpleDateFormat("MMM d", Locale.getDefault())
        binding.promoBannerDurationView.text =
            context.getString(
                R.string.x_to_y,
                formatter.format(startDate),
                formatter.format(endDate),
            )
        binding.promoBannerDurationView.setTextColor(
            ContextCompat.getColor(
                context,
                R.color.white,
            ),
        )
        binding.promptButton.background = buttonDrawable(context)
        binding.promptButton.setText(R.string.view_gem_bundles)
        binding.promptButton.setTextColor(ContextCompat.getColor(context, R.color.black))
        binding.promptButton.setOnClickListener { MainNavigationController.navigate(R.id.gemPurchaseActivity) }

        binding.instructionDescriptionView.text =
            context.getString(
                R.string.gem_promo_info_instructions,
                formatter.format(startDate),
                formatter.format(endDate),
            )
        val limitationsFormatter =
            SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.LONG, SimpleDateFormat.LONG)
        val utcTimeFormatter = SimpleDateFormat.getTimeInstance(SimpleDateFormat.LONG)
        utcTimeFormatter.timeZone = TimeZone.getTimeZone("UTC")
        binding.limitationsDescriptionView.text =
            context.getString(
                R.string.gems_promo_info_limitations_fixed,
                limitationsFormatter.format(startDate),
                utcTimeFormatter.format(startDate),
                limitationsFormatter.format(endDate),
                utcTimeFormatter.format(endDate),
            )
    }
}
