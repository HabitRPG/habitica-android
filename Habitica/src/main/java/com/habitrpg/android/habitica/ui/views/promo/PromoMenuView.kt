package com.habitrpg.android.habitica.ui.views.promo

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.PromoMenuBinding
import com.habitrpg.common.habitica.extensions.getThemeColor
import com.habitrpg.common.habitica.extensions.layoutInflater

class PromoMenuView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {
    var canClose: Boolean = false
        set(value) {
            field = value
            binding.closeButton.visibility = if (value) View.VISIBLE else View.GONE
        }
    var binding: PromoMenuBinding = PromoMenuBinding.inflate(context.layoutInflater, this)

    init {
        setBackgroundColor(context.getThemeColor(R.attr.colorWindowBackground))
        clipToPadding = false
        clipChildren = false
        clipToOutline = false
    }

    fun setTitleText(title: String?) {
        setText(binding.titleTextView, title)
    }

    fun setSubtitleText(subtitle: String?) {
        setText(binding.descriptionView, subtitle)
    }

    fun setTitleImage(title: Drawable?) {
        setImage(binding.titleImageView, title)
    }

    fun setSubtitleImage(subtitle: Drawable?) {
        setImage(binding.descriptionImageView, subtitle)
    }

    fun setDecoration(
        leftDrawable: Drawable?,
        rightDrawable: Drawable?
    ) {
        binding.leftImageView.setImageDrawable(leftDrawable)
        binding.rightImageView.setImageDrawable(rightDrawable)
    }

    private fun setImage(
        view: ImageView,
        drawable: Drawable?
    ) {
        if (drawable != null) {
            view.setImageDrawable(drawable)
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }

    private fun setText(
        view: TextView,
        text: String?
    ) {
        if (text != null) {
            view.text = text
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
    }
}
