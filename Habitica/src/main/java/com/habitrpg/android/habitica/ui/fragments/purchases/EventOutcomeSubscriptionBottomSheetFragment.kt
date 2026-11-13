package com.habitrpg.android.habitica.ui.fragments.purchases

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.R

class EventOutcomeSubscriptionBottomSheetFragment : SubscriptionBottomSheetFragment() {
    var eventType: String = ""

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        when (eventType) {
            EVENT_ARMOIRE_OPENED -> setArmoireEventSubscriptionViews()
            EVENT_DEATH_SCREEN -> setDeathScreenEventSubscriptionViews()
            EVENT_GEMS_FOR_GOLD -> setGemsForGoldEventSubscriptionViews()
            EVENT_HOURGLASS_SHOP_OPENED -> setHourglassShopEventSubscriptionViews()
        }
        content?.subscription3month?.visibility = View.GONE
        content?.subscription6month?.visibility = View.GONE
    }

    private fun setArmoireEventSubscriptionViews() {
        content?.subscribeBenefitsTitle?.text = getString(R.string.subscribe_second_armoire_open_text)
        content?.subscriberBenefits?.hideArmoireBenefit()
    }

    private fun setDeathScreenEventSubscriptionViews() {
        content?.subscribeBenefitsTitle?.text = getString(R.string.subscribe_second_chance_incentive_text)
        content?.subscriberBenefits?.hideDeathBenefit()
    }

    private fun setGemsForGoldEventSubscriptionViews() {
        content?.subscribeBenefitsTitle?.text = getString(R.string.subscribe_gems_for_gold_incentive_text)
        content?.subscriberBenefits?.hideGemsForGoldBenefit()
        content?.subscription3month?.visibility = View.GONE
        content?.gemsForGoldBanner?.isVisible = true
    }

    private fun setHourglassShopEventSubscriptionViews() {
        content?.subscribeBenefitsTitle?.text = getString(R.string.subscribe_hourglass_incentive_text)
        skus.firstOrNull { buttonForSku(it)?.isVisible == true }?.let { selectSubscription(it) }
    }

    companion object {
        const val TAG = "EventOutcomeSubscriptionBottomSheet"
        const val EVENT_ARMOIRE_OPENED = "armoire_opened"
        const val EVENT_DEATH_SCREEN = "death_screen"
        const val EVENT_GEMS_FOR_GOLD = "gems_for_gold"
        const val EVENT_HOURGLASS_SHOP_OPENED = "hourglass_shop_opened"
    }
}
