package com.habitrpg.android.habitica.ui.adapter.inventory

import android.view.View
import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.CanHatchItemBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.ui.viewHolders.PetViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import com.habitrpg.android.habitica.ui.views.dialogs.PetSuggestHatchDialog
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.helpers.Animations

class PetDetailRecyclerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    var onFeed: ((Pet, Food?) -> Unit)? = null
    var onEquip: ((String) -> Unit)? = null
    private var existingMounts: List<Mount>? = null
    private var ownedPets: Map<String, OwnedPet>? = null
    private var ownedMounts: Map<String, OwnedMount>? = null
    private var ownedItems: Map<String, OwnedItem>? = null
    var currentPet: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var ownsSaddles: Boolean = false

    private var itemList: List<Any> = ArrayList()

    fun setItemList(itemList: List<Any>) {
        this.itemList = itemList
        this.notifyDataSetChanged()
    }

    var animalIngredientsRetriever: ((Animal, ((Pair<Egg?, HatchingPotion?>) -> Unit)) -> Unit)? = null

    private fun canRaiseToMount(pet: Pet): Boolean {
        if (pet.type == "special") return false
        for (mount in existingMounts ?: emptyList()) {
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
            2 -> CanHatchViewHolder(parent, animalIngredientsRetriever)
            else -> PetViewHolder(parent, onEquip, onFeed, animalIngredientsRetriever)
        }

    override fun onBindViewHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val obj = this.itemList[position]) {
            is StableSection -> {
                (holder as? SectionViewHolder)?.bind(obj)
            }
            is Pet -> {
                val trained = ownedPets?.get(obj.key ?: "")?.trained ?: 0
                val eggCount = eggCount(obj)
                val potionCount = potionCount(obj)
                if (trained <= 0 && eggCount > 0 && potionCount > 0) {
                    (holder as? CanHatchViewHolder)?.bind(
                        obj,
                        eggCount,
                        potionCount,
                        ownedItems?.get(obj.animal + "-eggs") != null,
                        ownedItems?.get(obj.color + "-hatchingPotions") != null,
                        ownedMounts?.containsKey(obj.key) == true,
                    )
                } else {
                    (holder as? PetViewHolder)?.bind(
                        obj,
                        trained,
                        eggCount,
                        potionCount,
                        canRaiseToMount(obj),
                        ownsSaddles,
                        ownedItems?.get(obj.animal + "-eggs") != null,
                        ownedItems?.get(obj.color + "-hatchingPotions") != null,
                        ownedMounts?.containsKey(obj.key) == true,
                        currentPet
                    )
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (itemList.size <= position) return 3
        return if (itemList[position] is StableSection) {
            1
        } else {
            val pet = itemList[position] as Pet
            if (ownedPets?.get(pet.key ?: "")?.trained ?: 0 <= 0 && eggCount(pet) > 0 && potionCount(pet) > 0) {
                2
            } else {
                3
            }
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun setExistingMounts(existingMounts: List<Mount>) {
        this.existingMounts = existingMounts
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
        ownsSaddles = if (ownedItems.containsKey("Saddle-food")) (ownedItems["Saddle-food"]?.numberOwned ?: 0) > 0 else false
        notifyDataSetChanged()
    }

    class CanHatchViewHolder(
        parent: ViewGroup,
        private val ingredientsReceiver: ((Animal, ((Pair<Egg?, HatchingPotion?>) -> Unit)) -> Unit)?
    ) : androidx.recyclerview.widget.RecyclerView.ViewHolder(parent.inflate(R.layout.can_hatch_item)),
        View.OnClickListener {
        private var binding = CanHatchItemBinding.bind(itemView)

        private var hasMount: Boolean = false
        private var hasUnlockedPotion: Boolean = false
        private var hasUnlockedEgg: Boolean = false
        private var eggCount: Int = 0
        private var potionCount: Int = 0
        private var animal: Pet? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(
            item: Pet,
            eggCount: Int,
            potionCount: Int,
            hasUnlockedEgg: Boolean,
            hasUnlockedPotion: Boolean,
            hasMount: Boolean,
        ) {
            this.animal = item
            this.eggCount = eggCount
            this.potionCount = potionCount
            this.hasUnlockedEgg = hasUnlockedEgg
            this.hasUnlockedPotion = hasUnlockedPotion
            this.hasMount = hasMount

            binding.eggView.loadImage("Pet_Egg_${item.animal}")
            binding.hatchingPotionView.loadImage("Pet_HatchingPotion_${item.color}")

            binding.eggView.startAnimation(Animations.bobbingAnimation(4f))
            binding.hatchingPotionView.startAnimation(Animations.bobbingAnimation(-4f))
        }

        override fun onClick(p0: View?) {
            val context = itemView.context
            val dialog = PetSuggestHatchDialog(context)
            animal?.let {
                ingredientsReceiver?.invoke(it) { ingredients ->
                    dialog.configure(
                        it,
                        ingredients.first,
                        ingredients.second,
                        eggCount,
                        potionCount,
                        hasUnlockedEgg,
                        hasUnlockedPotion,
                        hasMount
                    )
                    dialog.show()
                }
            }
        }
    }
}
