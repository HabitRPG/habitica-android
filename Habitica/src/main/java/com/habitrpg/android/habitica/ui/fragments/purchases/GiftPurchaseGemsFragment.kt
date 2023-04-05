package com.habitrpg.android.habitica.ui.fragments.purchases

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.billingclient.api.ProductDetails
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentGiftGemPurchaseBinding
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.GemPurchaseOptionsView
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GiftPurchaseGemsFragment : BaseFragment<FragmentGiftGemPurchaseBinding>() {

    @Inject
    lateinit var socialRepository: SocialRepository

    override var binding: FragmentGiftGemPurchaseBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentGiftGemPurchaseBinding {
        return FragmentGiftGemPurchaseBinding.inflate(inflater, container, false)
    }

    var giftedMember: Member? = null
        @SuppressLint("SetTextI18n")
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    fun setupCheckout() {
        CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
            val skus = purchaseHandler?.getAllGemSKUs()
            withContext(Dispatchers.Main) {
                for (sku in skus ?: emptyList()) {
                    updateButtonLabel(sku, sku.oneTimePurchaseOfferDetails?.formattedPrice ?: "")
                }
            }
        }
    }

    fun setPurchaseHandler(handler: PurchaseHandler?) {
        this.purchaseHandler = handler
    }

    private fun updateButtonLabel(sku: ProductDetails, price: String) {
        val matchingView: GemPurchaseOptionsView? = when (sku.productId) {
            PurchaseTypes.Purchase4Gems -> binding?.gems4View
            PurchaseTypes.Purchase21Gems -> binding?.gems21View
            PurchaseTypes.Purchase42Gems -> binding?.gems42View
            PurchaseTypes.Purchase84Gems -> binding?.gems84View
            else -> return
        }
        if (matchingView != null) {
            matchingView.setPurchaseButtonText(price)
            matchingView.setOnPurchaseClickListener {
                purchaseGems(sku)
            }
            matchingView.sku = sku
        }
    }

    private fun purchaseGems(sku: ProductDetails) {
        giftedMember?.id?.let {
            activity?.let { it1 -> purchaseHandler?.purchase(it1, sku, it, giftedMember?.username) }
        }
    }
}
