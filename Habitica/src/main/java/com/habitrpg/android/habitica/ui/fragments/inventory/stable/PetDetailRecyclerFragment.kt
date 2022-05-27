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
import com.habitrpg.android.habitica.extensions.subscribeWithErrorHandler
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.interactors.FeedPetUseCase
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.ui.adapter.inventory.PetDetailRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemDialogFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.viewmodels.MainUserViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class PetDetailRecyclerFragment :
    BaseMainFragment<FragmentRefreshRecyclerviewBinding>(),
    SwipeRefreshLayout.OnRefreshListener {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    @Inject
    lateinit var feedPetUseCase: FeedPetUseCase

    @Inject
    lateinit var userViewModel: MainUserViewModel

    val adapter: PetDetailRecyclerAdapter = PetDetailRecyclerAdapter()
    private var animalType: String? = null
    private var animalGroup: String? = null
    private var animalColor: String? = null
    internal var layoutManager: androidx.recyclerview.widget.GridLayoutManager? = null

    override var binding: FragmentRefreshRecyclerviewBinding? = null

    override fun createBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentRefreshRecyclerviewBinding {
        return FragmentRefreshRecyclerviewBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        this.usesTabLayout = false
        if (savedInstanceState != null) {
            this.animalType = savedInstanceState.getString(ANIMAL_TYPE_KEY, "")
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
        showsBackButton = true
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = PetDetailRecyclerFragmentArgs.fromBundle(it)
            if (args.group != "drop") {
                animalGroup = args.group
            }
            animalType = args.type
            animalColor = args.color
        }
        binding?.refreshLayout?.setOnRefreshListener(this)

        layoutManager = androidx.recyclerview.widget.GridLayoutManager(getActivity(), 2)
        layoutManager?.spanSizeLookup =
            object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (adapter.getItemViewType(position) == 0 || adapter.getItemViewType(
                            position
                        ) == 1
                    ) {
                        layoutManager?.spanCount ?: 1
                    } else {
                        1
                    }
                }
            }
        binding?.recyclerView?.layoutManager = layoutManager
        adapter.animalIngredientsRetriever = { animal, callback ->
            lifecycleScope.launch {
                val egg = inventoryRepository.getItems(Egg::class.java, arrayOf(animal.animal))
                    .firstOrNull()?.firstOrNull() as? Egg
                val potion =
                    inventoryRepository.getItems(HatchingPotion::class.java, arrayOf(animal.color))
                        .firstOrNull()?.firstOrNull() as? HatchingPotion
                callback(Pair(egg, potion))
            }
        }
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
        this.loadItems()

        compositeSubscription.add(
            adapter.getEquipFlowable()
                .flatMap { key -> inventoryRepository.equip("pet", key) }
                .subscribe(
                    {
                        adapter.currentPet = it.currentPet
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
        userViewModel.user.observe(viewLifecycleOwner) { adapter.currentPet = it?.currentPet }
        compositeSubscription.add(adapter.feedFlowable.subscribe({
            showFeedingDialog(
                it.first,
                it.second
            )
        }, RxErrorHandler.handleEmptyError()))

        view.post { setGridSpanCount(view.width) }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = animalType
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ANIMAL_TYPE_KEY, this.animalType)
    }

    private fun setGridSpanCount(width: Int) {
        var spanCount = 0
        if (context != null && context?.resources != null) {
            val animalWidth = R.dimen.pet_width
            val itemWidth: Float = context?.resources?.getDimension(animalWidth) ?: 0.toFloat()

            spanCount = (width / itemWidth).toInt()
        }
        if (spanCount == 0) {
            spanCount = 1
        }
        layoutManager?.spanCount = spanCount
    }

    private fun loadItems() {
        if (animalType?.isNotEmpty() == true || animalGroup?.isNotEmpty() == true) {
            lifecycleScope.launch {
                inventoryRepository.getOwnedMounts()
                    .map { ownedMounts ->
                        val mountMap = mutableMapOf<String, OwnedMount>()
                        ownedMounts.forEach { mountMap[it.key ?: ""] = it }
                        return@map mountMap
                    }.collect { adapter.setOwnedMounts(it) }
            }
            compositeSubscription.add(
                inventoryRepository.getOwnedItems(true)
                    .subscribe({ adapter.setOwnedItems(it) }, RxErrorHandler.handleEmptyError())
            )
            lifecycleScope.launch {
                val mounts = inventoryRepository.getMounts(
                    animalType,
                    animalGroup,
                    animalColor
                ).firstOrNull() ?: emptyList()
                adapter.setExistingMounts(mounts)
                val pets = inventoryRepository.getPets(animalType, animalGroup, animalColor)
                    .firstOrNull() ?: emptyList()
                inventoryRepository.getOwnedPets()
                    .map { ownedPets ->
                        val petMap = mutableMapOf<String, OwnedPet>()
                        ownedPets.forEach { petMap[it.key ?: ""] = it }
                        return@map petMap
                    }.onEach { adapter.setOwnedPets(it) }
                    .collect { ownedPets ->
                        val items = mutableListOf<Any>()
                        var lastPet: Pet? = null
                        var currentSection: StableSection? = null
                        for (pet in pets) {
                            if (pet.type == "wacky" || pet.type == "special") continue
                            if (pet.type != lastPet?.type) {
                                currentSection =
                                    StableSection(
                                        pet.type,
                                        pet.getTranslatedType(context) ?: ""
                                    )
                                items.add(currentSection)
                            }
                            currentSection?.let { section ->
                                section.totalCount += 1
                                if (ownedPets.containsKey(pet.key)) {
                                    section.ownedCount += 1
                                }
                            }
                            items.add(pet)
                            lastPet = pet
                        }
                        adapter.setItemList(items)
                    }

            }
        }
    }

    private fun showFeedingDialog(pet: Pet, food: Food?) {
        if (food != null) {
            val context = activity ?: context ?: return
            compositeSubscription.add(
                feedPetUseCase.observable(
                    FeedPetUseCase.RequestValues(
                        pet, food,
                        context
                    )
                ).subscribeWithErrorHandler {}
            )
            return
        }
        val fragment = ItemDialogFragment()
        fragment.feedingPet = pet
        fragment.isFeeding = true
        fragment.isHatching = false
        fragment.itemType = "food"
        fragment.itemTypeText = getString(R.string.food)
        fragment.parentSubscription = compositeSubscription
        parentFragmentManager.let { fragment.show(it, "feedDialog") }
    }

    companion object {
        private const val ANIMAL_TYPE_KEY = "ANIMAL_TYPE_KEY"
    }

    override fun onRefresh() {
        compositeSubscription.add(
            userRepository.retrieveUser(false, true).subscribe(
                {
                    binding?.refreshLayout?.isRefreshing = false
                },
                RxErrorHandler.handleEmptyError()
            )
        )
    }
}
