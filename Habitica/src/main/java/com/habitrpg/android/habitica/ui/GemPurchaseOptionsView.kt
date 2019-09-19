package com.habitrpg.android.habitica.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.helpers.bindView


class GemPurchaseOptionsView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {

    private val gemImageView: ImageView by bindView(R.id.gem_image)
    private val gemAmountTextView: TextView by bindView(R.id.gem_amount)
    private val purchaseButton: Button by bindView(R.id.purchase_button)
    var sku: String? = null

    init {
        inflate(R.layout.purchase_gem_view, true)

        val a = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.GemPurchaseOptionsView,
                0, 0)

        gemAmountTextView.text = a.getText(R.styleable.GemPurchaseOptionsView_gemAmount)

        val iconRes = a.getDrawable(R.styleable.GemPurchaseOptionsView_gemDrawable)
        if (iconRes != null) {
            gemImageView.setImageDrawable(iconRes)
        }
    }

    fun setOnPurchaseClickListener(listener: OnClickListener) {
        purchaseButton.setOnClickListener(listener)
    }

    fun setPurchaseButtonText(price: String) {
        purchaseButton.text = price
    }
}
