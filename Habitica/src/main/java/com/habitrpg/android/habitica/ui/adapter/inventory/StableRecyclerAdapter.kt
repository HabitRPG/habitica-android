package com.habitrpg.android.habitica.ui.adapter.inventory

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ShopHeaderBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.StableFragmentDirections
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.helpers.loadImage
import com.habitrpg.android.habitica.ui.viewHolders.MountViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.PetViewHolder
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.realm.RealmResults


class StableRecyclerAdapter() : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var shopSpriteSuffix: String? = null
    private var eggs: Map<String, Egg> = mapOf()
    var animalIngredientsRetriever: ((Animal) -> Pair<Egg?, HatchingPotion?>)? = null
    var itemType: String? = null
    private val equipEvents = PublishSubject.create<String>()
    var ownedEggs: Map<String, OwnedItem>? = null

    fun getEquipFlowable(): Flowable<String> {
        return equipEvents.toFlowable(BackpressureStrategy.DROP)
    }

    private var itemList: List<Any> = ArrayList()

    fun setItemList(itemList: List<Any>) {
        this.itemList = itemList
        this.notifyDataSetChanged()
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
                        (holder as? PetViewHolder)?.bind(item,
                                item.numberOwned,
                                canRaiseToMount = false,
                                eggCount = 0,
                                potionCount = 0,
                                ownsSaddles = false,
                                hasUnlockedEgg = false,
                                hasUnlockedPotion = false,
                                hasMount = false)
                    } else if (item is Mount) {
                        (holder as? MountViewHolder)?.bind(item, item.numberOwned > 0)
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

        private val imageView: SimpleDraweeView by bindView(itemView, R.id.imageView)
        private val titleView: TextView by bindView(itemView, R.id.titleTextView)
        private val ownedTextView: TextView by bindView(itemView, R.id.ownedTextView)

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
                "Mount_Icon_" + item.key
            }

            this.ownedTextView.text = context.getString(R.string.pet_ownership_fraction, item.numberOwned, item.totalNumber)
            this.ownedTextView.background = context.getDrawable(R.drawable.layout_rounded_bg_shopitem_price)

            this.ownedTextView.setTextColor(ContextCompat.getColor(context, R.color.gray_200))

            ownedTextView.visibility = View.VISIBLE
            imageView.loadImage(imageName)

            val alpha = if (item.numberOwned <= 0 && ownedEggs?.containsKey(item.animal ?: "") != true) 0.2f else 1.0f
            this.imageView.alpha = alpha
            this.titleView.alpha = alpha
            this.ownedTextView.alpha = alpha

            if (item.numberOwned == item.totalNumber) {
                this.ownedTextView.background = context.getDrawable(R.drawable.layout_rounded_bg_animalitem_complete)
                this.ownedTextView.setTextColor(ContextCompat.getColor(context, R.color.white))
            }
        }

        override fun onClick(v: View) {
            val animal = this.animal
            if (animal != null) {
                val color = if (animal.type == "special") animal.color else null
                if (animal.numberOwned > 0 || ownedEggs?.containsKey(animal.animal ?: "") == true) {
                    if (itemType == "pets") {
                        MainNavigationController.navigate(StableFragmentDirections.openPetDetail(animal.animal, animal.type, color))
                    } else {
                        MainNavigationController.navigate(StableFragmentDirections.openMountDetail(animal.animal, animal.type, color))
                    }
                }
            }
        }
    }
}
