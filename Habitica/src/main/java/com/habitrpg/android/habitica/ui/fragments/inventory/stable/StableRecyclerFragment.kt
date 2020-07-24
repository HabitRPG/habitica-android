package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.extensions.getTranslatedType
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.models.user.*
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.adapter.inventory.StableRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment
import com.habitrpg.android.habitica.ui.helpers.*
import io.reactivex.Maybe
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import java.util.*
import javax.inject.Inject

class StableRecyclerFragment : BaseFragment() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    private val recyclerView: RecyclerViewEmptySupport? by bindView(R.id.recyclerView)
    private val emptyView: TextView? by bindView(R.id.emptyView)
    var adapter: StableRecyclerAdapter? = null
    var itemType: String? = null
    var itemTypeText: String? = null
    var user: User? = null
    internal var layoutManager: androidx.recyclerview.widget.GridLayoutManager? = null
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)

        if (savedInstanceState != null) {
            this.itemType = savedInstanceState.getString(ITEM_TYPE_KEY, "")
        }

        return container?.inflate(R.layout.fragment_recyclerview_stable)
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

        resetViews()
        
        recyclerView?.setEmptyView(emptyView)
        emptyView?.text = getString(R.string.empty_items, itemTypeText)

        layoutManager = androidx.recyclerview.widget.GridLayoutManager(activity, 2)
        layoutManager?.spanSizeLookup = object : androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter?.getItemViewType(position) == 0 || adapter?.getItemViewType(position) == 1) {
                    layoutManager?.spanCount ?: 1
                } else {
                    1
                }
            }
        }

        recyclerView?.layoutManager = layoutManager
        activity?.let {
            recyclerView?.addItemDecoration(MarginDecoration(it, setOf(HEADER_VIEW_TYPE)))
        }

        adapter = recyclerView?.adapter as? StableRecyclerAdapter
        if (adapter == null) {
            adapter = StableRecyclerAdapter()
            adapter?.animalIngredientsRetriever = {
                val egg = inventoryRepository.getItems(Egg::class.java, arrayOf(it.animal), null).firstElement().blockingGet().firstOrNull()
                val potion = inventoryRepository.getItems(HatchingPotion::class.java, arrayOf(it.color), null).firstElement().blockingGet().firstOrNull()
                Pair(egg as? Egg, potion as? HatchingPotion)
            }
            adapter?.itemType = this.itemType
            recyclerView?.adapter = adapter
            recyclerView?.itemAnimator = SafeDefaultItemAnimator()

            adapter?.let {
                compositeSubscription.add(it.getEquipFlowable()
                        .flatMap<Items> { key -> inventoryRepository.equip(user, if (itemType == "pets") "pet" else "mount", key) }
                        .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
            }
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
        val observable: Maybe<out RealmResults<out Animal>> = if ("pets" == itemType) {
            inventoryRepository.getPets().firstElement()
        } else {
            inventoryRepository.getMounts().firstElement()
        }
        val ownedObservable: Maybe<out Map<String, OwnedObject>> = if ("pets" == itemType) {
            inventoryRepository.getOwnedPets().firstElement()
        } else {
            inventoryRepository.getOwnedMounts().firstElement()
        }.map {
            val animalMap = mutableMapOf<String, OwnedObject>()
            it.forEach { animal ->
                val castedAnimal = animal as? OwnedObject ?: return@forEach
                animalMap[castedAnimal.key ?: ""] = castedAnimal
            }
            animalMap
        }

        compositeSubscription.add(observable.zipWith(ownedObservable, BiFunction<RealmResults<out Animal>, Map<String, OwnedObject>, ArrayList<Any>> { unsortedAnimals, ownedAnimals ->
            mapAnimals(unsortedAnimals, ownedAnimals)
        }).subscribe(Consumer { items -> adapter?.setItemList(items) }, RxErrorHandler.handleEmptyError()))
    }

    private fun mapAnimals(unsortedAnimals: RealmResults<out Animal>, ownedAnimals: Map<String, OwnedObject>): ArrayList<Any> {
        val items = ArrayList<Any>()
        var lastAnimal: Animal = unsortedAnimals[0] ?: return items
        var lastSection: StableSection? = null
        for (animal in unsortedAnimals) {
            val identifier = if (animal.animal.isNotEmpty() && (animal.type != "special" && animal.type != "wacky")) animal.animal else animal.key
            val lastIdentifier = if (lastAnimal.animal.isNotEmpty()) lastAnimal.animal else lastAnimal.key
            if (animal.type == "premium") {
                if (!items.contains(lastAnimal)) {
                    items.add(lastAnimal)
                }
                lastAnimal = items.first { (it as? Animal)?.animal == animal.animal } as Animal
            } else if (identifier != lastIdentifier || animal === unsortedAnimals[unsortedAnimals.size - 1]) {
                if (!((lastAnimal.type == "special") && lastAnimal.numberOwned == 0) && !items.contains(lastAnimal)) {
                    items.add(lastAnimal)
                }
                lastAnimal = animal
            }


            if (animal.type != lastSection?.key && animal.type != "premium") {
                if (items.size > 0 && items[items.size - 1].javaClass == StableSection::class.java) {
                    items.removeAt(items.size - 1)
                }
                val section = StableSection(animal.type, animal.getTranslatedType(context))
                items.add(section)
                lastSection = section
            }
            val isOwned = when (itemType) {
                "pets" -> {
                    val ownedPet = ownedAnimals[animal?.key] as? OwnedPet
                    ownedPet?.trained ?: 0 > 0
                }
                "mounts" -> {
                    val ownedMount = ownedAnimals[animal?.key] as? OwnedMount
                    ownedMount?.owned == true
                }
                else -> false
            }
            lastAnimal.totalNumber += 1
            lastSection?.totalCount = (lastSection?.totalCount ?: 0) + 1
            if (isOwned) {
                lastAnimal.numberOwned += 1
                lastSection?.ownedCount = (lastSection?.ownedCount ?: 0) + 1
            }
        }
        if (!((lastAnimal.type == "premium" || lastAnimal.type == "special") && lastAnimal.numberOwned == 0)) {
            items.add(lastAnimal)
        }

        items.add(0, "header")
        return items
    }

    companion object {
        private const val ITEM_TYPE_KEY = "CLASS_TYPE_KEY"
        private const val HEADER_VIEW_TYPE = 0
    }
}
