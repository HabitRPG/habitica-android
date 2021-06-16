package com.habitrpg.android.habitica.ui.adapter.inventory

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ShopHeaderBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.StableFragmentDirections
import com.habitrpg.android.habitica.ui.helpers.loadImage
import com.habitrpg.android.habitica.ui.viewHolders.MountViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.PetViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject


class StableRecyclerAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var shopSpriteSuffix: String? = null
    private var eggs: Map<String, Egg> = mapOf()
    var animalIngredientsRetriever: ((Animal, ((Pair<Egg?, HatchingPotion?>) -> Unit)) -> Unit)? = null
    var itemType: String? = null
    private var user: User? = null
    private val equipEvents = PublishSubject.create<String>()
    private var existingMounts: List<Mount>? = null
    private var ownedMounts: Map<String, OwnedMount>? = null
    private var ownedItems: Map<String, OwnedItem>? = null
    private var ownsSaddles: Boolean = false
    private var itemList: List<Any> = ArrayList()

    fun getEquipFlowable(): Flowable<String> {
        return equipEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun setUser(user: User){
        this.user = user
        notifyDataSetChanged()
    }

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

    fun setOwnedMounts(ownedMounts: Map<String, OwnedMount>) {
        this.ownedMounts = ownedMounts
        notifyDataSetChanged()
    }

    fun setOwnedItems(ownedItems: Map<String, OwnedItem>) {
        this.ownedItems = ownedItems
        ownsSaddles = if (ownedItems.containsKey("Saddle-food")) (ownedItems["Saddle-food"]?.numberOwned ?: 0)> 0 else false
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
                2 -> PetViewHolder(parent, equipEvents, animalIngredientsRetriever)
                3 -> MountViewHolder(parent, equipEvents)
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
                        (holder as? PetViewHolder)?.bind(
                            item = item,
                            trained = item.numberOwned,
                            eggCount = eggCount(item),
                            potionCount = potionCount(item),
                            canRaiseToMount = canRaiseToMount(item),
                            ownsSaddles = ownsSaddles,
                            hasUnlockedEgg = ownedItems?.get(item.animal + "-eggs") != null,
                            hasUnlockedPotion = ownedItems?.get(item.color + "-hatchingPotions") != null,
                            hasMount = ownedMounts?.containsKey(item.key) == true,
                            user = user
                        )
                    } else if (item is Mount) {
                        (holder as? MountViewHolder)?.bind(item, item.numberOwned > 0, user)
                    }
                    return
                }
                (holder as? StableViewHolder)?.bind(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = itemList[position]
        return if (item == "header") {
            0
        } else if (item is StableSection) {
            1
        } else if (item is Animal) {
            val isIndividualAnimal = item.type == "special" || item.type == "wacky"
            if (isIndividualAnimal) {
                if (itemType == "pets") {
                    2
                } else {
                    3
                }
            } else {
                if (itemType == "pets") {
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

        fun bind() {
            binding.npcBannerView.shopSpriteSuffix = shopSpriteSuffix ?: ""
            binding.npcBannerView.identifier = "stable"
            binding.namePlate.setText(R.string.stable_owner)
            binding.descriptionView.visibility = View.GONE
        }
    }

    internal inner class StableViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var animal: Animal? = null

        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val titleView: TextView = itemView.findViewById(R.id.titleTextView)
        private val ownedTextView: TextView = itemView.findViewById(R.id.ownedTextView)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(item: Animal) {
            this.animal = item
            val context = itemView.context
            val egg = eggs[item.animal]
            if (egg != null) {
                titleView.text = if (item.type == "drop" || itemType == "mounts") egg.mountText else egg.text
            } else item.animal
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

            val alpha = if (item.numberOwned <= 0 && (ownedItems?.containsKey(item.animal) != true || itemType == "mounts")) 0.2f else 1.0f
            this.imageView.alpha = alpha
            this.titleView.alpha = alpha
            this.ownedTextView.alpha = alpha

            if (item.numberOwned == item.totalNumber) {
                this.ownedTextView.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_animalitem_complete)
                this.ownedTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
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
