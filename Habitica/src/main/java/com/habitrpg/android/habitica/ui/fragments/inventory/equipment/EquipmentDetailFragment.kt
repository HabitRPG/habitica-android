package com.habitrpg.android.habitica.ui.fragments.inventory.equipment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.extensions.observeOnce
import com.habitrpg.android.habitica.helpers.ReviewManager
import com.habitrpg.common.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.ui.adapter.inventory.EquipmentRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import com.habitrpg.common.habitica.helpers.EmptyItem
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import com.habitrpg.common.habitica.helpers.launchCatching
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class EquipmentDetailFragment :
    BaseMainFragment<FragmentRefreshRecyclerviewBinding>(),
    SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    override var binding: FragmentRefreshRecyclerviewBinding? = null
    @Inject
    lateinit var userViewModel: MainUserViewModel
    private lateinit var reviewManager: ReviewManager

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRefreshRecyclerviewBinding {
        return FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)
    }

    var type: String? = null
    var equippedGear: String? = null
    var isCostume: Boolean? = null

    private var adapter: EquipmentRecyclerViewAdapter = EquipmentRecyclerViewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        adapter.onEquip = {
            lifecycleScope.launchCatching {
                inventoryRepository.equipGear(it, isCostume ?: false)

                userViewModel.user.observeOnce(viewLifecycleOwner) { user ->
                    val parentActivity = mainActivity
                    val totalCheckIns = user?.loginIncentives
                    if (totalCheckIns != null && parentActivity != null) {
                        reviewManager.requestReview(parentActivity, totalCheckIns)
                    }
                }
            }
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showsBackButton = true
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = EquipmentDetailFragmentArgs.fromBundle(it)
            type = args.type
            isCostume = args.isCostume
            equippedGear = args.equippedGear
        }
        binding?.refreshLayout?.setOnRefreshListener(this)
        binding?.recyclerView?.onRefresh = { onRefresh() }
        binding?.recyclerView?.emptyItem = EmptyItem(
            getString(R.string.empty_title),
            getString(R.string.empty_equipment_description),
            null,
        ) {
            MainNavigationController.navigate(R.id.marketFragment)
        }

        this.adapter.equippedGear = this.equippedGear
        this.adapter.isCostume = this.isCostume
        this.adapter.type = this.type

        binding?.recyclerView?.adapter = this.adapter
        binding?.recyclerView?.layoutManager = LinearLayoutManager(mainActivity)
        binding?.recyclerView?.addItemDecoration(DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL))
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        type?.let { type ->
            lifecycleScope.launchCatching {
                inventoryRepository.getOwnedEquipment(type).collect { adapter.data = it }
            }
        }
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun onRefresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(true, true)
            binding?.refreshLayout?.isRefreshing = false
        }
    }
}
