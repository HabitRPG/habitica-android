package com.habitrpg.android.habitica.ui


import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.Typewriter

class SpeechBubbleView(context: Context, attrs: AttributeSet) : FrameLayout(context, attrs), View.OnClickListener {

    private val namePlate: TextView by bindView(R.id.name_plate)
    private val textView: Typewriter by bindView(R.id.textView)
    private val npcImageView: ImageView by bindView(R.id.npc_image_view)
    private val confirmationButtons: ViewGroup by bindView(R.id.confirmation_buttons)
    private val continueIndicator: View by bindView(R.id.continue_indicator)
    private var showNextListener: ShowNextListener? = null

    init {
        inflate(R.layout.speechbubble)

        val attributes = context.theme.obtainStyledAttributes(
                attrs,
                R.styleable.SpeechBubbleView,
                0, 0)

        namePlate.text = attributes.getString(R.styleable.SpeechBubbleView_namePlate)
        textView.text = attributes.getString(R.styleable.SpeechBubbleView_text)

        val iconRes = attributes.getDrawable(R.styleable.SpeechBubbleView_npcDrawable)
        if (iconRes != null) {
            npcImageView.setImageDrawable(iconRes)
        }

        confirmationButtons.visibility = View.GONE

        this.setOnClickListener(this)
    }


    fun setConfirmationButtonVisibility(visibility: Int) {
        confirmationButtons.visibility = visibility
    }

    fun animateText(text: String) {
        this.textView.animateText(text)
    }

    fun setText(text: String) {
        this.textView.text = text
    }

    fun setHasMoreContent(hasMoreContent: Boolean) {
        continueIndicator.visibility = if (hasMoreContent) View.VISIBLE else View.GONE
    }

    fun setShowNextListener(listener: ShowNextListener) {
        showNextListener = listener
    }

    override fun onClick(v: View) {
        if (textView.isAnimating) {
            textView.stopTextAnimation()
        } else {
            if (showNextListener != null) {
                showNextListener!!.showNextStep()
            }
        }
    }

    interface ShowNextListener {
        fun showNextStep()
    }
}
