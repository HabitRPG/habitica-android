package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Animal
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

        return container?.inflate(R.layout.fragment_recyclerview)
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
                return if (adapter?.getItemViewType(position) == 0) {
                    layoutManager?.spanCount ?: 1
                } else {
                    1
                }
            }
        }
        recyclerView?.layoutManager = layoutManager
        activity?.let {
            recyclerView?.addItemDecoration(MarginDecoration(it))
        }


        adapter = recyclerView?.adapter as? StableRecyclerAdapter
        if (adapter == null) {
            adapter = StableRecyclerAdapter()
            adapter?.activity = this.activity as? MainActivity
            adapter?.itemType = this.itemType
            adapter?.context = context
            recyclerView?.adapter = adapter
            recyclerView?.itemAnimator = SafeDefaultItemAnimator()
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
            val itemWidth: Float = context?.resources?.getDimension(R.dimen.pet_width) ?: 0.toFloat()

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
        var lastSectionTitle = ""

        for (animal in unsortedAnimals) {
            val identifier = if (animal.animal.isNotEmpty()) animal.animal else animal.key
            val lastIdentifier = if (lastAnimal.animal.isNotEmpty()) lastAnimal.animal else lastAnimal.key
            if (identifier != lastIdentifier || animal === unsortedAnimals[unsortedAnimals.size - 1]) {
                if (!((lastAnimal.type == "premium" || lastAnimal.type == "special") && lastAnimal.numberOwned == 0)) {
                    items.add(lastAnimal)
                }
                lastAnimal = animal
            }
            if (animal.type != lastSectionTitle) {
                if (items.size > 0 && items[items.size - 1].javaClass == String::class.java) {
                    items.removeAt(items.size - 1)
                }
                items.add(animal.type)
                lastSectionTitle = animal.type
            }
            when (itemType) {
                "pets" -> {
                    val ownedPet = ownedAnimals[animal?.key] as? OwnedPet
                    if (ownedPet?.trained ?: 0 > 0) {
                        lastAnimal.numberOwned += 1
                    }
                }
                "mounts" -> {
                    val ownedMount = ownedAnimals[animal?.key] as? OwnedMount
                    if (ownedMount?.owned == true) {
                        lastAnimal.numberOwned = lastAnimal.numberOwned + 1
                    }
                }
            }
        }
        if (!((lastAnimal.type == "premium" || lastAnimal.type == "special") && lastAnimal.numberOwned == 0)) {
            items.add(lastAnimal)
        }
        return items
    }

    companion object {
        private const val ITEM_TYPE_KEY = "CLASS_TYPE_KEY"
    }
}
