package com.habitrpg.android.habitica.ui.fragments.inventory.shops

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.models.shops.Shop
import com.habitrpg.android.habitica.models.shops.ShopCategory
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.inventory.ShopRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.EventOutcomeSubscriptionBottomSheetFragment
import com.habitrpg.android.habitica.ui.fragments.purchases.SubscriptionBottomSheetFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.ShopGridSpacingDecoration
import com.habitrpg.android.habitica.ui.viewmodels.inventory.shops.ShopViewModel
import com.habitrpg.android.habitica.ui.views.CurrencyText
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaProgressDialog
import com.habitrpg.android.habitica.ui.views.insufficientCurrency.InsufficientGemsDialog
import com.habitrpg.android.habitica.ui.views.shops.PurchaseDialog
import com.habitrpg.common.habitica.helpers.RecyclerViewState
import com.habitrpg.common.habitica.helpers.launchCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class ShopFragment<VM: ShopViewModel> : BaseMainFragment<FragmentRefreshRecyclerviewBinding>() {
    internal abstract val viewModel: VM

    internal val currencyView: ComposeView by lazy {
        return@lazy ComposeView(requireContext())
    }

    var adapter: ShopRecyclerAdapter? = null
    var shopIdentifier: String? = null
    var shop: Shop? = null
    internal val hourglasses = mutableStateOf<Double?>(null)
    private val gems = mutableStateOf<Double?>(null)
    private val gold = mutableStateOf<Double?>(null)

    private var layoutManager: GridLayoutManager? = null

    private var gearCategories: List<ShopCategory>? = null

    override var binding: FragmentRefreshRecyclerviewBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
    ): FragmentRefreshRecyclerviewBinding = FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        viewModel.shopIdentifier = shopIdentifier
        this.hidesToolbar = true
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroyView() {
        toolbarAccessoryContainer?.removeView(currencyView)
        super.onDestroyView()
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        initializeCurrencyViews()
        toolbarAccessoryContainer?.addView(currencyView)
        binding?.recyclerView?.setBackgroundResource(R.color.content_background)
        binding?.recyclerView?.onRefresh = {
            loadShopInventory()
        }
        binding?.refreshLayout?.setOnRefreshListener {
            loadShopInventory()
        }
        adapter = binding?.recyclerView?.adapter as? ShopRecyclerAdapter
        if (adapter == null) {
            adapter = ShopRecyclerAdapter()
            adapter?.onNeedsRefresh = {
                loadShopInventory()
                if (Shop.MARKET == shopIdentifier) {
                    loadMarketGear()
                }
            }
            adapter?.onShowPurchaseDialog = { item, isPinned ->
                if (item.key == "gem" && !viewModel.isUserSubscribed) {
                    val subscriptionBottomSheet =
                        EventOutcomeSubscriptionBottomSheetFragment().apply {
                            eventType = EventOutcomeSubscriptionBottomSheetFragment.EVENT_GEMS_FOR_GOLD
                        }
                    activity?.let { activity ->
                        subscriptionBottomSheet.show(activity.supportFragmentManager, SubscriptionBottomSheetFragment.TAG)
                    }
                } else {
                    val dialog =
                        PurchaseDialog(
                            requireContext(),
                            item,
                            mainActivity,
                        )
                    dialog.shopIdentifier = shopIdentifier
                    dialog.isPinned = isPinned
                    dialog.onShopNeedsRefresh = {
                        loadShopInventory()
                        if (Shop.MARKET == shopIdentifier) {
                            loadMarketGear()
                        }
                    }
                    dialog.show()
                }
            }

            adapter?.context = context
            adapter?.mainActivity = mainActivity
            binding?.recyclerView?.adapter = adapter
            binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
            adapter?.shopSpriteSuffix = viewModel.shopSpriteSuffix
            val spacingPx = resources.getDimensionPixelSize(R.dimen.shop_item_spacing)
            binding?.recyclerView?.addItemDecoration(ShopGridSpacingDecoration(spacingPx))
            adapter?.changeClassEvents = {
                viewModel.userViewModel.user.value?.let { user -> showClassChangeDialog(it, user) }
            }
            adapter?.emptySectionClickedEvents = {
                viewModel.onEmptySectionTapped(it)
            }

            viewLifecycleOwner.lifecycleScope.launchCatching {
                viewModel.armoireItem.collect {
                    adapter?.armoireItem = it
                }
            }
            viewLifecycleOwner.lifecycleScope.launchCatching {
                viewModel.armoireCount.collect {
                    adapter?.armoireCount = it
                }
            }
        }

        if (binding?.recyclerView?.layoutManager == null) {
            layoutManager = GridLayoutManager(context, 2)
            layoutManager?.spanSizeLookup =
                object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int =
                        if ((adapter?.getItemViewType(position) ?: 0) < 5) {
                            layoutManager?.spanCount ?: 1
                        } else {
                            1
                        }
                }
            binding?.recyclerView?.layoutManager = layoutManager
        }

        if (savedInstanceState != null) {
            this.shopIdentifier = savedInstanceState.getString(SHOP_IDENTIFIER_KEY, "")
        }


        if (shop != null) {
            adapter?.setShop(shop)
        }

        val categories = gearCategories
        if (categories != null) {
            adapter?.gearCategories = categories
        } else {
            if (Shop.MARKET == shopIdentifier) {
                loadMarketGear()
            }
        }

        viewModel.userViewModel.user.observe(viewLifecycleOwner) {
            if (adapter?.selectedGearCategory == "") {
                adapter?.selectedGearCategory = it?.stats?.habitClass ?: ""
            }
            adapter?.user = it
            hourglasses.value = it?.hourglassCount?.toDouble() ?: 0.0
            gems.value = it?.gemCount?.toDouble() ?: 0.0
            gold.value = it?.stats?.gp ?: 0.0
        }

        view.post { setGridSpanCount(view.width) }

        viewLifecycleOwner.lifecycleScope.launchCatching {
            viewModel.ownedItems
                .collect { adapter?.setOwnedItems(it) }
        }

        viewLifecycleOwner.lifecycleScope.launchCatching {
            viewModel.inAppRewards
                .collect { adapter?.setPinnedItemKeys(it) }
        }
    }

    open fun initializeCurrencyViews() {
        currencyView.setContent {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                gems.value?.let { CurrencyText(currency = "gems", value = it) }
                gold.value?.let { CurrencyText(currency = "gold", value = it) }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (shop == null) {
            loadShopInventory()
        }
    }

    private fun loadShopInventory() {
        viewLifecycleOwner.lifecycleScope.launchCatching({
            binding?.recyclerView?.state = RecyclerViewState.FAILED
        }) {
            val newShop = viewModel.retrieveShopInventory(requireContext())
            shop = newShop
            adapter?.shopIdentifier = shopIdentifier
            adapter?.setShop(newShop)
            binding?.refreshLayout?.isRefreshing = false
        }
    }

    private fun loadMarketGear() {
        viewLifecycleOwner.lifecycleScope.launchCatching {
            val categories = viewModel.retrieveMarketGear()
            gearCategories = categories
            adapter?.gearCategories = categories ?: listOf()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SHOP_IDENTIFIER_KEY, this.shopIdentifier)
    }

    private fun setGridSpanCount(width: Int) {
        var spanCount = 0
        context?.let { context ->
            val itemWidth: Float = context.resources.getDimension(R.dimen.shop_column_width)

            spanCount = (width / itemWidth).toInt()
        }
        if (spanCount == 0) {
            spanCount = 1
        }
        layoutManager?.spanCount = spanCount
        layoutManager?.requestLayout()
    }

    private fun displayClassChanged(selectedClass: String) {
        context?.let { context ->
            val alert = HabiticaAlertDialog(context)
            alert.setMessage(getString(R.string.class_changed_description, selectedClass))
            alert.addButton(getString(R.string.complete_tutorial), true) { _, _ -> alert.dismiss() }
            alert.show()
        }
    }

    private fun showClassChangeDialog(classIdentifier: String, user: User) {
        lifecycleScope.launchCatching {
            context?.let { context ->
                if (user.gemCount <= 2) {
                    val dialog = mainActivity?.let { InsufficientGemsDialog(it, 3) }
                    dialog?.show()
                    return@launchCatching
                }
                if (user.flags?.classSelected == true && user.preferences?.disableClasses == false) {
                    val alert = HabiticaAlertDialog(context)
                    alert.setTitle(getString(R.string.change_class_selected_confirmation, classIdentifier))
                    alert.setMessage(getString(R.string.change_class_equipment_warning))
                    alert.addButton(R.string.choose_class, true) { _, _ ->
                        val dialog =
                            HabiticaProgressDialog.show(
                                requireActivity(),
                                getString(R.string.changing_class_progress),
                                300,
                            )
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                            userRepository.changeClass(classIdentifier)
                            dialog.dismiss()
                            displayClassChanged(classIdentifier)
                            loadMarketGear()
                        }
                    }
                    alert.addButton(R.string.close, false)
                    alert.show()
                } else {
                    val alert = HabiticaAlertDialog(context)
                    alert.setTitle(getString(R.string.class_confirmation, classIdentifier))
                    alert.addButton(R.string.choose_class, true) { _, _ ->
                        val dialog =
                            HabiticaProgressDialog.show(
                                requireActivity(),
                                getString(R.string.changing_class_progress),
                                300,
                            )
                        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                            userRepository.changeClass(classIdentifier)
                            dialog.dismiss()
                            displayClassChanged(classIdentifier)
                            loadMarketGear()
                        }
                    }
                    alert.addButton(R.string.close, false)
                    alert.show()
                }
            }
        }
    }

    companion object {
        private const val SHOP_IDENTIFIER_KEY = "SHOP_IDENTIFIER_KEY"
    }
}
