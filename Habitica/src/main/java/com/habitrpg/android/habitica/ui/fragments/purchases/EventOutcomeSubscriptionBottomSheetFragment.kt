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
        }

    }

    private fun setArmoireEventSubscriptionViews() {
        binding.subscriberBenefitBanner.visibility = View.GONE
        binding.subscribeBenefits.text = getString(R.string.subscribe_second_armoire_open_text)
    }

    private fun setDeathScreenEventSubscriptionViews() {
        binding.subscriberBenefitBanner.visibility = View.GONE
        binding.subscribeBenefits.text = getString(R.string.subscribe_second_chance_incentive_text)
    }

    companion object {
        const val TAG = "EventOutcomeSubscriptionBottomSheet"
        const val EVENT_ARMOIRE_OPENED = "armoire_opened"
        const val EVENT_DEATH_SCREEN = "death_screen"
    }
}