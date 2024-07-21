package com.habitrpg.android.habitica.ui.views.ads

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleCoroutineScope
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.AdButtonBinding
import com.habitrpg.android.habitica.extensions.getMinuteOrSeconds
import com.habitrpg.android.habitica.extensions.getShortRemainingString
import com.habitrpg.android.habitica.helpers.AdHandler
import com.habitrpg.android.habitica.helpers.AdType
import com.habitrpg.common.habitica.extensionsCommon.layoutInflater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class AdButton
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
    ) : LinearLayout(context, attrs) {
        var state: State = State.READY
            set(value) {
                field = value
                updateViews()
            }

        enum class State {
            EMPTY,
            READY,
            LOADING,
            UNAVAILABLE,
        }

        private var updateJob: Job? = null
        private var nextAdDate: Date? = null
        private val binding = AdButtonBinding.inflate(context.layoutInflater, this)

        private var activeBackgroundRes: Int = R.drawable.ad_button_background

        var text: String = ""
            set(value) {
                field = value
                updateViews()
            }

        init {
            context.theme?.obtainStyledAttributes(
                attrs,
                R.styleable.AdButton,
                0,
                0,
            )?.let { attributes ->
                text = attributes.getString(R.styleable.AdButton_text) ?: ""
                binding.currencyView.currency = attributes.getString(R.styleable.AdButton_currency)
                activeBackgroundRes =
                    attributes.getResourceId(
                        R.styleable.AdButton_activeBackground,
                        R.drawable.ad_button_background,
                    )
                binding.textView.setTextColor(
                    attributes.getColor(
                        R.styleable.AdButton_textColor,
                        ContextCompat.getColor(context, R.color.white),
                    ),
                )
            }
            binding.currencyView.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.currencyView.value = 0.0
            if (binding.currencyView.currency?.isNotBlank() != true) {
                binding.currencyView.visibility = View.GONE
            }
            gravity = Gravity.CENTER
            state = State.READY
        }

        private fun updateViews() {
            when (state) {
                State.READY -> {
                    binding.loadingIndicator.visibility = GONE
                    binding.textView.text = text
                    binding.textView.alpha = 1.0f
                    binding.textView.visibility = VISIBLE
                    binding.currencyView.visibility = VISIBLE
                    setBackgroundResource(activeBackgroundRes)
                }

                State.UNAVAILABLE -> {
                    binding.loadingIndicator.visibility = GONE
                    binding.textView.text =
                        context.getString(
                            R.string.available_in,
                            nextAdDate?.getShortRemainingString() ?: "",
                        )
                    binding.textView.alpha = 0.75f
                    binding.textView.visibility = VISIBLE
                    binding.currencyView.visibility = GONE
                    setBackgroundResource(R.drawable.ad_button_background_disabled)
                }

                State.EMPTY -> {
                    binding.loadingIndicator.visibility = GONE
                    binding.textView.visibility = GONE
                    binding.currencyView.visibility = GONE
                }

                State.LOADING -> {
                    binding.loadingIndicator.visibility = VISIBLE
                    binding.textView.visibility = GONE
                    binding.currencyView.visibility = GONE
                }
            }
            isEnabled = state == State.READY
        }

        fun updateForAdType(
            type: AdType,
            lifecycleScope: LifecycleCoroutineScope,
        ) {
            if (updateJob?.isActive == true) {
                updateJob?.cancel()
            }
            nextAdDate = AdHandler.nextAdAllowedDate(type)
            if (nextAdDate?.after(Date()) == true) {
                updateJob =
                    lifecycleScope.launch(Dispatchers.Main) {
                        while (nextAdDate?.after(Date()) == true) {
                            val remaining =
                                (
                                    (
                                        nextAdDate?.time
                                            ?: 0L
                                    ) - Date().time
                                ).toDuration(DurationUnit.MILLISECONDS)
                            state = if (remaining.isNegative()) State.READY else State.UNAVAILABLE
                            updateViews()
                            delay(1.toDuration(remaining.getMinuteOrSeconds()))
                        }
                        state = State.READY
                    }
            }
        }
    }
