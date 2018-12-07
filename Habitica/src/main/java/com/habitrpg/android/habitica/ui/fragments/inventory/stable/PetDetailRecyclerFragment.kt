package com.habitrpg.android.habitica.ui.fragments.inventory.stable

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.events.commands.FeedCommand
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.user.Items
import com.habitrpg.android.habitica.ui.adapter.inventory.PetDetailRecyclerAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment
import com.habitrpg.android.habitica.ui.helpers.MarginDecoration
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.resetViews
import io.reactivex.functions.Consumer
import io.realm.RealmResults
import org.greenrobot.eventbus.Subscribe
import javax.inject.Inject

class PetDetailRecyclerFragment : BaseMainFragment() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    private val recyclerView: androidx.recyclerview.widget.RecyclerView by bindView(R.id.recyclerView)

    var adapter: PetDetailRecyclerAdapter = PetDetailRecyclerAdapter(null, true)
    var animalType: String = ""
    var animalGroup: String = ""
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

    override fun injectFragment(component: AppComponent) {
        component.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments.notNull {
            val args = MountDetailRecyclerFragmentArgs.fromBundle(it)
            animalGroup = args.group
            animalType = args.type
        }

        resetViews()

        layoutManager = androidx.recyclerview.widget.GridLayoutManager(getActivity(), 2)
        recyclerView.layoutManager = layoutManager
        recyclerView.addItemDecoration(MarginDecoration(getActivity()))

        adapter.context = this.getActivity()
        adapter.itemType = this.animalType
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = SafeDefaultItemAnimator()
        this.loadItems()

        compositeSubscription.add(adapter.getEquipFlowable()
                .flatMap<Items> { key -> inventoryRepository.equip(user, "pet", key) }
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))

        view.post { setGridSpanCount(view.width) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(ANIMAL_TYPE_KEY, this.animalType)
    }

    private fun setGridSpanCount(width: Int) {
        var spanCount = 0
        if (context != null && context?.resources != null) {
            val itemWidth: Float = context?.resources?.getDimension(R.dimen.pet_width) ?: 120f

            spanCount = (width / itemWidth).toInt()
        }
        if (spanCount == 0) {
            spanCount = 1
        }
        layoutManager?.spanCount = spanCount
        layoutManager?.requestLayout()
    }

    private fun loadItems() {
        if (animalType.isNotEmpty() && animalGroup.isNotEmpty()) {
            compositeSubscription.add(inventoryRepository.getPets(animalType, animalGroup).firstElement().subscribe(Consumer<RealmResults<Pet>> { adapter.updateData(it) }, RxErrorHandler.handleEmptyError()))
            compositeSubscription.add(inventoryRepository.getOwnedMounts(animalType, animalGroup).subscribe(Consumer<RealmResults<Mount>> { adapter.setOwnedMounts(it) }, RxErrorHandler.handleEmptyError()))
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
            fragment.show(fragmentManager, "feedDialog")
        }
    }

    override fun customTitle(): String {
        return if (!isAdded) {
            ""
        } else getString(R.string.pets)
    }

    companion object {
        private const val ANIMAL_TYPE_KEY = "ANIMAL_TYPE_KEY"
    }
}
