/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.habitrpg.wearos.habitica.ui.views

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.graphics.Color
import android.provider.Settings
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import androidx.annotation.StyleRes
import androidx.annotation.VisibleForTesting
import androidx.core.content.res.use
import androidx.core.os.ConfigurationCompat
import androidx.core.view.isGone
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.CurvedTimeTextBinding
import com.habitrpg.android.habitica.databinding.StraightTimeTextBinding
import com.habitrpg.wearos.habitica.ui.views.TimeText.Clock
import com.habitrpg.wearos.habitica.ui.views.TimeTextViewBinding.TimeTextCurvedViewBinding
import com.habitrpg.wearos.habitica.ui.views.TimeTextViewBinding.TimeTextStraightViewBinding
import java.util.Calendar

/**
 * The max sweep angle for the [TimeText] to occupy.
 */
private const val MAX_SWEEP_ANGLE = 90f

class TimeText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = 0,
    @StyleRes defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {

    /**
     * The underlying [Calendar] instance for producing the time.
     *
     * This will be updated in [onTimeZoneChange] in response to any timezone updates.
     */
    private var time = Calendar.getInstance()

    /**
     * True if we should format the time in the 24 hour manner.
     *
     * This will be updated in [onTimeFormatChange] in response to any format updates.
     */
    private var use24HourFormat = DateFormat.is24HourFormat(context)

    /**
     * An [IntentFilter] for any time related broadcast.
     */
    private val timeBroadcastReceiverFilter = IntentFilter().apply {
        addAction(Intent.ACTION_TIME_TICK)
        addAction(Intent.ACTION_TIME_CHANGED)
        addAction(Intent.ACTION_TIMEZONE_CHANGED)
    }

    private val timeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                Intent.ACTION_TIMEZONE_CHANGED -> onTimeZoneChange()
                Intent.ACTION_TIME_TICK, Intent.ACTION_TIME_CHANGED -> onTimeChange()
            }
        }
    }

    /**
     * The wrapped view binding for the inflated views.
     */
    private val timeTextViewBinding: TimeTextViewBinding

    /**
     * A non-clock portion of the time text to display.
     */
    var title: CharSequence? = null
        set(value) {
            field = value

            timeTextViewBinding.timeTextTitle.text = title

            // Only show the title and divider if the title is non-empty
            val hideTitle = title.isNullOrEmpty()
            timeTextViewBinding.timeTextTitle.view.isGone = hideTitle
            timeTextViewBinding.timeTextDivider.view.isGone = hideTitle
        }

    /**
     * The color of the non-clock portion of the time text.
     */
    var titleTextColor: Int = Color.WHITE
        set(value) {
            field = value

            timeTextViewBinding.timeTextTitle.textColor = titleTextColor
        }

    /**
     * The backing [Clock] used to drive the time.
     *
     * Overridable for testing.
     */
    @VisibleForTesting
    var clock: Clock = Clock(System::currentTimeMillis)
        set(value) {
            field = value
            onTimeChange()
        }

    /**
     * The [ContentObserver] listening for a time format change.
     *
     * This is constructed lazily, since [getHandler] needs the view to be attached.
     */
    private val timeContentObserver by lazy(LazyThreadSafetyMode.NONE) {
        object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                onTimeFormatChange()
            }
        }
    }

    init {
        val layoutInflater = LayoutInflater.from(context)

        // Create the view structure based on whether the screen is round.
        // This will inflate one of two distinct layouts, which we abstract away in a TimeTextViewBinding
        timeTextViewBinding = if (resources.configuration.isScreenRound) {
            TimeTextCurvedViewBinding(CurvedTimeTextBinding.inflate(layoutInflater, this, true))
        } else {
            TimeTextStraightViewBinding(StraightTimeTextBinding.inflate(layoutInflater, this, true))
        }

        // Set the divider text
        timeTextViewBinding.timeTextDivider.text = "·"

        // Update based on the styled attributes.
        // Note that this runs the side-effects of setting those attributes.
        context.obtainStyledAttributes(attrs, R.styleable.TimeText, defStyleAttr, defStyleRes)
            .use { typedArray ->
                titleTextColor =
                    typedArray.getColor(R.styleable.TimeText_android_titleTextColor, titleTextColor)
                title = typedArray.getString(R.styleable.TimeText_titleText)
            }
    }

    /**
     * Restrict the total sweep angle on round screens to [MAX_SWEEP_ANGLE].
     *
     * We accomplish this with two measure passes:
     *
     * After the first, we measure to get the angle that the clock and divider occupy (together, these shouldn't ever
     * be more than [MAX_SWEEP_ANGLE].
     *
     * Then, we update the title's max sweep angle to the remaining angle, and measure again to apply the limit.
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        when (timeTextViewBinding) {
            is TimeTextCurvedViewBinding -> {
                // Reset the title sweep to ensure we get a true initial measurement
                timeTextViewBinding.timeTextTitle.view.setSweepRangeDegrees(0f, MAX_SWEEP_ANGLE)
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)

                val clockSweepAngle =
                    timeTextViewBinding.timeTextClock.view.sweepAngleDegrees.coerceAtLeast(0f)

                // Avoid getting the divider sweep angle if it is gone, since it won't be accurate
                val dividerSweepAngle = if (timeTextViewBinding.timeTextDivider.view.isGone) {
                    0f
                } else {
                    timeTextViewBinding.timeTextDivider.view.sweepAngleDegrees.coerceAtLeast(0f)
                }

                val maxTitleSweepAngle = MAX_SWEEP_ANGLE - clockSweepAngle - dividerSweepAngle

                // Update the title max sweep angle to effectively get a total max sweep of MAX_SWEEP_ANGLE
                timeTextViewBinding.timeTextTitle.view.setSweepRangeDegrees(0f, maxTitleSweepAngle)

                // Measure again, with the updated max sweep
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
            is TimeTextStraightViewBinding -> {
                // Need to do nothing special the for the straight view, just call through to super
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        onTimeZoneChange()
        onTimeFormatChange()
        onTimeChange()

        context.contentResolver.registerContentObserver(
            Settings.System.getUriFor(Settings.System.TIME_12_24),
            true,
            timeContentObserver
        )
        context.registerReceiver(timeBroadcastReceiver, timeBroadcastReceiverFilter)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        context.contentResolver.unregisterContentObserver(timeContentObserver)
        context.unregisterReceiver(timeBroadcastReceiver)
    }

    private fun onTimeChange() {
        val pattern = DateFormat.getBestDateTimePattern(
            ConfigurationCompat.getLocales(resources.configuration)[0],
            if (use24HourFormat) "Hm" else "hm"
        )
        // Remove the am/pm indicator (if any). This is locale safe.
        val patternWithoutAmPm = pattern.replace("a", "").trim()

        time.timeInMillis = clock.getCurrentTimeMillis()
        timeTextViewBinding.timeTextClock.text = DateFormat.format(patternWithoutAmPm, time)
    }

    private fun onTimeZoneChange() {
        time = Calendar.getInstance()
        onTimeChange()
    }

    private fun onTimeFormatChange() {
        use24HourFormat = DateFormat.is24HourFormat(context)
        onTimeChange()
    }

    /**
     * A provider of the current time.
     */
    fun interface Clock {

        /**
         * Returns the current time in milliseconds since the epoch.
         */
        fun getCurrentTimeMillis(): Long
    }
}

/**
 * An abstraction around the view binding, since we inflate two different layouts depending on the shape of the screen.
 */
private sealed class TimeTextViewBinding {

    abstract val timeTextTitle: TextViewWrapper

    abstract val timeTextDivider: TextViewWrapper

    abstract val timeTextClock: TextViewWrapper

    /**
     * The [TimeTextViewBinding] wrapping the [CurvedTimeTextBinding].
     */
    class TimeTextCurvedViewBinding(
        timeTextBinding: CurvedTimeTextBinding
    ) : TimeTextViewBinding() {
        override val timeTextTitle: CurvedTextViewWrapper =
            CurvedTextViewWrapper(timeTextBinding.timeTextTitle)
        override val timeTextDivider: CurvedTextViewWrapper =
            CurvedTextViewWrapper(timeTextBinding.timeTextDivider)
        override val timeTextClock: CurvedTextViewWrapper =
            CurvedTextViewWrapper(timeTextBinding.timeTextClock)
    }

    /**
     * The [TimeTextViewBinding] wrapping the [StraightTimeTextBinding].
     */
    class TimeTextStraightViewBinding(
        timeTextBinding: StraightTimeTextBinding
    ) : TimeTextViewBinding() {
        override val timeTextTitle: NormalTextViewWrapper =
            NormalTextViewWrapper(timeTextBinding.timeTextTitle)
        override val timeTextDivider: NormalTextViewWrapper =
            NormalTextViewWrapper(timeTextBinding.timeTextDivider)
        override val timeTextClock: NormalTextViewWrapper =
            NormalTextViewWrapper(timeTextBinding.timeTextClock)
    }
}
