package com.habitrpg.android.habitica.ui.fragments.purchases

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.databinding.FragmentGiftGemPurchaseBinding
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.HabiticaProduct
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.GemPurchaseOptionsView
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class GiftPurchaseGemsFragment : BaseFragment<FragmentGiftGemPurchaseBinding>() {
    @Inject
    lateinit var socialRepository: SocialRepository

    override var binding: FragmentGiftGemPurchaseBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentGiftGemPurchaseBinding {
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

    fun setupCheckout() {
        viewLifecycleOwner.lifecycleScope.launch(ExceptionHandler.coroutine()) {
            val skus = purchaseHandler?.loadGemProducts()
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

    private fun updateButtonLabel(
        sku: ProductDetails,
        price: String
    ) {
        val matchingView: GemPurchaseOptionsView? =
            when (HabiticaProduct.forSku(sku.productId)) {
                HabiticaProduct.PURCHASE_4_GEMS -> binding?.gems4View
                HabiticaProduct.PURCHASE_21_GEMS -> binding?.gems21View
                HabiticaProduct.PURCHASE_42_GEMS -> binding?.gems42View
                HabiticaProduct.PURCHASE_84_GEMS -> binding?.gems84View
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
            lifecycleScope.launchCatching {
                purchaseHandler?.purchase(requireActivity(), sku, it, giftedMember?.username) }
        }
    }
}
