package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentRefreshRecyclerviewBinding
import com.habitrpg.android.habitica.extensions.getTranslatedType
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.launchCatching
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.ui.adapter.inventory.MountDetailRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class MountDetailRecyclerFragment :
    BaseMainFragment<FragmentRefreshRecyclerviewBinding>(),
    SwipeRefreshLayout.OnRefreshListener {

    @Inject
    internal lateinit var inventoryRepository: InventoryRepository
    @Inject
    lateinit var userViewModel: MainUserViewModel

    var adapter: MountDetailRecyclerAdapter? = null
    var animalType: String? = null
    var animalGroup: String? = null
    var animalColor: String? = null
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
        this.usesTabLayout = false
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
        showsBackButton = true
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = MountDetailRecyclerFragmentArgs.fromBundle(it)
            if (args.group != "drop") {
                animalGroup = args.group
            }
            animalType = args.type
            animalColor = args.color
        }
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

        adapter = binding?.recyclerView?.adapter as? MountDetailRecyclerAdapter
        if (adapter == null) {
            adapter = MountDetailRecyclerAdapter()
            binding?.recyclerView?.adapter = adapter
            binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
            this.loadItems()

            adapter?.onEquip = {
                lifecycleScope.launchCatching {
                    val items = inventoryRepository.equip("mount", it)
                    adapter?.currentMount = items?.currentMount
                }
            }
        }
        userViewModel.user.observe(viewLifecycleOwner) { adapter?.currentMount = it?.currentMount }

        if (savedInstanceState != null) {
            this.animalType = savedInstanceState.getString(ANIMAL_TYPE_KEY, "")
        }

        view.post { setGridSpanCount(view.width) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ANIMAL_TYPE_KEY, this.animalType)
    }

    private fun setGridSpanCount(width: Int) {
        var spanCount = 0
        context?.resources?.let {
            resources
            val itemWidth: Float = resources.getDimension(R.dimen.mount_width)

            spanCount = (width / itemWidth).toInt()
        }
        if (spanCount == 0) {
            spanCount = 1
        }
        layoutManager?.spanCount = spanCount
        layoutManager?.requestLayout()
    }

    private fun loadItems() {
        if (animalType != null || animalGroup != null) {
            lifecycleScope.launch(ExceptionHandler.coroutine()) {
                val mounts = inventoryRepository.getMounts(animalType, animalGroup, animalColor).firstOrNull() ?: emptyList()
                inventoryRepository.getOwnedMounts().map { ownedMounts ->
                    val mountMap = mutableMapOf<String, OwnedMount>()
                    ownedMounts.forEach { mountMap[it.key ?: ""] = it }
                    return@map mountMap
                }.onEach { adapter?.setOwnedMounts(it) }
                    .collect { ownedMounts ->
                        val items = mutableListOf<Any>()
                        var lastMount: Mount? = null
                        var currentSection: StableSection? = null
                        for (mount in mounts) {
                            if (mount.type == "wacky" || mount.type == "special") continue
                            if (mount.type != lastMount?.type) {
                                currentSection = StableSection(mount.type, mount.getTranslatedType(context) ?: "")
                                items.add(currentSection)
                            }
                            currentSection?.let {
                                it.totalCount += 1
                                if (ownedMounts.containsKey(mount.key)) {
                                    it.ownedCount += 1
                                }
                            }
                            items.add(mount)
                            lastMount = mount
                        }
                        adapter?.setItemList(items)
                    }
            }
        }
    }

    companion object {
        private const val ANIMAL_TYPE_KEY = "ANIMAL_TYPE_KEY"
    }

    override fun onRefresh() {
        lifecycleScope.launch(ExceptionHandler.coroutine()) {
            userRepository.retrieveUser(false, true)
            binding?.refreshLayout?.isRefreshing = false
        }
    }
}
