package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.inventory.StableRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseFragment

import java.util.ArrayList

import javax.inject.Inject

import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.helpers.*
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Consumer

class StableRecyclerFragment : BaseFragment() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    private val recyclerView: RecyclerViewEmptySupport? by bindView(R.id.recyclerView)
    private val emptyView: TextView? by bindView(R.id.emptyView)
    var adapter: StableRecyclerAdapter? = null
    var itemType: String? = null
    var itemTypeText: String? = null
    var user: User? = null
    internal var layoutManager: GridLayoutManager? = null
    
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

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        resetViews()
        
        recyclerView?.setEmptyView(emptyView)
        emptyView?.text = getString(R.string.empty_items, itemTypeText)

        layoutManager = GridLayoutManager(activity, 2)
        layoutManager?.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return if (adapter?.getItemViewType(position) == 0) {
                    layoutManager?.spanCount ?: 1
                } else {
                    1
                }
            }
        }
        recyclerView?.layoutManager = layoutManager
        activity.notNull {
            recyclerView?.addItemDecoration(MarginDecoration(it))
        }


        adapter = recyclerView?.adapter as? StableRecyclerAdapter
        if (adapter == null) {
            adapter = StableRecyclerAdapter()
            adapter?.activity = this.activity as? MainActivity
            adapter?.itemType = this.itemType
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
        val observable: Flowable<out Animal> = if ("pets" == itemType) {
            inventoryRepository.getPets().firstElement().toFlowable().flatMap { Flowable.fromIterable(it) }
        } else {
            inventoryRepository.getMounts().firstElement().toFlowable().flatMap { Flowable.fromIterable(it) }
        }

        compositeSubscription.add(observable.toList().flatMap { unsortedAnimals ->
            val items = ArrayList<Any>()
            if (unsortedAnimals.size == 0) {
                return@flatMap Single.just<List<Any>>(items)
            }
            var lastSectionTitle = ""

            var lastAnimal: Animal = unsortedAnimals[0]
            for (animal in unsortedAnimals) {
                if (animal.animal != lastAnimal.animal || animal === unsortedAnimals[unsortedAnimals.size - 1]) {
                    if (!((lastAnimal.animalGroup == "premiumPets" || lastAnimal.animalGroup == "specialPets"
                                    || lastAnimal.animalGroup == "specialMounts" || lastAnimal.animalGroup == "premiumMounts") && lastAnimal.numberOwned == 0)) {
                        items.add(lastAnimal)
                    }
                    lastAnimal = animal
                }
                if (animal.animalGroup != lastSectionTitle) {
                    if (items.size > 0 && items[items.size - 1].javaClass == String::class.java) {
                        items.removeAt(items.size - 1)
                    }
                    items.add(animal.animalGroup)
                    lastSectionTitle = animal.animalGroup
                }
                if (user != null && user?.items != null) {
                    when (itemType) {
                        "pets" -> {
                            val pet = animal as Pet
                            if (pet.trained > 0) {
                                lastAnimal.numberOwned = lastAnimal.numberOwned + 1
                            }
                        }
                        "mounts" -> {
                            val mount = animal as Mount
                            if (mount.owned) {
                                lastAnimal.numberOwned = lastAnimal.numberOwned + 1
                            }
                        }
                    }
                }
            }
            Single.just<List<Any>>(items)
        }.subscribe(Consumer { items -> adapter?.setItemList(items) }, RxErrorHandler.handleEmptyError()))
    }

    companion object {
        private const val ITEM_TYPE_KEY = "CLASS_TYPE_KEY"
    }
}
