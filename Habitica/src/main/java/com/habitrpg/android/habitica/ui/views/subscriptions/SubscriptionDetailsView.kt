package com.habitrpg.android.habitica.ui.views.subscriptions

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.net.toUri
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.SubscriptionDetailsBinding
import com.habitrpg.android.habitica.extensions.toZonedDateTime
import com.habitrpg.android.habitica.models.user.SubscriptionPlan
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.layoutInflater
import java.text.DateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class SubscriptionDetailsView : LinearLayout {
    lateinit var binding: SubscriptionDetailsBinding

    private var plan: SubscriptionPlan? = null

    var onShowSubscriptionOptions: (() -> Unit)? = null
    var onUpdateSubscriptionsTapped: (() -> Unit)? = null

    var currentUserID: String? = null

    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet) {
        setupView()
    }

    constructor(context: Context) : super(context) {
        setupView()
    }

    private fun setupView() {
        binding = SubscriptionDetailsBinding.inflate(context.layoutInflater, this, true)
        binding.changeSubscriptionButton.setOnClickListener { changeSubscriptionButtonTapped() }
        binding.updateSubscriptionButton.setOnClickListener { onUpdateSubscriptionsTapped?.invoke() }
        binding.heartIcon.setImageBitmap(HabiticaIconsHelper.imageOfHeartLarge())
    }

    fun setPlan(plan: SubscriptionPlan) {
        this.plan = plan

        updateSubscriptionStatusPill(plan)

        var duration: String? = null

        if (plan.planId != null && plan.dateTerminated == null) {
            when (plan.planId) {
                SubscriptionPlan.PLANID_BASIC, SubscriptionPlan.PLANID_BASICEARNED -> {
                    duration = resources.getString(R.string.month)
                }

                SubscriptionPlan.PLANID_BASIC3MONTH -> {
                    duration = resources.getString(R.string.three_months)
                }

                SubscriptionPlan.PLANID_BASIC6MONTH, SubscriptionPlan.PLANID_GOOGLE6MONTH -> {
                    duration = resources.getString(R.string.six_months)
                }

                SubscriptionPlan.PLANID_BASIC12MONTH -> {
                    duration = resources.getString(R.string.twelve_months)
                }
            }
        }

        when {
            duration != null ->
                binding.subscriptionDurationTextView.text =
                    resources.getString(R.string.subscription_duration, duration)

            plan.isGroupPlanSub -> binding.subscriptionDurationTextView.setText(R.string.member_group_plan)
            plan.dateTerminated != null ->
                binding.subscriptionDurationTextView.text =
                    resources.getString(
                        R.string.benefits_end,
                        DateFormat.getDateInstance().format(plan.dateTerminated ?: Date())
                    )
        }

        if ((plan.extraMonths ?: 0) > 0) {
            binding.subscriptionCreditCard.visibility = VISIBLE
            if (plan.extraMonths == 1) {
                binding.subscriptionCreditTextView.text =
                    resources.getString(R.string.subscription_credit_canceling, 1)
            } else {
                binding.subscriptionCreditTextView.text =
                    resources.getString(R.string.subscription_credit_canceling, plan.extraMonths)
            }
        } else {
            binding.subscriptionCreditCard.visibility = GONE
        }

        binding.updateSubscriptionButton.visibility = GONE
        when (plan.paymentMethod) {
            "Amazon Payments" -> {
                binding.paymentProcessorImageView.setImageResource(R.drawable.payment_amazon)
                binding.subscriptionPaymentMethodTextview.text = context.getString(R.string.amazon)
            }

            "Apple" -> {
                binding.paymentProcessorImageView.setImageResource(R.drawable.payment_apple)
                binding.subscriptionPaymentMethodTextview.text =
                    context.getString(R.string.apple_pay)
            }

            "Google" -> {
                binding.paymentProcessorImageView.setImageResource(R.drawable.payment_google)
                val billingDate = plan.nextBillingDate
                if (billingDate != null) {
                    var paymentMethodString =
                        context.getString(
                            R.string.next_payment_date,
                            DateFormat.getDateInstance().format(billingDate)
                        )
                    if (plan.deferredPlanId != null) {
                        when (plan.deferredPlanId) {
                            SubscriptionPlan.PLANID_BASIC, SubscriptionPlan.PLANID_BASICEARNED -> paymentMethodString += "\n" + context.getString(
                                R.string.will_change_to_x_duration,
                                context.getString(R.string.month)
                            )
                            SubscriptionPlan.PLANID_BASIC3MONTH -> paymentMethodString += "\n" + context.getString(
                                R.string.will_change_to_x_duration,
                                context.getString(R.string.three_months)
                            )
                            SubscriptionPlan.PLANID_BASIC6MONTH, SubscriptionPlan.PLANID_GOOGLE6MONTH -> paymentMethodString += "\n" + context.getString(
                                R.string.will_change_to_x_duration,
                                context.getString(R.string.six_months)
                            )
                            SubscriptionPlan.PLANID_BASIC12MONTH -> paymentMethodString += "\n" + context.getString(
                                R.string.will_change_to_x_duration,
                                context.getString(R.string.twelve_months)
                            )
                        }
                    }
                    binding.subscriptionPaymentMethodTextview.text = paymentMethodString
                }
                binding.updateSubscriptionButton.visibility = VISIBLE
                if (plan.isActive && plan.dateTerminated != null) {
                    binding.updateSubscriptionButton.setText(R.string.subscribe_again)
                } else {
                    binding.updateSubscriptionButton.setText(R.string.change_subscription_plan)
                }
            }

            "PayPal" -> {
                binding.paymentProcessorImageView.setImageResource(R.drawable.payment_paypal)
                binding.subscriptionPaymentMethodTextview.text = context.getString(R.string.paypal)
            }

            "Stripe" -> {
                binding.paymentProcessorImageView.setImageResource(R.drawable.payment_stripe)
                binding.subscriptionPaymentMethodTextview.text =
                    context.getString(R.string.stripe_payment)
            }

            else -> {
                if (plan.isGiftedSub) {
                    binding.paymentProcessorImageView.setImageResource(R.drawable.payment_gift)
                    binding.subscriptionPaymentMethodTextview.text =
                        context.getString(R.string.gifted)
                    binding.updateSubscriptionButton.visibility = VISIBLE
                    binding.updateSubscriptionButton.setText(R.string.upgrade_subscription)
                } else {
                    binding.paymentProcessorWrapper.visibility = GONE
                }
            }
        }

        binding.monthsSubscribedTextView.text = plan.consecutive?.count.toString()
        binding.gemCapTextView.text = plan.totalNumberOfGems.toString()

        val now = LocalDate.now()
        val nextHourglassDate =
            now.plusMonths(plan.monthsUntilNextHourglass.toLong())
                .withDayOfMonth(1)
        val terminatedLocalDate = plan.dateTerminated?.toZonedDateTime()?.toLocalDate()
        if (plan.isActive && (terminatedLocalDate == null || nextHourglassDate.isBefore(terminatedLocalDate))) {
            val format =
                if (now.year != nextHourglassDate.year) {
                    "MMMM yyyy"
                } else {
                    "LLLL"
                }
            val nextHourglassMonth = DateTimeFormatter.ofPattern(format).format(nextHourglassDate)
            nextHourglassMonth?.let { binding.nextHourglassTextview.text = it }
            binding.resubscribeForHourglassesLabel.visibility = GONE
        } else {
            binding.nextHourglassTextview.visibility = GONE
            binding.nextHourglassLabel.visibility = GONE
            binding.resubscribeForHourglassesLabel.visibility = VISIBLE
        }

        binding.changeSubscriptionButton.visibility = VISIBLE
        if (plan.paymentMethod != null) {
            binding.changeSubscriptionTitle.setText(R.string.cancel_subscription)
            if (plan.paymentMethod == "Google") {
                binding.changeSubscriptionDescription.setText(R.string.cancel_subscription_google_description)
                binding.changeSubscriptionButton.setText(R.string.open_in_store)
            } else {
                if (plan.isGroupPlanSub) {
                    binding.changeSubscriptionDescription.setText(R.string.cancel_subscription_group_plan)
                    binding.changeSubscriptionButton.visibility = GONE
                } else if (plan.paymentMethod == "Apple") {
                    binding.changeSubscriptionDescription.setText(R.string.cancel_subscription_apple_description)
                    binding.changeSubscriptionButton.visibility = GONE
                } else {
                    binding.changeSubscriptionDescription.setText(R.string.cancel_subscription_notgoogle_description)
                    binding.changeSubscriptionButton.setText(R.string.open_habitica_website)
                }
            }
        }
        if (plan.dateTerminated != null) {
            binding.changeSubscriptionTitle.setText(R.string.resubscribe)
            binding.changeSubscriptionDescription.setText(R.string.resubscribe_description)
            binding.changeSubscriptionButton.setText(R.string.renew_subscription)
            binding.changeSubscriptionWrapper.visibility = GONE
        }
    }

    private fun updateSubscriptionStatusPill(plan: SubscriptionPlan) {
        if (plan.isActive) {
            if (plan.dateTerminated != null) {
                if (plan.isGiftedSub) {
                    binding.subscriptionStatusNotRecurring.visibility = VISIBLE
                    binding.subscriptionStatusCancelled.visibility = GONE
                } else {
                    binding.subscriptionStatusNotRecurring.visibility = GONE
                    binding.subscriptionStatusCancelled.visibility = VISIBLE
                }
                binding.subscriptionStatusActive.visibility = GONE
                binding.subscriptionStatusGroupPlan.visibility = GONE
            } else {
                if (plan.isGroupPlanSub) {
                    binding.subscriptionStatusGroupPlan.visibility = VISIBLE
                    binding.subscriptionStatusActive.visibility = GONE
                    binding.subscriptionStatusNotRecurring.visibility = GONE
                } else {
                    binding.subscriptionStatusActive.visibility = VISIBLE
                    binding.subscriptionStatusNotRecurring.visibility = GONE
                    binding.subscriptionStatusGroupPlan.visibility = GONE
                }
            }
            binding.subscriptionStatusInactive.visibility = GONE
        } else {
            binding.subscriptionStatusActive.visibility = GONE
            binding.subscriptionStatusInactive.visibility = VISIBLE
            binding.subscriptionStatusNotRecurring.visibility = GONE
            binding.subscriptionStatusGroupPlan.visibility = GONE
        }
    }

    private fun changeSubscriptionButtonTapped() {
        if (plan?.paymentMethod != null && plan?.dateTerminated == null) {
            val url =
                if (plan?.paymentMethod == "Google") {
                    "https://play.google.com/store/account/subscriptions"
                } else {
                    context.getString(R.string.base_url) + "/"
                }
            context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
        } else if (plan?.dateTerminated != null) {
            onShowSubscriptionOptions?.invoke()
        }
    }
}
