package com.habitrpg.android.habitica.ui

import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.OverlayTutorialBinding
import com.habitrpg.android.habitica.extensions.consumeWindowInsetsAbove30
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.common.habitica.extensions.dpToPx
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.helpers.MainNavigationController

class TutorialView(
    context: Context,
    val step: TutorialStep,
    private val onReaction: OnTutorialReaction
) : FrameLayout(context) {
    private val binding = OverlayTutorialBinding.inflate(context.layoutInflater, this, true)
    private var tutorialTexts: List<String> = emptyList()
    private var currentTextIndex: Int = 0

    private val isDisplayingLastStep: Boolean
        get() = currentTextIndex == tutorialTexts.size - 1

    init {
        binding.speechBubbleView.setConfirmationButtonVisibility(View.GONE)
        binding.speechBubbleView.setShowNextListener(
            object : SpeechBubbleView.ShowNextListener {
                override fun showNextStep() {
                    displayNextTutorialText()
                }
            }
        )

        binding.speechBubbleView.binding.completeButton.setOnClickListener { completeButtonClicked() }
        binding.speechBubbleView.binding.dismissButton.setOnClickListener { dismissButtonClicked() }
        binding.background.setOnClickListener { backgroundClicked() }

        if (step.linkFAQ) {
            binding.speechBubbleView.binding.dismissButton.setText(R.string.visit_faq)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
                        or WindowInsetsCompat.Type.displayCutout()
            )
            v.updatePadding(
                bottom = bars.bottom + 16.dpToPx(context)
            )
            consumeWindowInsetsAbove30(insets)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insets = rootWindowInsets.getInsets(WindowInsets.Type.systemBars())
            binding.speechBubbleContainer.updatePadding(
                bottom = insets.bottom + 16.dpToPx(context),
            )
        }
    }

    fun setTutorialText(text: String) {
        binding.speechBubbleView.animateText(text)
        binding.speechBubbleView.setConfirmationButtonVisibility(View.VISIBLE)
    }

    fun setTutorialTexts(texts: List<String>) {
        if (texts.size == 1) {
            setTutorialText(texts.first())
            return
        }
        tutorialTexts = texts
        currentTextIndex = -1
        displayNextTutorialText()
    }

    fun setCanBeDeferred(canBeDeferred: Boolean) {
        binding.speechBubbleView.binding.dismissButton.visibility =
            if (canBeDeferred) View.VISIBLE else View.GONE
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
            onReaction.onTutorialCompleted(step)
        }
    }

    private fun completeButtonClicked() {
        onReaction.onTutorialCompleted(step)
        post {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    private fun dismissButtonClicked() {
        post {
            (parent as? ViewGroup)?.removeView(this)
        }
        if (step.linkFAQ) {
            onReaction.onTutorialCompleted(step)
            MainNavigationController.navigate(R.id.FAQOverviewFragment)
        } else {
            onReaction.onTutorialDeferred(step)
        }
    }

    private fun backgroundClicked() {
        binding.speechBubbleView.onClick(binding.speechBubbleView)
    }

    interface OnTutorialReaction {
        fun onTutorialCompleted(step: TutorialStep)

        fun onTutorialDeferred(step: TutorialStep)
    }
}
