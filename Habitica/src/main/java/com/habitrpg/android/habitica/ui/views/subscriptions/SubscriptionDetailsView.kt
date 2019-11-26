package com.habitrpg.android.habitica.ui.views.subscriptions


import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.SubscriptionDetailsBinding
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.user.SubscriptionPlan
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import java.text.DateFormat
import java.util.*

class SubscriptionDetailsView : LinearLayout {

    lateinit var binding: SubscriptionDetailsBinding

    private var plan: SubscriptionPlan? = null

    var onShowSubscriptionOptions: (() -> Unit)? = null

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
        binding.heartIcon.setImageDrawable(BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfHeartLightBg()))
    }

    fun setPlan(plan: SubscriptionPlan) {
        this.plan = plan

        updateSubscriptionStatusPill(plan)

        var duration: String? = null

        if (plan.planId != null) {
            if (plan.planId == SubscriptionPlan.PLANID_BASIC || plan.planId == SubscriptionPlan.PLANID_BASICEARNED) {
                duration = resources.getString(R.string.month)
            } else if (plan.planId == SubscriptionPlan.PLANID_BASIC3MONTH) {
                duration = resources.getString(R.string.three_months)
            } else if (plan.planId == SubscriptionPlan.PLANID_BASIC6MONTH || plan.planId == SubscriptionPlan.PLANID_GOOGLE6MONTH) {
                duration = resources.getString(R.string.six_months)
            } else if (plan.planId == SubscriptionPlan.PLANID_BASIC12MONTH) {
                duration = resources.getString(R.string.twelve_months)
            }
        }

        when {
            duration != null -> binding.subscriptionDurationTextView.text = resources.getString(R.string.subscription_duration, duration)
            plan.isGroupPlanSub -> binding.subscriptionDurationTextView.setText(R.string.member_group_plan)
            plan.dateTerminated != null -> binding.subscriptionDurationTextView.text = resources.getString(R.string.ending_on, DateFormat.getDateInstance().format(plan.dateTerminated ?: Date()))
        }

        if (plan.extraMonths > 0) {
            binding.subscriptionCreditWrapper.visibility = View.VISIBLE
            if (plan.extraMonths == 1) {
                binding.subscriptionCreditTextView.text = resources.getString(R.string.one_month)
            } else {
                binding.subscriptionCreditTextView.text = resources.getString(R.string.x_months, plan.extraMonths)
            }
        } else {
            binding.subscriptionCreditWrapper.visibility = View.GONE
        }

        when (plan.paymentMethod) {
            "Amazon Payments" -> binding.paymentProcessorImageView.setImageResource(R.drawable.payment_amazon)
            "Apple" -> binding.paymentProcessorImageView.setImageResource(R.drawable.payment_apple)
            "Google" -> binding.paymentProcessorImageView.setImageResource(R.drawable.payment_google)
            "PayPal" -> binding.paymentProcessorImageView.setImageResource(R.drawable.payment_paypal)
            "Stripe" -> binding.paymentProcessorImageView.setImageResource(R.drawable.payment_stripe)
             else -> {
                 if (plan.isGiftedSub) {
                     binding.paymentProcessorImageView.setImageResource(R.drawable.payment_gift)
                     binding.subscriptionPaymentMethodTextview.text = context.getString(R.string.gifted)
                 } else {
                     binding.paymentProcessorWrapper.visibility = View.GONE
                 }
             }
        }

        if (plan.consecutive?.count == 1) {
            binding.monthsSubscribedTextView.text = resources.getString(R.string.one_month)
        } else {
            binding.monthsSubscribedTextView.text = resources.getString(R.string.x_months, plan.consecutive?.count ?: 0)
        }
        binding.gemCapTextView.text = plan.totalNumberOfGems().toString()
        binding.currentHourglassesTextView.text = plan.consecutive?.trinkets.toString()

        binding.changeSubscriptionButton.visibility = View.VISIBLE
        if (plan.paymentMethod != null) {
            binding.changeSubscriptionTitle.setText(R.string.cancel_subscription)
            if (plan.paymentMethod == "Google") {
                binding.changeSubscriptionDescription.setText(R.string.cancel_subscription_google_description)
                binding.changeSubscriptionButton.setText(R.string.open_in_store)
            } else {
                if (plan.isGroupPlanSub) {
                    /*if (plan.ownerID == currentUserID) {
                        binding.changeSubscriptionDescription.setText(R.string.cancel_subscription_group_plan_owner)
                    } else {*/
                        binding.changeSubscriptionDescription.setText(R.string.cancel_subscription_group_plan)
                        binding.changeSubscriptionButton.visibility = View.GONE
                    //}
                } else {
                    binding.changeSubscriptionDescription.setText(R.string.cancel_subscription_notgoogle_description)
                }
                binding.changeSubscriptionButton.setText(R.string.visit_habitica_website)
            }
        }
        if (plan.dateTerminated != null) {
            binding.changeSubscriptionTitle.setText(R.string.resubscribe)
            binding.changeSubscriptionDescription.setText(R.string.resubscribe_description)
            binding.changeSubscriptionButton.setText(R.string.renew_subscription)
        }
    }

    private fun updateSubscriptionStatusPill(plan: SubscriptionPlan) {
        if (plan.isActive) {
            if (plan.dateTerminated != null) {
                if (plan.isGiftedSub) {
                    binding.subscriptionStatusNotRecurring.visibility = View.VISIBLE
                    binding.subscriptionStatusCancelled.visibility = View.GONE
                } else {
                    binding.subscriptionStatusNotRecurring.visibility = View.GONE
                    binding.subscriptionStatusCancelled.visibility = View.VISIBLE
                }
                binding.subscriptionStatusActive.visibility = View.GONE
                binding.subscriptionStatusGroupPlan.visibility = View.GONE
            } else {
                if (plan.isGroupPlanSub) {
                    binding.subscriptionStatusGroupPlan.visibility = View.VISIBLE
                    binding.subscriptionStatusActive.visibility = View.GONE
                    binding.subscriptionStatusNotRecurring.visibility = View.GONE
                } else {
                    binding.subscriptionStatusActive.visibility = View.VISIBLE
                    binding.subscriptionStatusNotRecurring.visibility = View.GONE
                    binding.subscriptionStatusGroupPlan.visibility = View.GONE
                }
            }
            binding.subscriptionStatusInactive.visibility = View.GONE
        } else {
            binding.subscriptionStatusActive.visibility = View.GONE
            binding.subscriptionStatusInactive.visibility = View.VISIBLE
            binding.subscriptionStatusNotRecurring.visibility = View.GONE
            binding.subscriptionStatusGroupPlan.visibility = View.GONE
        }
    }

    private fun changeSubscriptionButtonTapped() {
        if (plan?.paymentMethod != null) {
            val url = if (plan?.paymentMethod == "Google") {
                "https://play.google.com/store/account/subscriptions"
            } else {
                context.getString(R.string.base_url) + "/"
            }
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } else if (plan?.dateTerminated != null) {
            onShowSubscriptionOptions?.invoke()
        }
    }
}
