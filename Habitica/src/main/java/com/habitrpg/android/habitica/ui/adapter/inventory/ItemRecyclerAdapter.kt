package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ItemItemBinding
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.inventory.Egg
import com.habitrpg.android.habitica.models.inventory.Food
import com.habitrpg.android.habitica.models.inventory.HatchingPotion
import com.habitrpg.android.habitica.models.inventory.Item
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem
import com.habitrpg.android.habitica.ui.views.dialogs.DetailDialog
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ItemRecyclerAdapter(val context: Context) : BaseRecyclerViewAdapter<OwnedItem, ItemRecyclerAdapter.ItemViewHolder>() {
    var user: User? = null
    var isHatching: Boolean = false
    var isFeeding: Boolean = false
    var hatchingItem: Item? = null
    var feedingPet: Pet? = null
    var fragment: DialogFragment? = null
    private var existingPets: List<Pet>? = null
    private var ownedPets: Map<String, OwnedPet>? = null
    var items: Map<String, Item>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val sellItemEvents = PublishSubject.create<OwnedItem>()
    private val questInvitationEvents = PublishSubject.create<QuestContent>()
    private val openMysteryItemEvents = PublishSubject.create<Item>()
    private val startHatchingSubject = PublishSubject.create<Item>()
    private val hatchPetSubject = PublishSubject.create<Pair<HatchingPotion, Egg>>()
    private val feedPetSubject = PublishSubject.create<Food>()
    private val createNewPartySubject = PublishSubject.create<Boolean>()
    private val useSpecialSubject = PublishSubject.create<SpecialItem>()

    fun getSellItemFlowable(): Flowable<OwnedItem> {
        return sellItemEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getQuestInvitationFlowable(): Flowable<QuestContent> {
        return questInvitationEvents.toFlowable(BackpressureStrategy.DROP)
    }
    fun getOpenMysteryItemFlowable(): Flowable<Item> {
        return openMysteryItemEvents.toFlowable(BackpressureStrategy.DROP)
    }

    val startHatchingEvents: Flowable<Item> = startHatchingSubject.toFlowable(BackpressureStrategy.DROP)
    val hatchPetEvents: Flowable<Pair<HatchingPotion, Egg>> = hatchPetSubject.toFlowable(BackpressureStrategy.DROP)
    val feedPetEvents: Flowable<Food> = feedPetSubject.toFlowable(BackpressureStrategy.DROP)
    val startNewPartyEvents: Flowable<Boolean> = createNewPartySubject.toFlowable(BackpressureStrategy.DROP)
    val useSpecialEvents: Flowable<SpecialItem> = useSpecialSubject.toFlowable(BackpressureStrategy.DROP)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(ItemItemBinding.inflate(context.layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val ownedItem = data[position]
        holder.bind(ownedItem, items?.get(ownedItem.key))
    }

    fun setExistingPets(pets: List<Pet>) {
        existingPets = pets
        notifyDataSetChanged()
    }

    fun setOwnedPets(ownedPets: Map<String, OwnedPet>) {
        this.ownedPets = ownedPets
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(val binding: ItemItemBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        private var ownedItem: OwnedItem? = null
        var item: Item? = null

        var resources: Resources = itemView.resources

        private val canHatch: Boolean
            get() {
                val petKey: String = if (item is Egg) {
                    item?.key + "-" + hatchingItem?.key
                } else {
                    hatchingItem?.key + "-" + item?.key
                }
                val pet = existingPets?.firstOrNull { it.key == petKey && it.type != "special" }
                return pet != null && ownedPets?.get(pet.key)?.trained ?: 0 <= 0
            }

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(ownedItem: OwnedItem, item: Item?) {
            this.ownedItem = ownedItem
            this.item = item
            binding.titleTextView.text = item?.text ?: ownedItem.key
            binding.ownedTextView.text = ownedItem.numberOwned.toString()

            val disabled = if (isHatching) {
                    !this.canHatch
                } else false
            val imageName = getImageName(item)
            binding.imageView.loadImage(imageName)

            var alpha = 1.0f
            if (disabled) {
                alpha = 0.3f
            }
            binding.imageView.alpha = alpha
            binding.titleTextView.alpha = alpha
            binding.ownedTextView.alpha = alpha
        }

        private fun getImageName(
            item: Item?,
        ): String {
            return if (item is QuestContent) {
                "inventory_quest_scroll_" + item.key
            } else if (item is SpecialItem) {
                if (item.key == "inventory_present") {
                    val sdf = SimpleDateFormat("MM", Locale.getDefault())
                    val month = sdf.format(Date())
                    "inventory_present_$month"
                } else {
                    "shop_" + item.key
                }
            } else {
                val type = when (item?.type) {
                    "eggs" -> "Egg"
                    "food" -> "Food"
                    "hatchingPotions" -> "HatchingPotion"
                    else -> ""
                }
                "Pet_" + type + "_" + item?.key
            }
        }

        override fun onClick(v: View) {
            val context = context
            if (!isHatching && !isFeeding) {
                val menu = BottomSheetMenu(context)
                menu.setTitle(item?.text)
                menu.setImage(getImageName(item))
                if (item !is QuestContent && item !is SpecialItem) {
                    menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.sell_no_price), true, "gold", item?.value?.toDouble() ?: 0.0))
                }
                if (item is Egg) {
                    menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.hatch_with_potion)))
                } else if (item is HatchingPotion) {
                    menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.hatch_egg)))
                } else if (item is QuestContent) {
                    menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.details)))
                    if (user?.hasParty == true) {
                        menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.invite_party)))
                    } else {
                        menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.create_new_party)))
                    }
                } else if (item is SpecialItem) {
                    val specialItem = item as SpecialItem
                    if (specialItem.isMysteryItem && ownedItem?.numberOwned ?: 0 > 0) {
                        menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.open)))
                    } else if (ownedItem?.numberOwned ?: 0 > 0) {
                        menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.use_item)))
                    }
                }
                menu.setSelectionRunnable { index ->
                    item?.let { selectedItem ->
                        if (!(selectedItem is QuestContent || selectedItem is SpecialItem) && index == 0) {
                            ownedItem?.let { selectedOwnedItem -> sellItemEvents.onNext(selectedOwnedItem) }
                            return@let
                        }
                        when (selectedItem) {
                            is Egg -> item?.let { startHatchingSubject.onNext(it) }
                            is HatchingPotion -> startHatchingSubject.onNext(selectedItem)
                            is QuestContent -> {
                                if (index == 0) {
                                    val dialog = DetailDialog(context)
                                    dialog.quest = selectedItem
                                    dialog.show()
                                } else {
                                    if (user?.hasParty == true) {
                                        questInvitationEvents.onNext(selectedItem)
                                    } else {
                                        createNewPartySubject.onNext(true)
                                    }
                                }
                            }
                            is SpecialItem ->
                                if (item?.key != "inventory_present") {
                                    useSpecialSubject.onNext(selectedItem)
                                } else {
                                    openMysteryItemEvents.onNext(selectedItem)
                                }
                        }
                    }
                }
                menu.show()
            } else if (isHatching) {
                if (!this.canHatch) {
                    return
                }
                item?.let { firstItem ->
                    if (firstItem is Egg) {
                        (hatchingItem as? HatchingPotion)?.let { potion ->
                            hatchPetSubject.onNext(Pair(potion, firstItem))
                        }
                    } else if (firstItem is HatchingPotion) {
                        (hatchingItem as? Egg)?.let { egg ->
                            hatchPetSubject.onNext(Pair(firstItem, egg))
                        }
                    }
                    return@let
                }
            } else if (isFeeding) {
                feedPetSubject.onNext(item as Food?)
                fragment?.dismiss()
            }
        }
    }
}
