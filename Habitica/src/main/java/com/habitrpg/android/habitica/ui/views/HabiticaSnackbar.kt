package com.habitrpg.android.habitica.ui.views

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.SnackbarViewBinding
import com.habitrpg.common.habitica.helpers.Animations
import com.plattysoft.leonids.ParticleSystem

class HabiticaSnackbar
/**
 * Constructor for the transient bottom bar.
 *
 * @param parent The parent for this transient bottom bar.
 * @param content The content view for this transient bottom bar.
 * @param callback The content view callback for this transient bottom bar.
 */
private constructor(parent: ViewGroup, content: View, callback: ContentViewCallback) :
    BaseTransientBottomBar<HabiticaSnackbar>(parent, content, callback) {
    val binding: SnackbarViewBinding = SnackbarViewBinding.bind(content)

    fun setTitle(title: CharSequence?): HabiticaSnackbar {
        binding.snackbarTitle.text = title
        binding.snackbarTitle.visibility = if (title != null) View.VISIBLE else View.GONE
        return this
    }

    fun setText(text: CharSequence?): HabiticaSnackbar {
        binding.snackbarText.text = text
        binding.snackbarText.visibility = if (text != null) View.VISIBLE else View.GONE
        return this
    }

    fun setTitleColor(color: Int): HabiticaSnackbar {
        binding.snackbarTitle.setTextColor(color)
        return this
    }

    fun setTextColor(color: Int): HabiticaSnackbar {
        binding.snackbarText.setTextColor(color)
        return this
    }

    fun setRightDiff(
        icon: Drawable?,
        textColor: Int,
        text: String?
    ): HabiticaSnackbar {
        if (icon == null) {
            return this
        }
        binding.rightView.visibility = View.VISIBLE
        binding.rightIconView.setImageDrawable(icon)
        binding.rightTextView.setTextColor(textColor)
        binding.rightTextView.text = text
        return this
    }

    fun setLeftIcon(image: Drawable?): HabiticaSnackbar {
        binding.leftImageView.setImageDrawable(image)
        binding.leftImageView.visibility = if (image != null) View.VISIBLE else View.GONE
        return this
    }

    fun setBackgroundColor(
        @ColorInt color: Int
    ): HabiticaSnackbar {
        view.setBackgroundColor(color)
        return this
    }

    fun setBackgroundResource(resourceId: Int): HabiticaSnackbar {
        binding.snackbarView.setBackgroundResource(resourceId)
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        return this
    }

    private fun setSpecialView(specialView: View?): HabiticaSnackbar {
        if (specialView != null) {
            binding.contentContainer.addView(specialView)
        }
        return this
    }

    private class ContentViewCallback(private val content: View) :
        com.google.android.material.snackbar.ContentViewCallback {
        @Suppress("SameParameterValue")
        override fun animateContentIn(
            delay: Int,
            duration: Int
        ) {
            content.scaleY = 0f
            content.scaleX = 0f
            content.animate().scaleY(1f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
            content.animate().scaleX(1f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
            content.animate().alpha(1f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
        }

        override fun animateContentOut(
            delay: Int,
            duration: Int
        ) {
            content.scaleY = 1f
            content.scaleX = 1f
            content.animate().scaleY(0f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
            content.animate().scaleX(0f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
            content.animate().alpha(0f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
        }
    }

    enum class SnackbarDisplayType {
        NORMAL,
        FAILURE,
        FAILURE_BLUE,
        DROP,
        SUCCESS,
        BLUE,
        BLACK,
        SUBSCRIBER_BENEFIT
    }

    companion object {
        const val MIN_LEVEL_FOR_SKILLS = 11

        private fun make(
            parent: ViewGroup,
            duration: Int
        ): HabiticaSnackbar {
            val inflater = LayoutInflater.from(parent.context)
            val content = inflater.inflate(R.layout.snackbar_view, parent, false)
            val viewCallback = ContentViewCallback(content)
            val customSnackbar = HabiticaSnackbar(parent, content, viewCallback)
            customSnackbar.duration = duration
            return customSnackbar
        }

        fun showSnackbar(
            container: ViewGroup,
            content: CharSequence?,
            displayType: SnackbarDisplayType,
            isCelebratory: Boolean = false,
            isSubscriberBenefit: Boolean = false,
            duration: Int = Snackbar.LENGTH_LONG
        ) {
            showSnackbar(
                container,
                null,
                null,
                content,
                null,
                null,
                0,
                null,
                displayType,
                isCelebratory,
                isSubscriberBenefit,
                duration
            )
        }

        fun showSnackbar(
            container: ViewGroup,
            leftImage: Drawable,
            title: CharSequence?,
            content: CharSequence?,
            displayType: SnackbarDisplayType,
            isCelebratory: Boolean = false,
            isSubscriberBenefit: Boolean = false,
            duration: Int = Snackbar.LENGTH_LONG
        ) {
            showSnackbar(
                container,
                leftImage,
                title,
                content,
                null,
                null,
                0,
                null,
                displayType,
                isCelebratory,
                isSubscriberBenefit,
                duration
            )
        }

        fun showSnackbar(
            container: ViewGroup,
            title: CharSequence?,
            content: CharSequence?,
            rightIcon: Drawable,
            rightTextColor: Int?,
            rightText: String,
            displayType: SnackbarDisplayType,
            isCelebratory: Boolean = false,
            isSubscriberBenefit: Boolean = false,
            duration: Int = Snackbar.LENGTH_LONG
        ) {
            showSnackbar(
                container,
                null,
                title,
                content,
                null,
                rightIcon,
                rightTextColor,
                rightText,
                displayType,
                isCelebratory,
                isSubscriberBenefit,
                duration
            )
        }

        fun showSnackbar(
            container: ViewGroup,
            title: CharSequence?,
            content: CharSequence?,
            specialView: View?,
            displayType: SnackbarDisplayType,
            isCelebratory: Boolean = false,
            isSubscriberBenefit: Boolean = false,
            duration: Int = Snackbar.LENGTH_LONG
        ) {
            showSnackbar(
                container,
                null,
                title,
                content,
                specialView,
                null,
                0,
                null,
                displayType,
                isCelebratory,
                isSubscriberBenefit,
                duration
            )
        }

        fun showSnackbar(
            container: ViewGroup,
            leftImage: Drawable?,
            title: CharSequence?,
            content: CharSequence?,
            specialView: View?,
            rightIcon: Drawable?,
            rightTextColor: Int?,
            rightText: String?,
            displayType: SnackbarDisplayType,
            isCelebratory: Boolean = false,
            isSubscriberBenefit: Boolean = false,
            duration: Int = Snackbar.LENGTH_LONG
        ) {
            val snackbar =
                make(container, duration)
                    .setSpecialView(specialView)
                    .setLeftIcon(leftImage)
            if (title?.isNotBlank() == true) {
                snackbar.setTitle(title)
            }
            if (content?.isNotBlank() == true) {
                if (title?.isNotBlank() != true) {
                    snackbar.setTitle(content)
                } else {
                    snackbar.setText(content)
                }
            }
            rightTextColor?.let {
                snackbar.setRightDiff(rightIcon, rightTextColor, rightText)
            }

            when (displayType) {
                SnackbarDisplayType.FAILURE -> snackbar.setBackgroundResource(R.drawable.snackbar_background_red)
                SnackbarDisplayType.BLACK -> snackbar.setBackgroundResource(R.drawable.snackbar_background_black)
                SnackbarDisplayType.FAILURE_BLUE, SnackbarDisplayType.BLUE ->
                    snackbar.setBackgroundResource(
                        R.drawable.snackbar_background_blue
                    )

                SnackbarDisplayType.DROP, SnackbarDisplayType.NORMAL ->
                    snackbar.setBackgroundResource(
                        R.drawable.snackbar_background_gray
                    )

                SnackbarDisplayType.SUCCESS -> snackbar.setBackgroundResource(R.drawable.snackbar_background_green)
                SnackbarDisplayType.SUBSCRIBER_BENEFIT -> {
                    snackbar.setBackgroundResource(R.drawable.subscriber_benefit_snackbar_bg)
                    snackbar.setTitleColor(
                        ContextCompat.getColor(
                            container.context,
                            R.color.green_1
                        )
                    )
                    snackbar.setTextColor(
                        ContextCompat.getColor(
                            container.context,
                            R.color.green_1
                        )
                    )
                }
            }

            if (isCelebratory) {
                showConfettiAnimation(container)
            } else if (isSubscriberBenefit) {
                showSubscriberBenefitAnimation(container, snackbar)
            }

            snackbar.show()
            if (displayType == SnackbarDisplayType.FAILURE || displayType == SnackbarDisplayType.FAILURE_BLUE) {
                container.postDelayed({
                    snackbar.getView().startAnimation(Animations.negativeShakeAnimation())
                }, 600L)
            }
        }

        private fun showSubscriberBenefitAnimation(
            container: ViewGroup,
            snackbar: HabiticaSnackbar
        ) {
            container.postDelayed(
                {
                    ParticleSystem(
                        container,
                        300,
                        ContextCompat.getDrawable(container.context, R.drawable.confetti_subs),
                        800L
                    )
                        .setFadeOut(200L)
                        .setSpeedRange(0.05f, 0.2f)
                        .setScaleRange(0.8f, 1.2f)
                        .setRotationSpeedRange(134f, 164f)
                        .emit(snackbar.getView(), 200, 600)
                },
                500L
            )
        }

        private fun showConfettiAnimation(container: ViewGroup) {
            container.postDelayed(
                {
                    ParticleSystem(
                        container,
                        30,
                        ContextCompat.getDrawable(container.context, R.drawable.confetti_blue),
                        6000
                    )
                        .setAcceleration(0.00070f, 90)
                        .setRotationSpeedRange(134f, 164f)
                        .setScaleRange(0.8f, 1.2f)
                        .setSpeedByComponentsRange(-0.15f, 0.15f, -0.15f, -0.45f)
                        .setFadeOut(200, AccelerateInterpolator())
                        .emitWithGravity(container, Gravity.BOTTOM, 7, 1000)
                    ParticleSystem(
                        container,
                        30,
                        ContextCompat.getDrawable(container.context, R.drawable.confetti_red),
                        6000
                    )
                        .setAcceleration(0.00060f, 90)
                        .setRotationSpeedRange(134f, 164f)
                        .setScaleRange(0.8f, 1.2f)
                        .setSpeedByComponentsRange(-0.15f, 0.15f, -0.15f, -0.45f)
                        .setFadeOut(200, AccelerateInterpolator())
                        .emitWithGravity(container, Gravity.BOTTOM, 7, 1000)
                    ParticleSystem(
                        container,
                        30,
                        ContextCompat.getDrawable(
                            container.context,
                            R.drawable.confetti_yellow
                        ),
                        6000
                    )
                        .setAcceleration(0.00070f, 90)
                        .setRotationSpeedRange(134f, 164f)
                        .setScaleRange(0.8f, 1.2f)
                        .setSpeedByComponentsRange(-0.15f, 0.15f, -0.15f, -0.45f)
                        .setFadeOut(200, AccelerateInterpolator())
                        .emitWithGravity(container, Gravity.BOTTOM, 7, 1000)
                    ParticleSystem(
                        container,
                        30,
                        ContextCompat.getDrawable(
                            container.context,
                            R.drawable.confetti_purple
                        ),
                        6000
                    )
                        .setAcceleration(0.00090f, 90)
                        .setRotationSpeedRange(134f, 164f)
                        .setScaleRange(0.8f, 1.2f)
                        .setSpeedByComponentsRange(-0.15f, 0.15f, -0.15f, -0.45f)
                        .setFadeOut(200, AccelerateInterpolator())
                        .emitWithGravity(container, Gravity.BOTTOM, 7, 1000)
                },
                500
            )
        }
    }
}

interface SnackbarActivity {
    fun snackbarContainer(): ViewGroup

    fun showSnackbar(
        leftImage: Drawable? = null,
        title: CharSequence? = null,
        content: CharSequence? = null,
        specialView: View? = null,
        rightIcon: Drawable? = null,
        rightTextColor: Int? = null,
        rightText: String? = null,
        displayType: HabiticaSnackbar.SnackbarDisplayType = HabiticaSnackbar.SnackbarDisplayType.NORMAL,
        isCelebratory: Boolean = false
    ) {
        HabiticaSnackbar.showSnackbar(
            snackbarContainer(),
            leftImage,
            title,
            content,
            specialView,
            rightIcon,
            rightTextColor,
            rightText,
            displayType,
            isCelebratory
        )
    }
}
