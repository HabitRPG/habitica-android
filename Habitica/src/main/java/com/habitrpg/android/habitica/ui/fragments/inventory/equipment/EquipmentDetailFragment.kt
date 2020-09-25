package com.habitrpg.android.habitica.ui.fragments.inventory.equipment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.InventoryRepository
import com.habitrpg.android.habitica.databinding.FragmentRecyclerviewBinding
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.ui.adapter.inventory.EquipmentRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.BaseMainFragment
import com.habitrpg.android.habitica.ui.helpers.SafeDefaultItemAnimator
import javax.inject.Inject

class EquipmentDetailFragment : BaseMainFragment<FragmentRecyclerviewBinding>() {

    @Inject
    lateinit var inventoryRepository: InventoryRepository

    override var binding: FragmentRecyclerviewBinding? = null

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentRecyclerviewBinding {
        return FragmentRecyclerviewBinding.inflate(inflater, container, false)
    }

    var type: String? = null
    var equippedGear: String? = null
    var isCostume: Boolean? = null

    private var adapter: EquipmentRecyclerViewAdapter = EquipmentRecyclerViewAdapter(null, true)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        compositeSubscription.add(this.adapter.equipEvents.flatMapMaybe { key -> inventoryRepository.equipGear(user, key, isCostume ?: false).firstElement() }
                .subscribe({ }, RxErrorHandler.handleEmptyError()))
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showsBackButton = true
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            val args = EquipmentDetailFragmentArgs.fromBundle(it)
            type = args.type
            isCostume = args.isCostume
            equippedGear = args.equippedGear
        }

        this.adapter.equippedGear = this.equippedGear
        this.adapter.isCostume = this.isCostume
        this.adapter.type = this.type

        binding?.recyclerView?.adapter = this.adapter
        binding?.recyclerView?.layoutManager = LinearLayoutManager(activity)
        binding?.recyclerView?.addItemDecoration(DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL))
        binding?.recyclerView?.itemAnimator = SafeDefaultItemAnimator()

        type?.let { type -> inventoryRepository.getOwnedEquipment(type).firstElement().subscribe({ this.adapter.updateData(it) }, RxErrorHandler.handleEmptyError()) }
    }

    override fun onDestroy() {
        inventoryRepository.close()
        super.onDestroy()
    }

    override fun injectFragment(component: UserComponent) {
        component.inject(this)
    }
}
