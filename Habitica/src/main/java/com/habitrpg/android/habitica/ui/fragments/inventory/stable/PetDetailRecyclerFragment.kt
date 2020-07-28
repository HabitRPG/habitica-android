package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.events.commands.FeedCommand
import com.habitrpg.android.habitica.extensions.getTranslatedType
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedObject
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.ui.adapter.inventory.PetDetailRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import org.greenrobot.eventbus.Subscribe
import java.util.ArrayList
import javax.inject.Inject

class PetDetailRecyclerFragment : BaseMainFragment() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    private val recyclerView: androidx.recyclerview.widget.RecyclerView by bindView(R.id.recyclerView)

    var adapter: PetDetailRecyclerAdapter = PetDetailRecyclerAdapter()
    var animalType: String? = null
    var animalGroup: String? = null
    var animalColor: String? = null
    internal var layoutManager: androidx.recyclerview.widget.GridLayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.usesTabLayout = false
        super.onCreateView(inflater, container, savedInstanceState)
        if (savedInstanceState != null) {
            this.animalType = savedInstanceState.getString(ANIMAL_TYPE_KEY, "")
        }

        return inflater.inflate(R.layout.fragment_recyclerview, container, false)
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


        resetViews()

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
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(MarginDecoration(getActivity()))
        adapter.animalIngredientsRetriever = {
            val egg = inventoryRepository.getItems(Egg::class.java, arrayOf(it.animal)).firstElement().blockingGet().firstOrNull()
            val potion = inventoryRepository.getItems(HatchingPotion::class.java, arrayOf(it.color)).firstElement().blockingGet().firstOrNull()
            Pair(egg as? Egg, potion as? HatchingPotion)
        }
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = SafeDefaultItemAnimator()
        this.loadItems()

        compositeSubscription.add(adapter.getEquipFlowable()
                .flatMap<Items> { key -> inventoryRepository.equip(user, "pet", key) }
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))


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
                    .subscribe(Consumer { adapter.setOwnedMounts(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(inventoryRepository.getOwnedItems(true).subscribe(Consumer { adapter.setOwnedItems(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(inventoryRepository.getPets(animalType, animalGroup, animalColor).zipWith(inventoryRepository.getOwnedPets()
                    .map { ownedPets ->
                        val petMap = mutableMapOf<String, OwnedPet>()
                        ownedPets.forEach { petMap[it.key ?: ""] = it }
                        return@map petMap
                    }.doOnNext {
                        adapter.setOwnedPets(it)
                    }, BiFunction<RealmResults<out Pet>, Map<String, OwnedObject>, List<Any>> { unsortedAnimals, ownedAnimals ->
                        val items = mutableListOf<Any>()
                        var lastPet: Pet? = null
                        var currentSection: StableSection? = null
                        for (pet in unsortedAnimals) {
                            if (pet.type == "wacky" || pet.type == "special") continue
                            if (pet.type != lastPet?.type) {
                                currentSection = StableSection(pet.type, pet.getTranslatedType(context))
                                items.add(currentSection)
                            }
                            currentSection?.let {
                                it.totalCount += 1
                                if (ownedAnimals.containsKey(pet.key)) {
                                    it.ownedCount += 1
                                }
                            }
                            items.add(pet)
                            lastPet = pet
                        }
                        items
                    })
                    .subscribe(Consumer { adapter.setItemList(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(inventoryRepository.getMounts(animalType, animalGroup, animalColor).subscribe(Consumer<RealmResults<Mount>> { adapter.setExistingMounts(it) }, RxErrorHandler.handleEmptyError()))
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
            fragmentManager?.let { fragment.show(it, "feedDialog") }
        }
    }

    companion object {
        private const val ANIMAL_TYPE_KEY = "ANIMAL_TYPE_KEY"
    }
}
