package com.habitrpg.android.habitica.ui.adapter.inventory

import android.view.ViewGroup
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.ui.viewHolders.PetViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.realm.RealmResults

class PetDetailRecyclerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    private var existingMounts: RealmResults<Mount>? = null
    private var ownedPets: Map<String, OwnedPet>? = null
    private var ownedMounts: Map<String, OwnedMount>? = null
    private var ownedItems: Map<String, OwnedItem>? = null
    private val equipEvents = PublishSubject.create<String>()
    private var ownsSaddles: Boolean = false

    private var itemList: List<Any> = ArrayList()

    fun setItemList(itemList: List<Any>) {
        this.itemList = itemList
        this.notifyDataSetChanged()
    }

    fun getEquipFlowable(): Flowable<String> {
        return equipEvents.toFlowable(BackpressureStrategy.DROP)
    }

    var animalIngredientsRetriever: ((Animal, ((Pair<Egg?, HatchingPotion?>) -> Unit)) -> Unit)? = null

    private fun canRaiseToMount(pet: Pet): Boolean {
        for (mount in existingMounts ?: emptyList<Mount>()) {
            if (mount.key == pet.key) {
                return !(ownedMounts?.get(mount.key)?.owned ?: false)
            }
        }
        return false
    }

    private fun eggCount(pet: Pet): Int {
        return ownedItems?.get(pet.animal + "-eggs")?.numberOwned ?: 0
    }
    private fun potionCount(pet: Pet): Int {
        return ownedItems?.get(pet.color + "-hatchingPotions")?.numberOwned ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder =
        when (viewType) {
            1 -> SectionViewHolder(parent)
            else -> PetViewHolder(parent, equipEvents, animalIngredientsRetriever)
        }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        when (val obj = this.itemList[position]) {
            is StableSection -> {
                (holder as? SectionViewHolder)?.bind(obj)
            }
            is Pet -> {
                (holder as? PetViewHolder)?.bind(obj,
                        ownedPets?.get(obj.key ?: "")?.trained ?: 0,
                        eggCount(obj),
                        potionCount(obj),
                        canRaiseToMount(obj),
                        ownsSaddles,
                        ownedItems?.get(obj.animal + "-eggs") != null,
                        ownedItems?.get(obj.color + "-hatchingPotions") != null,
                        ownedMounts?.containsKey(obj.key) == true
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int = if (itemList[position] is StableSection) 1 else 2

    override fun getItemCount(): Int = itemList.size

    fun setExistingMounts(existingMounts: RealmResults<Mount>) {
        this.existingMounts = existingMounts
        notifyDataSetChanged()
    }

    fun setOwnedMounts(ownedMounts: Map<String, OwnedMount>) {
        this.ownedMounts = ownedMounts
        notifyDataSetChanged()
    }

    fun setOwnedPets(ownedPets: Map<String, OwnedPet>) {
        this.ownedPets = ownedPets
        notifyDataSetChanged()
    }

    fun setOwnedItems(ownedItems: Map<String, OwnedItem>) {
        this.ownedItems = ownedItems
        ownsSaddles = if (ownedItems.containsKey("Saddle-food")) (ownedItems["Saddle-food"]?.numberOwned ?: 0)> 0 else false
        notifyDataSetChanged()
    }
}
