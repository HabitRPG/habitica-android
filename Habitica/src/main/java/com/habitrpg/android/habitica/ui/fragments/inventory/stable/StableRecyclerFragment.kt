package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.helpers.AppConfigManager
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.ui.adapter.inventory.StableRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.android.habitica.ui.viewmodels.StableViewModel
import com.habitrpg.android.habitica.ui.viewmodels.StableViewModelFactory
import com.habitrpg.common.habitica.helpers.EmptyItem
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

class StableRecyclerFragment :
    BaseFragment<FragmentRefreshRecyclerviewBinding>(),
    SwipeRefreshLayout.OnRefreshListener {

    private val viewModel: StableViewModel by viewModels(factoryProducer = {
        StableViewModelFactory(context?.applicationContext as? Application, itemType)
    })

    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var userRepository: UserRepository
    @Inject
    lateinit var configManager: AppConfigManager
    @Inject
    lateinit var userViewModel: MainUserViewModel

    var adapter: StableRecyclerAdapter? = null
    var itemType: String? = null
    var itemTypeText: String? = null
    internal var layoutManager: androidx.recyclerview.widget.GridLayoutManager? = null

    override var binding: FragmentRefreshRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRefreshRecyclerviewBinding {
        return FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (savedInstanceState != null) {
            this.itemType = savedInstanceState.getString(ITEM_TYPE_KEY, "")
        }

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding?.recyclerView?.emptyItem = EmptyItem(
            getString(R.string.empty_items, itemTypeText ?: itemType)
        )
        binding?.refreshLayout?.setOnRefreshListener(this)

        layoutManager = androidx.recyclerview.widget.GridLayoutManager(activity, 4)
        layoutManager?.spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter?.getItemViewType(position) == 0 || adapter?.getItemViewType(position) == 1) {
                    layoutManager?.spanCount ?: 1
                } else {
                    1
                }
            }
        }

        binding?.recyclerView?.layoutManager = layoutManager
        activity?.let {
            binding?.recyclerView?.addItemDecoration(MarginDecoration(it, setOf(HEADER_VIEW_TYPE)))
        }

        adapter = binding?.recyclerView?.adapter as? StableRecyclerAdapter
        if (adapter == null) {
            adapter = StableRecyclerAdapter()
            adapter?.animalIngredientsRetriever = { animal, callback ->
                lifecycleScope.launch(ExceptionHandler.coroutine()) {
                    val egg = inventoryRepository.getItems(Egg::class.java, arrayOf(animal.animal)).firstOrNull()?.firstOrNull() as? Egg
                    val potion = inventoryRepository.getItems(HatchingPotion::class.java, arrayOf(animal.color)).firstOrNull()?.firstOrNull() as? HatchingPotion
                    callback(Pair(egg, potion))
                }
            }
            adapter?.itemType = this.itemType
            adapter?.shopSpriteSuffix = configManager.shopSpriteSuffix()
            binding?.recyclerView?.adapter = adapter
            binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()

            adapter?.let {
                it.onEquip = {
                    lifecycleScope.launchCatching {
                        inventoryRepository.equip(if (itemType == "pets") "pet" else "mount", it)
                    }
                }
            }
        }
        userViewModel.user.observe(viewLifecycleOwner) {
            adapter?.currentPet = it?.currentPet
            adapter?.currentMount = it?.currentMount
        }

        this.loadItems()
        view.post { setGridSpanCount(view.width) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ITEM_TYPE_KEY, this.itemType)
    }

    private fun setGridSpanCount(width: Int) {
        var spanCount = 0
        if (context != null && context?.resources != null) {
            val animalWidth = if (itemType == "pets") R.dimen.pet_width else R.dimen.mount_width
            val itemWidth: Float = context?.resources?.getDimension(animalWidth) ?: 0.toFloat()

            spanCount = (width / itemWidth).toInt()
        }
        if (spanCount == 0) {
            spanCount = 1
        }
        layoutManager?.spanCount = spanCount
    }

    private fun loadItems() {
        viewModel.items.observe(viewLifecycleOwner) {
            adapter?.setItemList(it)
        }
        viewModel.eggs.observe(viewLifecycleOwner) {
            adapter?.setEggs(it)
        }
        viewModel.ownedItems.observe(viewLifecycleOwner) {
            adapter?.setOwnedItems(it)
        }
        viewModel.mounts.observe(viewLifecycleOwner) {
            adapter?.setExistingMounts(it)
        }
        viewModel.ownedMounts.observe(viewLifecycleOwner) {
            adapter?.setOwnedMounts(it)
        }
    }

    companion object {
        private const val ITEM_TYPE_KEY = "CLASS_TYPE_KEY"
        private const val HEADER_VIEW_TYPE = 0
    }

    override fun onRefresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(true, true)
            binding?.refreshLayout?.isRefreshing = false
        }
    }
}
