package com.habitrpg.android.habitica.ui

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.habitrpg.android.habitica.databinding.OverlayTutorialBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.TutorialStep

class TutorialView(context: Context, var step: TutorialStep, var onReaction: OnTutorialReaction?) : FrameLayout(context) {
    private val binding = OverlayTutorialBinding.inflate(context.layoutInflater, this, true)
    private var tutorialTexts: List<String> = emptyList()
    private var currentTextIndex: Int = 0

    private val isDisplayingLastStep: Boolean
        get() = currentTextIndex == tutorialTexts.size - 1

    init {
        binding.speechBubbleView.setConfirmationButtonVisibility(View.GONE)
        binding.speechBubbleView.setShowNextListener(object : SpeechBubbleView.ShowNextListener {
            override fun showNextStep() {
                displayNextTutorialText()
            }
        })

        binding.speechBubbleView.binding.completeButton.setOnClickListener { completeButtonClicked() }
        binding.speechBubbleView.binding.dismissButton.setOnClickListener { dismissButtonClicked() }
        binding.background.setOnClickListener { backgroundClicked() }
    }

    fun setTutorialText(text: String) {
        binding.speechBubbleView.animateText(text)
        binding.speechBubbleView.setConfirmationButtonVisibility(View.VISIBLE)
    }

    fun setTutorialTexts(texts: List<String>) {
        tutorialTexts = texts
        currentTextIndex = -1
        displayNextTutorialText()
    }

    fun setCanBeDeferred(canBeDeferred: Boolean) {
        binding.speechBubbleView.binding.dismissButton.visibility = if (canBeDeferred) View.VISIBLE else View.GONE
    }

    private fun displayNextTutorialText() {
        currentTextIndex++
        if (currentTextIndex < tutorialTexts.size) {
            binding.speechBubbleView.animateText(tutorialTexts[currentTextIndex])
            if (isDisplayingLastStep) {
                binding.speechBubbleView.setConfirmationButtonVisibility(View.VISIBLE)
                binding.speechBubbleView.setHasMoreContent(false)
            } else {
                binding.speechBubbleView.setHasMoreContent(true)
            }
        } else {
            this.onReaction?.onTutorialCompleted(this.step)
        }
    }

    private fun completeButtonClicked() {
        this.onReaction?.onTutorialCompleted(this.step)
        (parent as? ViewGroup)?.removeView(this)
    }

    private fun dismissButtonClicked() {
        this.onReaction?.onTutorialDeferred(this.step)
        (parent as? ViewGroup)?.removeView(this)
    }

    private fun backgroundClicked() {
        binding.speechBubbleView.onClick(binding.speechBubbleView)
    }

    interface OnTutorialReaction {
        fun onTutorialCompleted(step: TutorialStep)

        fun onTutorialDeferred(step: TutorialStep)
    }
}
