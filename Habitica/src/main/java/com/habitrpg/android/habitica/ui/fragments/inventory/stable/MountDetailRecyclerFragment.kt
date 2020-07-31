package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.extensions.getTranslatedType
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedObject
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.ui.adapter.inventory.MountDetailRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Consumer
import io.realm.RealmResults

import javax.inject.Inject

class MountDetailRecyclerFragment : BaseMainFragment() {

    @Inject
    internal lateinit var inventoryRepository: InventoryRepository

    private val recyclerView: androidx.recyclerview.widget.RecyclerView by bindView(R.id.recyclerView)
    var adapter: MountDetailRecyclerAdapter? = null
    var animalType: String? = null
    var animalGroup: String? = null
    var animalColor: String? = null
    internal var layoutManager: androidx.recyclerview.widget.GridLayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        this.usesTabLayout = false
        super.onCreateView(inflater, container, savedInstanceState)
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
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(MarginDecoration(activity))

        adapter = recyclerView.adapter as? MountDetailRecyclerAdapter
        if (adapter == null) {
            adapter = MountDetailRecyclerAdapter()
            recyclerView.adapter = adapter
            recyclerView.itemAnimator = SafeDefaultItemAnimator()
            this.loadItems()

            adapter?.getEquipFlowable()?.flatMap { key -> inventoryRepository.equip(user, "mount", key) }
                    ?.subscribe(Consumer { }, RxErrorHandler.handleEmptyError())?.let { compositeSubscription.add(it) }
        }

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
        context?.resources?.let { resources
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
            compositeSubscription.add(inventoryRepository.getMounts(animalType, animalGroup, animalColor)
                    .zipWith(inventoryRepository.getOwnedMounts()
                    .map { ownedMounts ->
                        val mountMap = mutableMapOf<String, OwnedMount>()
                        ownedMounts.forEach { mountMap[it.key ?: ""] = it }
                        return@map mountMap
                    }.doOnNext {
                        adapter?.setOwnedMounts(it)
                    }, BiFunction<RealmResults<out Mount>, Map<String, OwnedObject>, List<Any>> { unsortedAnimals, ownedAnimals ->
                        val items = mutableListOf<Any>()
                        var lastMount: Mount? = null
                        var currentSection: StableSection? = null
                        for (mount in unsortedAnimals) {
                            if (mount.type == "wacky" || mount.type == "special") continue
                            if (mount.type != lastMount?.type) {
                                currentSection = StableSection(mount.type, mount.getTranslatedType(context))
                                items.add(currentSection)
                            }
                            currentSection?.let {
                                it.totalCount += 1
                                if (ownedAnimals.containsKey(mount.key)) {
                                    it.ownedCount += 1
                                }
                            }
                            items.add(mount)
                            lastMount = mount
                        }
                        items
                    })
                    .subscribe(Consumer { adapter?.setItemList(it) }, RxErrorHandler.handleEmptyError()))
        }
    }

    companion object {
        private const val ANIMAL_TYPE_KEY = "ANIMAL_TYPE_KEY"
    }
}
