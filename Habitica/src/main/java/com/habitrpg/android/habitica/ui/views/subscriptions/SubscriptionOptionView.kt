package com.habitrpg.android.habitica.ui.views.subscriptions

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.PurchaseSubscriptionViewBinding
import com.habitrpg.common.habitica.extensions.layoutInflater


class SubscriptionOptionView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs) {
    private val binding = PurchaseSubscriptionViewBinding.inflate(context.layoutInflater, this, true)

    var sku: String? = null
    private var isPromoted: Boolean = false
    private var gemCap: Int = 24

    init {
        val a = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SubscriptionOptionView,
            0,
            0,
        )

        if (a.getBoolean(R.styleable.SubscriptionOptionView_isNonRecurring, false)) {
            binding.descriptionTextView.text = context.getString(
                R.string.subscription_duration_norenew,
                a.getText(R.styleable.SubscriptionOptionView_recurringText),
            )
        } else {
            binding.descriptionTextView.text = context.getString(
                R.string.subscription_duration,
                a.getText(R.styleable.SubscriptionOptionView_recurringText),
            )
        }

        gemCap = a.getInteger(R.styleable.SubscriptionOptionView_gemCapText, 24)
        setGemCapText(false)
        setAddtlGemText(false)
        setFlagText(a.getText(R.styleable.SubscriptionOptionView_flagText))
    }

    private fun setGemCapText(isSelected: Boolean) {
        binding.gemCapTextView.text = highlightText(context.getString(R.string.unlocks_x_gems_per_month, gemCap),
            context.getString(R.string.x_gems, gemCap),
            ContextCompat.getColor(context, if (isSelected) R.color.yellow_5 else R.color.white))
    }

    private fun setAddtlGemText(isSelected: Boolean) {
        if (gemCap == 50) {
            binding.hourglassTextView.text = highlightText(
                context.getString(R.string.max_gem_cap),
                context.getString(R.string.gem_cap),
                ContextCompat.getColor(context, if (isSelected) R.color.yellow_5 else R.color.white)
            )
        } else {
            binding.hourglassTextView.text = highlightText(
                context.getString(R.string.two_gems_per_month),
                context.getString(R.string.plus_two_gems),
                ContextCompat.getColor(context, if (isSelected) R.color.yellow_5 else R.color.white)
            )
        }
    }

    fun setOnPurchaseClickListener(listener: OnClickListener) {
        this.setOnClickListener(listener)
    }

    fun showHourglassPromo(show: Boolean) {
        binding.hourglassPromoView.isVisible = show
        binding.hourglassPromoView.text = highlightText(
            context.getString(R.string.get_12_mystic_hourglasses),
            context.getString(R.string.twelve_mystic_hourglasses),
            ContextCompat.getColor(context, R.color.teal_1)
        )
    }

    fun setPriceText(text: String) {
        binding.priceLabel.text = text
        if (isPromoted) {
            val shader: Shader = LinearGradient(
                0f, 0f, binding.priceLabel.paint.measureText(text), binding.priceLabel.lineHeight.toFloat(), Color.parseColor("#2995CD"), Color.parseColor("#24CC8F"), Shader.TileMode.REPEAT
            )
            binding.priceLabel.paint.setShader(shader)
        }
    }

    fun setSalePrice(price: String) {
        binding.salePriceLabel.text = price
    }

    fun setFlagText(text: CharSequence?) {
        isPromoted = text?.isNotBlank() ?: false
        if (isPromoted) {
            binding.flagFlap.visibility = View.VISIBLE
            binding.flagTextview.visibility = View.VISIBLE
            binding.flagTextview.text = text
        } else {
            binding.flagFlap.visibility = View.GONE
            binding.flagTextview.visibility = View.GONE
        }
    }

    private fun highlightText(text: String, substring: String, color: Int): Spannable {
        val spannable = SpannableString(text)
        val start = text.indexOf(substring)
        val end = start + substring.length
        spannable.setSpan(ForegroundColorSpan(color), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        spannable.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        return spannable
    }

    fun setIsSelected(purchased: Boolean) {
        if (purchased) {
            binding.selectedIndicator.visibility = View.VISIBLE
            binding.root.setBackgroundResource(R.drawable.subscription_box_bg_selected)
            val textColor = if (isPromoted) {
                ContextCompat.getColor(context, R.color.teal_1)
            } else {
                ContextCompat.getColor(context, R.color.brand_300)
            }
            binding.gemCapTextView.setTextColor(textColor)
            setGemCapText(true)
            binding.hourglassTextView.setTextColor(textColor)
            setAddtlGemText(true)
            if (!isPromoted) {
                binding.priceLabel.setTextColor(textColor)
            }
            binding.descriptionTextView.setTextColor(textColor)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TextViewCompat.setCompoundDrawableTintList(binding.gemCapTextView, ContextCompat.getColorStateList(context, R.color.yellow_100))
                TextViewCompat.setCompoundDrawableTintList(binding.hourglassTextView, ContextCompat.getColorStateList(context, R.color.yellow_100))
            }
        } else {
            binding.selectedIndicator.visibility = View.GONE
            binding.root.setBackgroundResource(R.drawable.subscription_type_box_bg)
            binding.gemCapTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.brand_600,
                ),
            )
            setGemCapText(false)
            binding.hourglassTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.brand_600,
                ),
            )
            setAddtlGemText(false)
            if (!isPromoted) {
                binding.priceLabel.setTextColor(ContextCompat.getColor(context, if (isPromoted) R.color.promo_gradient else R.color.brand_600))
            }
            binding.descriptionTextView.setTextColor(
                ContextCompat.getColor(
                    context,
                    R.color.brand_600,
                ),
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                TextViewCompat.setCompoundDrawableTintList(binding.gemCapTextView, ContextCompat.getColorStateList(context, R.color.brand_400))
                TextViewCompat.setCompoundDrawableTintList(binding.hourglassTextView, ContextCompat.getColorStateList(context, R.color.brand_400))
            }
        }
    }
}
