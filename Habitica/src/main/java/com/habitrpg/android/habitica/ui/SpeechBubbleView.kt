package com.habitrpg.android.habitica.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.SpeechbubbleBinding
import com.habitrpg.common.habitica.extensions.layoutInflater

class SpeechBubbleView(context: Context, attrs: AttributeSet) :
    FrameLayout(context, attrs),
    View.OnClickListener {
    internal var binding = SpeechbubbleBinding.inflate(context.layoutInflater, this, true)
    private var showNextListener: ShowNextListener? = null

    init {
        val attributes =
            context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SpeechBubbleView,
                0,
                0
            )

        binding.namePlate.text = attributes.getString(R.styleable.SpeechBubbleView_namePlate)
        binding.textView.text = attributes.getString(R.styleable.SpeechBubbleView_text)

        val iconRes = attributes.getDrawable(R.styleable.SpeechBubbleView_npcDrawable)
        if (iconRes != null) {
            binding.npcImageView.setImageDrawable(iconRes)
        }

        binding.confirmationButtons.visibility = View.GONE

        this.setOnClickListener(this)
    }

    fun setConfirmationButtonVisibility(visibility: Int) {
        binding.confirmationButtons.visibility = visibility
    }

    fun animateText(text: String) {
        binding.textView.animateText(text)
    }

    fun setText(text: String) {
        binding.textView.text = text
    }

    fun setHasMoreContent(hasMoreContent: Boolean) {
        binding.continueIndicator.visibility = if (hasMoreContent) View.VISIBLE else View.GONE
    }

    fun setShowNextListener(listener: ShowNextListener) {
        showNextListener = listener
    }

    override fun onClick(v: View) {
        if (binding.textView.isAnimating) {
            binding.textView.stopTextAnimation()
        } else {
            showNextListener?.showNextStep()
        }
    }

    interface ShowNextListener {
        fun showNextStep()
    }
}
