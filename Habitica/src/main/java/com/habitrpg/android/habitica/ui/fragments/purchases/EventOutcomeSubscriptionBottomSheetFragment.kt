package com.habitrpg.android.habitica.ui.fragments.purchases

import android.os.Bundle
import android.view.View
import com.habitrpg.android.habitica.R

class EventOutcomeSubscriptionBottomSheetFragment : SubscriptionBottomSheetFragment() {

    var eventType: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        when (eventType) {
            EVENT_ARMOIRE_OPENED -> setArmoireEventSubscriptionViews()
            EVENT_DEATH_SCREEN -> setDeathScreenEventSubscriptionViews()
            EVENT_GEMS_FOR_GOLD -> setGemsForGoldEventSubscriptionViews()
        }

    }

    private fun setArmoireEventSubscriptionViews() {
        binding.subscriberBenefitBanner.visibility = View.GONE
        binding.subscribeBenefits.text = getString(R.string.subscribe_second_armoire_open_text)
        binding.subscriberBenefits.hideArmoireBenefit()
    }

    private fun setDeathScreenEventSubscriptionViews() {
        binding.subscriberBenefitBanner.visibility = View.GONE
        binding.subscribeBenefits.text = getString(R.string.subscribe_second_chance_incentive_text)
        binding.subscriberBenefits.hideDeathBenefit()
    }

    private fun setGemsForGoldEventSubscriptionViews() {
        binding.subscribeBenefits.text = getString(R.string.subscribe_gems_for_gold_incentive_text)
        binding.subscriberBenefits.hideGemsForGoldBenefit()
        binding.subscription3month.visibility = View.GONE
    }

    companion object {
        const val TAG = "EventOutcomeSubscriptionBottomSheet"
        const val EVENT_ARMOIRE_OPENED = "armoire_opened"
        const val EVENT_DEATH_SCREEN = "death_screen"
        const val EVENT_GEMS_FOR_GOLD = "gems_for_gold"
    }
}
