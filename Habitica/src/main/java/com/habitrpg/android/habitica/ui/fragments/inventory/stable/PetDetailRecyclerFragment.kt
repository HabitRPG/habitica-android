package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.android.habitica.events.commands.FeedCommand
import com.habitrpg.android.habitica.extensions.getTranslatedType
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.ui.adapter.inventory.PetDetailRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.kotlin.Flowables
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class PetDetailRecyclerFragment : BaseMainFragment<FragmentRecyclerviewBinding>() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository


    var adapter: PetDetailRecyclerAdapter = PetDetailRecyclerAdapter()
    private var animalType: String? = null
    private var animalGroup: String? = null
    private var animalColor: String? = null
    internal var layoutManager: androidx.recyclerview.widget.GridLayoutManager? = null

    override var binding: FragmentRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRecyclerviewBinding {
        return FragmentRecyclerviewBinding.inflate(inflater, container, false)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
            val args = MountDetailRecyclerFragmentArgs.fromBundle(it)
            if (args.group != "drop") {
                animalGroup = args.group
            }
            animalType = args.type
            animalColor = args.color
        }

        layoutManager = androidx.recyclerview.widget.GridLayoutManager(getActivity(), 2)
        layoutManager?.spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter.getItemViewType(position) == 0 || adapter.getItemViewType(position) == 1) {
                    layoutManager?.spanCount ?: 1
                } else {
                    1
                }
            }
        }
        binding?.recyclerView?.layoutManager = layoutManager
        binding?.recyclerView?.addItemDecoration(MarginDecoration(getActivity()))
        adapter.animalIngredientsRetriever = { animal, callback ->
            Maybe.zip(
                    inventoryRepository.getItems(Egg::class.java, arrayOf(animal.animal)).firstElement(),
                    inventoryRepository.getItems(HatchingPotion::class.java, arrayOf(animal.color)).firstElement(), { eggs, potions ->
                Pair(eggs.first() as? Egg, potions.first() as? HatchingPotion)
            }
            ).subscribe({
                callback(it)
            }, RxErrorHandler.handleEmptyError())
        }
        binding?.recyclerView?.adapter = adapter
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()
        this.loadItems()

        compositeSubscription.add(adapter.getEquipFlowable()
                .flatMap { key -> inventoryRepository.equip(user, "pet", key) }
                .subscribe({ }, RxErrorHandler.handleEmptyError()))


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
            compositeSubscription.add(inventoryRepository.getOwnedMounts()
                    .map { ownedMounts ->
                        val mountMap = mutableMapOf<String, OwnedMount>()
                        ownedMounts.forEach { mountMap[it.key ?: ""] = it }
                        return@map mountMap
                    }
                    .subscribe({ adapter.setOwnedMounts(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(inventoryRepository.getOwnedItems(true).subscribe({ adapter.setOwnedItems(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(Flowables.combineLatest(inventoryRepository.getPets(animalType, animalGroup, animalColor), inventoryRepository.getOwnedPets()
                    .map { ownedPets ->
                        val petMap = mutableMapOf<String, OwnedPet>()
                        ownedPets.forEach { petMap[it.key ?: ""] = it }
                        return@map petMap
                    }.doOnNext {
                        adapter.setOwnedPets(it)
                    }).map {
                        val items = mutableListOf<Any>()
                        var lastPet: Pet? = null
                        var currentSection: StableSection? = null
                        for (pet in it.first) {
                            if (pet.type == "wacky" || pet.type == "special") continue
                            if (pet.type != lastPet?.type) {
                                currentSection = StableSection(pet.type, pet.getTranslatedType(context) ?: "")
                                items.add(currentSection)
                            }
                            currentSection?.let {section ->
                                section.totalCount += 1
                                if (it.second.containsKey(pet.key)) {
                                    section.ownedCount += 1
                                }
                            }
                            items.add(pet)
                            lastPet = pet
                        }
                        items
                    }.subscribe({ adapter.setItemList(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(inventoryRepository.getMounts(animalType, animalGroup, animalColor).subscribe({ adapter.setExistingMounts(it) }, RxErrorHandler.handleEmptyError()))
        }
    }


    @Subscribe
    fun showFeedingDialog(event: FeedCommand) {
        if (event.usingPet == null || event.usingFood == null) {
            val fragment = ItemRecyclerFragment()
            fragment.feedingPet = event.usingPet
            fragment.isFeeding = true
            fragment.isHatching = false
            fragment.itemType = "food"
            fragment.itemTypeText = getString(R.string.food)
            parentFragmentManager.let { fragment.show(it, "feedDialog") }
        }
    }

    companion object {
        private const val ANIMAL_TYPE_KEY = "ANIMAL_TYPE_KEY"
    }
}
