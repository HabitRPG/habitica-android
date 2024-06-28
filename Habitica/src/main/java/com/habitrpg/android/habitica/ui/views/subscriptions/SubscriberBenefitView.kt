package com.habitrpg.android.habitica.ui.views.subscriptions

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.core.view.isVisible
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.SubscriptionBenefitsBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class SubscriberBenefitView
@JvmOverloads
constructor(
    context: Context,
    attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    private val binding: SubscriptionBenefitsBinding

    @Inject
    lateinit var configManager: AppConfigManager

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ThisEntryPoint {
        fun configManager(): AppConfigManager

        fun inventoryRepository(): InventoryRepository
    }

    init {
        binding = SubscriptionBenefitsBinding.inflate(context.layoutInflater, this)
        orientation = VERTICAL
        val hiltEntryPoint =
            EntryPointAccessors.fromApplication(context, ThisEntryPoint::class.java)
        configManager = hiltEntryPoint.configManager()
        inventoryRepository = hiltEntryPoint.inventoryRepository()

        MainScope().launchCatching {
            val item = inventoryRepository.getLatestMysteryItem().firstOrNull()
            binding.subBenefitsMysteryItemIcon.loadImage(
                "shop_set_mystery_${
                item?.key?.split(
                    "_"
                )?.last()
                }"
            )
            binding.subBenefitsMysteryItemText.text =
                context.getString(R.string.subscribe_listitem3_description_new, item?.text)
        }

        binding.benefitArmoireWrapper.isVisible = configManager.enableArmoireSubs()
        binding.benefitFaintWrapper.isVisible = configManager.enableFaintSubs()
    }

    fun hideDeathBenefit() {
        binding.benefitFaintWrapper.isVisible = false
    }

    fun hideArmoireBenefit() {
        binding.benefitArmoireWrapper.isVisible = false
    }

    fun hideGemsForGoldBenefit() {
        binding.benefitGemsForGoldWrapper.isVisible = false
    }

    fun hideMysticHourglassBenefit() {
        binding.benefitHourglassesWrapper.isVisible = false
    }
}
