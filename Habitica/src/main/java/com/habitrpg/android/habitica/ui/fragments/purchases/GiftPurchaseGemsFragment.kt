package com.habitrpg.android.habitica.ui.fragments.purchases

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.HabiticaPurchaseVerifier
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentGiftGemPurchaseBinding
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.proxy.CrashlyticsProxy
import com.habitrpg.android.habitica.ui.GemPurchaseOptionsView
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import javax.inject.Inject

class GiftPurchaseGemsFragment : BaseFragment<FragmentGiftGemPurchaseBinding>() {

    @Inject
    lateinit var crashlyticsProxy: CrashlyticsProxy
    @Inject
    lateinit var socialRepository: SocialRepository

    override var binding: FragmentGiftGemPurchaseBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGiftGemPurchaseBinding {
        return FragmentGiftGemPurchaseBinding.inflate(inflater, container, false)
    }

    var giftedMember: Member? = null
    set(value) {
        field = value
        field?.let {
            binding?.avatarView?.setAvatar(it)
            binding?.displayNameTextview?.username = it.profile?.name
            binding?.displayNameTextview?.tier = it.contributor?.level ?: 0
            binding?.usernameTextview?.text = "@${it.username}"
        }
    }

    private var purchaseHandler: PurchaseHandler? = null

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.gems4View?.setOnPurchaseClickListener { purchaseGems(PurchaseTypes.Purchase4Gems) }
        binding?.gems21View?.setOnPurchaseClickListener { purchaseGems(PurchaseTypes.Purchase21Gems) }
        binding?.gems42View?.setOnPurchaseClickListener { purchaseGems(PurchaseTypes.Purchase42Gems) }
        binding?.gems84View?.setOnPurchaseClickListener({ purchaseGems(PurchaseTypes.Purchase84Gems) })
    }

    fun setupCheckout() {
        purchaseHandler?.getAllGemSKUs { skus ->
            for (sku in skus) {
                updateButtonLabel(sku.id.code, sku.price)
            }
        }
    }

    fun setPurchaseHandler(handler: PurchaseHandler?) {
        this.purchaseHandler = handler
    }

    private fun updateButtonLabel(sku: String, price: String) {
        val matchingView: GemPurchaseOptionsView? = when (sku) {
            PurchaseTypes.Purchase4Gems -> binding?.gems4View
            PurchaseTypes.Purchase21Gems -> binding?.gems21View
            PurchaseTypes.Purchase42Gems -> binding?.gems42View
            PurchaseTypes.Purchase84Gems -> binding?.gems84View
            else -> return
        }
        if (matchingView != null) {
            matchingView.setPurchaseButtonText(price)
            matchingView.sku = sku
        }
    }

    private fun purchaseGems(identifier: String) {
        HabiticaPurchaseVerifier.addGift(identifier, giftedMember?.id)
        purchaseHandler?.purchaseGems(identifier)
    }
}
