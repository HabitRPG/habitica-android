package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.ui.adapter.inventory.MountDetailRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.functions.Consumer

import javax.inject.Inject

class MountDetailRecyclerFragment : BaseMainFragment() {

    @Inject
    internal lateinit var inventoryRepository: InventoryRepository

    private val recyclerView: androidx.recyclerview.widget.RecyclerView by bindView(R.id.recyclerView)
    var adapter: MountDetailRecyclerAdapter? = null
    var animalType: String? = null
    var animalGroup: String? = null
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
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = MountDetailRecyclerFragmentArgs.fromBundle(it)
            animalGroup = args.group
            animalType = args.type
        }

        layoutManager = androidx.recyclerview.widget.GridLayoutManager(activity, 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(MarginDecoration(activity))

        adapter = recyclerView.adapter as? MountDetailRecyclerAdapter
        if (adapter == null) {
            adapter = MountDetailRecyclerAdapter(null, true)
            adapter?.itemType = this.animalType
            adapter?.context = context
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
            val itemWidth: Float = resources.getDimension(R.dimen.pet_width)

            spanCount = (width / itemWidth).toInt()
        }
        if (spanCount == 0) {
            spanCount = 1
        }
        layoutManager?.spanCount = spanCount
        layoutManager?.requestLayout()
    }

    private fun loadItems() {
        if (animalType != null && animalGroup != null) {
            compositeSubscription.add(inventoryRepository.getOwnedMounts().firstElement()
                    .map { ownedMounts ->
                        val mountMap = mutableMapOf<String, OwnedMount>()
                        ownedMounts.forEach { mountMap[it.key ?: ""] = it }
                        return@map mountMap
                    }
                    .subscribe(Consumer { adapter?.setOwnedMounts(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(inventoryRepository.getMounts(animalType!!, animalGroup!!).firstElement().subscribe(Consumer { adapter?.updateData(it) }, RxErrorHandler.handleEmptyError()))
        }
    }

    companion object {
        private const val ANIMAL_TYPE_KEY = "ANIMAL_TYPE_KEY"
    }
}
