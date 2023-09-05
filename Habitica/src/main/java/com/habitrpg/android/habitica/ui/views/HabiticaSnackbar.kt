package com.habitrpg.android.habitica.ui.views

import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.habitrpg.android.habitica.R
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

    fun setTitle(title: CharSequence?): HabiticaSnackbar {
        val textView = view.findViewById<View>(R.id.snackbar_title) as? TextView
        textView?.text = title
        textView?.visibility = if (title != null) View.VISIBLE else View.GONE
        return this
    }

    fun setText(text: CharSequence?): HabiticaSnackbar {
        val textView = view.findViewById<View>(R.id.snackbar_text) as? TextView
        textView?.text = text
        textView?.visibility = if (text != null) View.VISIBLE else View.GONE
        return this
    }

    fun setRightDiff(icon: Drawable?, textColor: Int, text: String?): HabiticaSnackbar {
        if (icon == null) {
            return this
        }
        val rightView = view.findViewById<View>(R.id.rightView)
        rightView.visibility = View.VISIBLE
        val rightIconView = view.findViewById<ImageView>(R.id.rightIconView)
        rightIconView.setImageDrawable(icon)
        val rightTextView = view.findViewById<TextView>(R.id.rightTextView)
        rightTextView.setTextColor(textColor)
        rightTextView.text = text
        return this
    }

    fun setLeftIcon(image: Drawable?): HabiticaSnackbar {
        val imageView = view.findViewById<ImageView>(R.id.leftImageView)
        imageView.setImageDrawable(image)
        imageView.visibility = if (image != null) View.VISIBLE else View.GONE
        return this
    }

    fun setBackgroundColor(@ColorInt color: Int): HabiticaSnackbar {
        view.setBackgroundColor(color)
        return this
    }

    fun setBackgroundResource(resourceId: Int): HabiticaSnackbar {
        val snackbarView = view.findViewById<View>(R.id.snackbar_view)
        snackbarView.setBackgroundResource(resourceId)
        view.setBackgroundColor(ContextCompat.getColor(context, R.color.transparent))
        return this
    }

    private fun setSpecialView(specialView: View?): HabiticaSnackbar {
        if (specialView != null) {
            val snackbarView = view.findViewById<View>(R.id.content_container) as? LinearLayout
            snackbarView?.addView(specialView)
        }
        return this
    }

    private class ContentViewCallback(private val content: View) :
        com.google.android.material.snackbar.ContentViewCallback {

        @Suppress("SameParameterValue")
        override fun animateContentIn(delay: Int, duration: Int) {
            content.scaleY = 0f
            content.scaleX = 0f
            ViewCompat.animate(content).scaleY(1f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
            ViewCompat.animate(content).scaleX(1f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
            ViewCompat.animate(content).alpha(1f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
        }

        override fun animateContentOut(delay: Int, duration: Int) {
            content.scaleY = 1f
            content.scaleX = 1f
            ViewCompat.animate(content).scaleY(0f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
            ViewCompat.animate(content).scaleX(0f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
            ViewCompat.animate(content).alpha(0f).setDuration(duration.toLong()).startDelay =
                delay.toLong()
        }
    }

    enum class SnackbarDisplayType {
        NORMAL, FAILURE, FAILURE_BLUE, DROP, SUCCESS, BLUE
    }

    companion object {

        const val MIN_LEVEL_FOR_SKILLS = 11

        private fun make(parent: ViewGroup, duration: Int): HabiticaSnackbar {
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
            isSubscriberBenefit: Boolean = false
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
                isSubscriberBenefit
            )
        }

        fun showSnackbar(
            container: ViewGroup,
            leftImage: Drawable,
            title: CharSequence?,
            content: CharSequence?,
            displayType: SnackbarDisplayType,
            isCelebratory: Boolean = false,
            isSubscriberBenefit: Boolean = false
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
                isSubscriberBenefit
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
            isSubscriberBenefit: Boolean = false
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
                isSubscriberBenefit
            )
        }

        fun showSnackbar(
            container: ViewGroup,
            title: CharSequence?,
            content: CharSequence?,
            specialView: View?,
            displayType: SnackbarDisplayType,
            isCelebratory: Boolean = false,
            isSubscriberBenefit: Boolean = false
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
                isSubscriberBenefit
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
            isSubscriberBenefit: Boolean = false
        ) {
            val snackbar = make(container, Snackbar.LENGTH_LONG)
                .setTitle(title)
                .setText(content)
                .setSpecialView(specialView)
                .setLeftIcon(leftImage)
            rightTextColor?.let {
                snackbar.setRightDiff(rightIcon, rightTextColor, rightText)
            }

            when (displayType) {
                SnackbarDisplayType.FAILURE -> snackbar.setBackgroundResource(R.drawable.snackbar_background_red)
                SnackbarDisplayType.FAILURE_BLUE, SnackbarDisplayType.BLUE -> snackbar.setBackgroundResource(
                    R.drawable.snackbar_background_blue
                )

                SnackbarDisplayType.DROP, SnackbarDisplayType.NORMAL -> snackbar.setBackgroundResource(
                    R.drawable.snackbar_background_gray
                )

                SnackbarDisplayType.SUCCESS -> snackbar.setBackgroundResource(R.drawable.snackbar_background_green)
            }

            if (isCelebratory) {
                showConfettiAnimation(container)
            } else if (isSubscriberBenefit) {
                showSubscriberBenefitAnimation(container, snackbar)
            }

            snackbar.show()
        }

        private fun showSubscriberBenefitAnimation(container: ViewGroup, snackbar: HabiticaSnackbar) {
            container.postDelayed(
                {
                    ParticleSystem(
                        container,
                        200,
                        ContextCompat.getDrawable(container.context, R.drawable.confetti_subs),
                        500L
                    )
                        .setFadeOut(200L)
                        .setSpeedRange(0.1f, 0.4f)
                        .setScaleRange(0.8f, 1.2f)
                        .setRotationSpeedRange(134f, 164f)
                        .emit(snackbar.getView(), 150, 500)
                }, 500L
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
