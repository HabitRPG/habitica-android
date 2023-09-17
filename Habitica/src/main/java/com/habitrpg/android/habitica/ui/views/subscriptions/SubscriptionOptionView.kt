package com.habitrpg.android.habitica.ui.views.subscriptions

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.PurchaseSubscriptionViewBinding
import com.habitrpg.common.habitica.extensions.layoutInflater

class SubscriptionOptionView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val binding = PurchaseSubscriptionViewBinding.inflate(context.layoutInflater, this, true)

    var sku: String? = null

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SubscriptionOptionView,
            0,
            0
        )

        if (a.getBoolean(R.styleable.SubscriptionOptionView_isNonRecurring, false)) {
            binding.descriptionTextView.text = context.getString(R.string.subscription_duration_norenew, a.getText(R.styleable.SubscriptionOptionView_recurringText))
        } else {
            binding.descriptionTextView.text = context.getString(R.string.subscription_duration, a.getText(R.styleable.SubscriptionOptionView_recurringText))
        }

        binding.gemCapTextView.text = a.getText(R.styleable.SubscriptionOptionView_gemCapText)
        setFlagText(a.getText(R.styleable.SubscriptionOptionView_flagText))
        val hourGlassCount = a.getInteger(R.styleable.SubscriptionOptionView_hourGlassCount, 0)
        binding.hourglassTextView.visibility = View.VISIBLE
        if (hourGlassCount != 0) {
            binding.hourglassTextView.text = context.getString(R.string.subscription_hourglasses, hourGlassCount)
        } else {
            binding.hourglassTextView.text = context.getString(R.string.subscription_hourglasses_3month_timeframe)
        }
    }

    fun setOnPurchaseClickListener(listener: OnClickListener) {
        this.setOnClickListener(listener)
    }

    fun setPriceText(text: String) {
        binding.priceLabel.text = text
    }

    fun setFlagText(text: CharSequence?) {
        if (text?.length ?: 0 == 0) {
            binding.flagFlap.visibility = View.GONE
            binding.flagTextview.visibility = View.GONE
        } else {
            binding.flagFlap.visibility = View.VISIBLE
            binding.flagTextview.visibility = View.VISIBLE
            binding.flagTextview.text = text
        }
    }

    fun setIsSelected(purchased: Boolean) {
        if (purchased) {
            binding.wrapper.setBackgroundResource(R.drawable.subscription_box_bg_selected)
            binding.subscriptionSelectedView.setBackgroundResource(R.drawable.subscription_selected)
            binding.gemCapTextView.setBackgroundResource(R.drawable.pill_bg_purple_400)
            binding.gemCapTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.hourglassTextView.setBackgroundResource(R.drawable.pill_bg_purple_400)
            binding.hourglassTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.priceLabel.setTextColor(ContextCompat.getColor(context, R.color.text_brand))
            binding.descriptionTextView.setTextColor(ContextCompat.getColor(context, R.color.text_brand))
        } else {
            binding.wrapper.setBackgroundResource(R.drawable.subscription_type_box_bg)
            binding.subscriptionSelectedView.setBackgroundResource(R.drawable.subscription_unselected)
            binding.gemCapTextView.setBackgroundResource(R.drawable.pill_bg_gray)
            binding.gemCapTextView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            binding.hourglassTextView.setBackgroundResource(R.drawable.pill_bg_gray)
            binding.hourglassTextView.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
            binding.priceLabel.setTextColor(ContextCompat.getColor(context, R.color.text_ternary))
            binding.descriptionTextView.setTextColor(ContextCompat.getColor(context, R.color.text_ternary))
        }
        val horizontalPadding = resources.getDimension(R.dimen.pill_horizontal_padding).toInt()
        val verticalPadding = resources.getDimension(R.dimen.pill_vertical_padding).toInt()
        binding.gemCapTextView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        binding.hourglassTextView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }
}
