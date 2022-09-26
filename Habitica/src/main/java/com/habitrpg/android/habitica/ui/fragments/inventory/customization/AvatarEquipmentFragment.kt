package com.habitrpg.android.habitica.ui.fragments.inventory.customization

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.models.responses.UnlockResponse
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.CustomizationEquipmentRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.launch
import javax.inject.Inject

class AvatarEquipmentFragment :
    BaseMainFragment<FragmentRefreshRecyclerviewBinding>(),
    SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var userViewModel: MainUserViewModel

    override var binding: FragmentRefreshRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRefreshRecyclerviewBinding {
        return FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)
    }

    var type: String? = null
    var category: String? = null
    private var activeEquipment: String? = null

    internal var adapter: CustomizationEquipmentRecyclerViewAdapter = CustomizationEquipmentRecyclerViewAdapter()
    internal var layoutManager: GridLayoutManager = GridLayoutManager(activity, 2)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        showsBackButton = true
        compositeSubscription.add(
            adapter.getSelectCustomizationEvents()
                .flatMap { equipment ->
                    val key = (if (equipment.key?.isNotBlank() != true) activeEquipment else equipment.key) ?: ""
                    inventoryRepository.equip(if (userViewModel.user.value?.preferences?.costume == true) "costume" else "equipped", key)
                }
                .subscribe({ }, ExceptionHandler.rx())
        )
        compositeSubscription.add(
            adapter.getUnlockCustomizationEvents()
                .flatMap<UnlockResponse> {
                    Flowable.empty()
                }
                .subscribe({ }, ExceptionHandler.rx())
        )
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showsBackButton = true
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val args = AvatarEquipmentFragmentArgs.fromBundle(it)
            type = args.type
            if (args.category.isNotEmpty()) {
                category = args.category
            }
        }
        binding?.refreshLayout?.setOnRefreshListener(this)
        setGridSpanCount(view.width)
        val layoutManager = GridLayoutManager(activity, 4)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == 0) {
                    layoutManager.spanCount
                } else {
                    1
                }
            }
        }
        binding?.recyclerView?.layoutManager = layoutManager
        binding?.recyclerView?.addItemDecoration(MarginDecoration(context))

        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
        this.loadEquipment()

        userViewModel.user.observe(viewLifecycleOwner) { updateUser(it) }
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }

    private fun loadEquipment() {
        val type = this.type ?: return
        compositeSubscription.add(
            inventoryRepository.getEquipmentType(type, category ?: "").subscribe(
                {
                    adapter.setEquipment(it)
                },
                ExceptionHandler.rx()
            )
        )
    }

    private fun setGridSpanCount(width: Int) {
        val itemWidth = context?.resources?.getDimension(R.dimen.customization_width) ?: 0F
        var spanCount = (width / itemWidth).toInt()
        if (spanCount == 0) {
            spanCount = 1
        }
        layoutManager.spanCount = spanCount
    }

    fun updateUser(user: User?) {
        this.updateActiveCustomization(user)
        this.adapter.gemBalance = user?.gemCount ?: 0
        adapter.notifyDataSetChanged()
    }

    private fun updateActiveCustomization(user: User?) {
        if (this.type == null || user?.preferences == null) {
            return
        }
        val outfit = if (user.preferences?.costume == true) user.items?.gear?.costume else user.items?.gear?.equipped
        val activeEquipment = when (this.type) {
            "headAccessory" -> outfit?.headAccessory
            "back" -> outfit?.back
            "eyewear" -> outfit?.eyeWear
            else -> ""
        }
        if (activeEquipment != null) {
            this.activeEquipment = activeEquipment
            this.adapter.activeEquipment = activeEquipment
        }
    }

    override fun onRefresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(true, true)
            binding?.refreshLayout?.isRefreshing = false
        }
    }
}
