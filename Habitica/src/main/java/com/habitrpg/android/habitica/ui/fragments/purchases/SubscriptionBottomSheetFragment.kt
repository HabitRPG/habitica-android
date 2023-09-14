package com.habitrpg.android.habitica.ui.fragments.purchases

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentBottomsheetSubscriptionBinding
import com.habitrpg.android.habitica.helpers.Analytics
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.helpers.PurchaseTypes
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.views.subscriptions.SubscriptionOptionView
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
open class SubscriptionBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentBottomsheetSubscriptionBinding? = null
    val binding get() = _binding!!

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var appConfigManager: AppConfigManager

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var purchaseHandler: PurchaseHandler

    private var selectedSubscriptionSku: ProductDetails? = null
    private var skus: List<ProductDetails> = emptyList()

    private var user: User? = null
    private var hasLoadedSubscriptionOptions: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBottomsheetSubscriptionBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.subscriptionOptions.visibility = View.GONE
        binding.seeMoreOptions.setOnClickListener {
            dismiss()
            MainNavigationController.navigate(R.id.subscriptionPurchaseActivity)
        }
        binding.subscribeButton.setOnClickListener { purchaseSubscription() }

        lifecycleScope.launchCatching {
            userRepository.getUser().collect { user ->
                user?.let { setUser(it) }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val bottomSheetDialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        bottomSheetDialog.setOnShowListener { dialog: DialogInterface ->
            val notificationDialog = dialog as BottomSheetDialog
            notificationDialog.behavior.state = BottomSheetBehavior.STATE_HALF_EXPANDED
            notificationDialog.behavior.isDraggable = true
        }
        return bottomSheetDialog
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launchCatching {
            purchaseHandler.queryPurchases()
        }
        refresh()
        loadInventory()
    }

    private fun refresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(false, true)
        }
    }

    private fun loadInventory() {
        CoroutineScope(Dispatchers.IO).launchCatching {
            val subscriptions = purchaseHandler.getAllSubscriptionProducts()
            skus = subscriptions
            withContext(Dispatchers.Main) {
                for (sku in subscriptions) {
                    updateButtonLabel(sku, sku.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "")
                }
                subscriptions.minByOrNull { it.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.priceAmountMicros ?: 0 }?.let { selectSubscription(it) }
                hasLoadedSubscriptionOptions = true
                updateSubscriptionInfo()
            }
        }
    }

    private fun updateButtonLabel(sku: ProductDetails, price: String) {
        val matchingView = buttonForSku(sku)
        if (matchingView != null) {
            matchingView.setPriceText(price)
            matchingView.sku = sku.productId
            matchingView.setOnPurchaseClickListener {
                selectSubscription(sku)
            }
        }
    }

    private fun selectSubscription(sku: ProductDetails) {
        if (this.selectedSubscriptionSku != null) {
            val oldButton = buttonForSku(this.selectedSubscriptionSku)
            oldButton?.setIsSelected(false)
        }
        this.selectedSubscriptionSku = sku
        val subscriptionOptionButton = buttonForSku(this.selectedSubscriptionSku)
        subscriptionOptionButton?.setIsSelected(true)
        binding.subscribeButton.isEnabled = true
    }

    private fun buttonForSku(sku: ProductDetails?): SubscriptionOptionView? {
        return buttonForSku(sku?.productId)
    }

    private fun buttonForSku(sku: String?): SubscriptionOptionView? {
        return when (sku) {
            PurchaseTypes.Subscription1Month -> binding.subscription1month
            PurchaseTypes.Subscription12Month -> binding.subscription12month
            else -> null
        }
    }

    private fun purchaseSubscription() {
        selectedSubscriptionSku?.let { sku ->
            activity?.let {
                purchaseHandler.purchase(it, sku)
                dismiss()
            }
        }
    }

    fun setUser(newUser: User) {
        user = newUser
        this.updateSubscriptionInfo()
        checkIfNeedsCancellation()
    }

    private fun updateSubscriptionInfo() {
        if (hasLoadedSubscriptionOptions) {
            binding.subscriptionOptions.visibility = View.VISIBLE
            binding.loadingIndicator.visibility = View.GONE
        }
        if (user != null) {
            binding.loadingIndicator.visibility = View.GONE
        }
    }

    private fun checkIfNeedsCancellation() {
        CoroutineScope(Dispatchers.IO).launch(ExceptionHandler.coroutine()) {
            val newestSubscription = purchaseHandler.checkForSubscription()
            if (user?.purchased?.plan?.paymentMethod == "Google" &&
                user?.purchased?.plan?.isActive == true &&
                user?.purchased?.plan?.dateTerminated == null &&
                (newestSubscription?.isAutoRenewing != true)
            ) {
                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                    purchaseHandler.cancelSubscription()
                }
            }
        }
    }

    companion object {
        const val TAG = "SubscriptionBottomSheet"
    }
}
