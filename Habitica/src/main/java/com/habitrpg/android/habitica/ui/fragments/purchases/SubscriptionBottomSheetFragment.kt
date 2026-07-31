package com.habitrpg.android.habitica.ui.fragments.purchases

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.ProductDetails
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentBottomsheetSubscriptionBinding
import com.habitrpg.android.habitica.databinding.FragmentSubscriptionContentBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.helpers.PurchaseHandler
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
open class SubscriptionBottomSheetFragment : BottomSheetDialogFragment(), CommonSubscriptionFragment {
    private var binding: FragmentBottomsheetSubscriptionBinding? = null

    override val content: FragmentSubscriptionContentBinding?
        get() = binding?.content

    @Inject
    override lateinit var userRepository: UserRepository
    @Inject
    lateinit var appConfigManager: AppConfigManager
    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    override lateinit var purchaseHandler: PurchaseHandler

    override var selectedSubscriptionSku: ProductDetails? = null
    override var skus: List<ProductDetails> = emptyList()

    override var user: User? = null
    override var hasLoadedSubscriptionOptions: Boolean = false

    override fun getViewLifecycleOwner(): LifecycleOwner {
        return super.getViewLifecycleOwner()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentBottomsheetSubscriptionBinding.inflate(layoutInflater)
        return binding?.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupView(requireActivity())
        val content = content ?: return
        content.subscribeBenefitsFooter.visibility = View.GONE
        content.giftSegmentSubscribed.root.visibility = View.GONE
        content.giftSegmentUnsubscribed.root.visibility = View.GONE
        content.headerImageView.visibility = View.GONE
        content.seeMoreButton.visibility = View.VISIBLE

        content.seeMoreButton.setOnClickListener {
            MainNavigationController.navigate(
                R.id.gemPurchaseActivity,
                bundleOf(Pair("openSubscription", true)),
            )
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

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun purchaseSubscription() {
        selectedSubscriptionSku?.let { sku ->
            lifecycleScope.launchCatching {
                purchaseHandler.purchase(requireActivity(), sku)
                dismiss()
            }
        }
    }

    companion object {
        const val TAG = "SubscriptionBottomSheet"
    }
}
