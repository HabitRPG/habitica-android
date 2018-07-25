package com.habitrpg.android.habitica.ui

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.ui.helpers.bindView

class TutorialView(context: Context, var step: TutorialStep, var onReaction: OnTutorialReaction?) : FrameLayout(context) {
    private val speechBubbleView: SpeechBubbleView by bindView(R.id.speech_bubble)
    private val background: RelativeLayout by bindView(R.id.background)
    private val dismissButton: Button by bindView(R.id.dismissButton)
    private val completeButton: Button by bindView(R.id.completeButton)

    private var tutorialTexts: List<String> = emptyList()
    private var currentTextIndex: Int = 0

    private val isDisplayingLastStep: Boolean
        get() = currentTextIndex == tutorialTexts.size - 1

    init {
        inflate(R.layout.overlay_tutorial)
        speechBubbleView.setConfirmationButtonVisibility(View.GONE)
        speechBubbleView.setShowNextListener(object : SpeechBubbleView.ShowNextListener {
            override fun showNextStep() {
                displayNextTutorialText()
            }
        })

        completeButton.setOnClickListener { completeButtonClicked() }
        dismissButton.setOnClickListener { dismissButtonClicked() }
        background.setOnClickListener { backgroundClicked() }
    }

    fun setTutorialText(text: String) {
        this.speechBubbleView.animateText(text)
        speechBubbleView.setConfirmationButtonVisibility(View.VISIBLE)
    }

    fun setTutorialTexts(texts: List<String>) {
        tutorialTexts = texts
        currentTextIndex = -1
        displayNextTutorialText()
    }

    fun setCanBeDeferred(canBeDeferred: Boolean) {
        dismissButton.visibility = if (canBeDeferred) View.VISIBLE else View.GONE
    }

    private fun displayNextTutorialText() {
        currentTextIndex++
        if (currentTextIndex < tutorialTexts.size) {
            speechBubbleView.animateText(tutorialTexts[currentTextIndex])
            if (isDisplayingLastStep) {
                speechBubbleView.setConfirmationButtonVisibility(View.VISIBLE)
                speechBubbleView.setHasMoreContent(false)
            } else {
                speechBubbleView.setHasMoreContent(true)
            }
        } else {
            this.onReaction?.onTutorialCompleted(this.step)
        }
    }

    private fun completeButtonClicked() {
        this.onReaction?.onTutorialCompleted(this.step)
    }

    private fun dismissButtonClicked() {
        this.onReaction?.onTutorialDeferred(this.step)
    }

    private fun backgroundClicked() {
        speechBubbleView.onClick(speechBubbleView)
    }

    interface OnTutorialReaction {
        fun onTutorialCompleted(step: TutorialStep)

        fun onTutorialDeferred(step: TutorialStep)
    }
}
