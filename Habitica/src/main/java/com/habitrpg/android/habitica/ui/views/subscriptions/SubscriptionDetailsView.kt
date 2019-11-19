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

        if (plan.isActive) {
            if (plan.dateTerminated != null) {
                if (plan.customerId == "Gift") {
                    binding.subscriptionStatusNotRecurring.visibility = View.VISIBLE
                    binding.subscriptionStatusCancelled.visibility = View.GONE
                } else {
                    binding.subscriptionStatusNotRecurring.visibility = View.GONE
                    binding.subscriptionStatusCancelled.visibility = View.VISIBLE
                }
                binding.subscriptionStatusActive.visibility = View.GONE
            } else {
                binding.subscriptionStatusActive.visibility = View.VISIBLE
                binding.subscriptionStatusNotRecurring.visibility = View.GONE
            }
            binding.subscriptionStatusInactive.visibility = View.GONE
        } else {
            binding.subscriptionStatusActive.visibility = View.GONE
            binding.subscriptionStatusInactive.visibility = View.VISIBLE
            binding.subscriptionStatusNotRecurring.visibility = View.GONE
        }

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

        if (duration != null) {
            binding.subscriptionDurationTextView.text = resources.getString(R.string.subscription_duration, duration)
        } else if (plan.dateTerminated != null) {
            binding.subscriptionDurationTextView.text = resources.getString(R.string.ending_on, DateFormat.getDateInstance().format(plan.dateTerminated ?: Date()))
        }

        when (plan.paymentMethod) {
            "Amazon" -> binding.paymentProcessorImageView.setImageResource(R.drawable.payment_amazon)
            "Apple" -> binding.paymentProcessorImageView.setImageResource(R.drawable.payment_apple)
            "Google" -> binding.paymentProcessorImageView.setImageResource(R.drawable.payment_google)
            "Stripe" -> binding.paymentProcessorImageView.setImageResource(R.drawable.payment_stripe)
             else -> {
                 if (plan.customerId == "Gift") {
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

        if (plan.paymentMethod != null) {
            binding.changeSubscriptionTitle.setText(R.string.cancel_subscription)
            if (plan.paymentMethod == "Google") {
                binding.changeSubscriptionDescription.setText(R.string.cancel_subscription_google_description)
                binding.changeSubscriptionButton.setText(R.string.open_in_store)
            } else {
                binding.changeSubscriptionDescription.setText(R.string.cancel_subscription_notgoogle_description)
                binding.changeSubscriptionButton.setText(R.string.visit_habitica_website)
            }
        }
        if (plan.dateTerminated != null) {
            binding.changeSubscriptionTitle.setText(R.string.resubscribe)
            binding.changeSubscriptionDescription.setText(R.string.resubscribe_description)
            binding.changeSubscriptionButton.setText(R.string.renew_subscription)
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
