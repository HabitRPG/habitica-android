package com.habitrpg.android.habitica.ui.views.subscriptions

import android.content.Context
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.extensions.inflate


class SubscriptionOptionView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val priceTextView: TextView by bindView(R.id.priceLabel)
    internal val descriptionTextView: TextView by bindView(R.id.descriptionTextView)
    internal val subscriptionSelectedView: View by bindView(R.id.subscriptionSelectedView)
    internal val subscriptionSelectedFrameView: View by bindView(R.id.subscriptionSelectedFrameView)
    internal val gemCapTextView: TextView by bindView(R.id.gemCapTextView)
    private val hourGlassTextView: TextView by bindView(R.id.hourglassTextView)

    var sku: String? = null

    init {
        inflate(R.layout.purchase_subscription_view, true)

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SubscriptionOptionView,
                0, 0)

        if (a.getBoolean(R.styleable.SubscriptionOptionView_isNonRecurring, false)) {
            descriptionTextView.text = context.getString(R.string.subscription_duration_norenew, a.getText(R.styleable.SubscriptionOptionView_recurringText))
        } else {
            descriptionTextView.text = context.getString(R.string.subscription_duration, a.getText(R.styleable.SubscriptionOptionView_recurringText))
        }

        gemCapTextView.text = a.getText(R.styleable.SubscriptionOptionView_gemCapText)
        val hourGlassCount = a.getInteger(R.styleable.SubscriptionOptionView_hourGlassCount, 0)
        if (hourGlassCount != 0) {
            hourGlassTextView.text = context.getString(R.string.subscription_hourglasses, hourGlassCount)
            hourGlassTextView.visibility = View.VISIBLE
        } else {
            hourGlassTextView.visibility = View.GONE
        }

    }

    fun setOnPurchaseClickListener(listener: OnClickListener) {
        this.setOnClickListener(listener)
    }

    fun setPriceText(text: String) {
        this.priceTextView.text = text
    }

    fun setIsPurchased(purchased: Boolean) {
        if (purchased) {
            subscriptionSelectedView.setBackgroundResource(R.drawable.subscription_selected)
            subscriptionSelectedFrameView.setBackgroundResource(R.color.brand_300)
            gemCapTextView.setBackgroundResource(R.drawable.pill_bg_green)
            gemCapTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
            hourGlassTextView.setBackgroundResource(R.drawable.pill_bg_green)
            hourGlassTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
        } else {
            subscriptionSelectedView.setBackgroundResource(R.drawable.subscription_unselected)
            subscriptionSelectedFrameView.setBackgroundResource(R.color.brand_700)
            gemCapTextView.setBackgroundResource(R.drawable.pill_bg)
            gemCapTextView.setTextColor(ContextCompat.getColor(context, R.color.gray_50))
            hourGlassTextView.setBackgroundResource(R.drawable.pill_bg)
            hourGlassTextView.setTextColor(ContextCompat.getColor(context, R.color.gray_50))
        }
        val horizontalPadding = resources.getDimension(R.dimen.pill_horizontal_padding).toInt()
        val verticalPadding = resources.getDimension(R.dimen.pill_vertical_padding).toInt()
        gemCapTextView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
        hourGlassTextView.setPadding(horizontalPadding, verticalPadding, horizontalPadding, verticalPadding)
    }
}
