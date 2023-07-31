package com.habitrpg.android.habitica.ui.adapter.inventory

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ShopHeaderBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
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
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.StableFragmentDirections
import com.habitrpg.android.habitica.ui.viewHolders.MountViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.PetViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.views.PixelArtView

class StableRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var shopSpriteSuffix: String? = null
    private var eggs: Map<String, Egg> = mapOf()
    var animalIngredientsRetriever: ((Animal, ((Pair<Egg?, HatchingPotion?>) -> Unit)) -> Unit)? = null
    var onFeed: ((Pet, Food?) -> Unit)? = null
    var itemType: String? = null
    var currentPet: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var currentMount: String? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var onEquip: ((String) -> Unit)? = null
    private var existingMounts: List<Mount>? = null
    private var ownedPets: Map<String, OwnedPet>? = null
    private var ownedMounts: Map<String, OwnedMount>? = null
    private var ownedItems: Map<String, OwnedItem>? = null
    private var ownsSaddles: Boolean = false
    private var itemList: List<Any> = ArrayList()

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

    fun setOwnedPets(ownedPets: Map<String, OwnedPet>) {
        this.ownedPets = ownedPets
        notifyDataSetChanged()
    }

    fun setOwnedMounts(ownedMounts: Map<String, OwnedMount>) {
        this.ownedMounts = ownedMounts
        notifyDataSetChanged()
    }

    fun setOwnedItems(ownedItems: Map<String, OwnedItem>) {
        this.ownedItems = ownedItems
        ownsSaddles = if (ownedItems.containsKey("Saddle-food")) (ownedItems["Saddle-food"]?.numberOwned ?: 0) > 0 else false
        notifyDataSetChanged()
    }

    fun setExistingMounts(existingMounts: List<Mount>) {
        this.existingMounts = existingMounts
        notifyDataSetChanged()
    }

    fun setItemList(itemList: List<Any>) {
        this.itemList = itemList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            1 -> SectionViewHolder(parent)
            4 -> StableViewHolder(parent.inflate(R.layout.pet_overview_item))
            5 -> StableViewHolder(parent.inflate(R.layout.mount_overview_item))
            2 -> PetViewHolder(parent, onEquip, onFeed, animalIngredientsRetriever)
            22 -> PetDetailRecyclerAdapter.CanHatchViewHolder(parent, animalIngredientsRetriever)
            3 -> MountViewHolder(parent, onEquip)
            else -> StableHeaderViewHolder(parent)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = this.itemList[position]) {
            "header" -> (holder as? StableHeaderViewHolder)?.bind()
            is StableSection -> {
                if (item.key == "drop") {
                    val params = holder.itemView.layoutParams as GridLayoutManager.LayoutParams
                    params.topMargin = -50
                    holder.itemView.layoutParams = params
                }
                (holder as? SectionViewHolder)?.bind(item)
            }
            is Animal -> {
                val isIndividualAnimal = item.type == "special" || item.type == "wacky"
                if (isIndividualAnimal) {
                    if (item is Pet) {
                        val trained = ownedPets?.get(item.key ?: "")?.trained ?: 0
                        val eggCount = eggCount(item)
                        val potionCount = potionCount(item)
                        if (trained <= 0 && eggCount > 0 && potionCount > 0) {
                            (holder as? PetDetailRecyclerAdapter.CanHatchViewHolder)?.bind(
                                item,
                                eggCount,
                                potionCount,
                                ownedItems?.get(item.animal + "-eggs") != null,
                                ownedItems?.get(item.color + "-hatchingPotions") != null,
                                ownedMounts?.containsKey(item.key) == true
                            )
                        } else {
                            (holder as? PetViewHolder)?.bind(
                                item,
                                trained,
                                eggCount,
                                potionCount,
                                canRaiseToMount(item),
                                ownsSaddles,
                                ownedItems?.get(item.animal + "-eggs") != null,
                                ownedItems?.get(item.color + "-hatchingPotions") != null,
                                ownedMounts?.containsKey(item.key) == true,
                                currentPet
                            )
                        }
                    } else if (item is Mount) {
                        (holder as? MountViewHolder)?.bind(item, item.numberOwned > 0, currentMount)
                    }
                    return
                }
                (holder as? StableViewHolder)?.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (itemList.size <= position) { return 0 }
        val item = itemList[position]
        return if (item == "header") {
            0
        } else if (item is StableSection) {
            1
        } else if (item is Animal) {
            val isIndividualAnimal = item.type == "special" || item.type == "wacky"
            if (isIndividualAnimal) {
                if (item is Pet) {
                    if ((
                        ownedPets?.get(item.key ?: "")?.trained
                            ?: 0
                        ) <= 0 && eggCount(item) > 0 && potionCount(item) > 0
                    ) {
                        22
                    } else {
                        2
                    }
                } else {
                    3
                }
            } else {
                if (item is Pet) {
                    4
                } else {
                    5
                }
            }
        } else {
            0
        }
    }

    override fun getItemCount(): Int = itemList.size

    fun setEggs(eggs: Map<String, Egg>) {
        this.eggs = eggs
        notifyDataSetChanged()
    }

    internal inner class StableHeaderViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(parent.inflate(R.layout.shop_header)) {
        private var binding: ShopHeaderBinding = ShopHeaderBinding.bind(itemView)

        init {
            binding.root.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.window_background))
        }

        fun bind() {
            binding.npcBannerView.shopSpriteSuffix = shopSpriteSuffix ?: ""
            binding.npcBannerView.identifier = "stable"
            binding.namePlate.setText(R.string.stable_owner)
            binding.descriptionView.visibility = View.GONE
        }
    }

    internal inner class StableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var animal: Animal? = null

        private val imageView: PixelArtView = itemView.findViewById(R.id.imageView)
        private val titleView: TextView = itemView.findViewById(R.id.titleTextView)
        private val ownedTextView: TextView = itemView.findViewById(R.id.ownedTextView)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(item: Animal) {
            this.animal = item
            val context = itemView.context
            val egg = eggs[item.animal]
            titleView.text = if (egg != null) {
                if (item.type == "drop" || itemType == "mounts") egg.mountText else egg.text
            } else {
                item.animal
            }
            ownedTextView.visibility = View.VISIBLE

            val imageName = if (itemType == "pets") {
                "Pet_Egg_" + item.animal
            } else {
                "Mount_Icon_" + item.animal + "-Base"
            }

            this.ownedTextView.text = context.getString(R.string.pet_ownership_fraction, item.numberOwned, item.totalNumber)
            this.ownedTextView.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_shopitem_price)

            this.ownedTextView.setTextColor(ContextCompat.getColor(context, R.color.text_ternary))

            ownedTextView.visibility = View.VISIBLE
            imageView.loadImage(imageName)

            val alpha = if (item.numberOwned <= 0 && (ownedItems?.containsKey(item.animal + "-eggs") != true || itemType == "mounts")) 0.2f else 1.0f
            this.imageView.alpha = alpha
            this.titleView.alpha = alpha
            this.ownedTextView.alpha = alpha

            if (item.numberOwned == item.totalNumber) {
                this.ownedTextView.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_animalitem_complete)
                this.ownedTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
            }

            itemView.contentDescription = "${titleView.text} ${ownedTextView.text}"
        }

        override fun onClick(v: View) {
            val animal = this.animal
            if (animal != null) {
                val color = if (animal.type == "special") animal.color else null
                if (itemType == "pets") {
                    MainNavigationController.navigate(StableFragmentDirections.openPetDetail(animal.animal, animal.type ?: "", color))
                } else {
                    MainNavigationController.navigate(StableFragmentDirections.openMountDetail(animal.animal, animal.type ?: "", color))
                }
            }
        }
    }
}
