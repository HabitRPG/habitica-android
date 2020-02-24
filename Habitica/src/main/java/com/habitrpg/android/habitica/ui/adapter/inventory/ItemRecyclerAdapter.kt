package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ItemItemBinding
import com.habitrpg.android.habitica.events.commands.FeedCommand
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.shared.habitica.models.user.OwnedItem
import com.habitrpg.shared.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem
import com.habitrpg.android.habitica.ui.views.dialogs.DetailDialog
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.*

class ItemRecyclerAdapter(data: OrderedRealmCollection<OwnedItem>?, autoUpdate: Boolean, val context: Context) : RealmRecyclerViewAdapter<OwnedItem, ItemRecyclerAdapter.ItemViewHolder>(data, autoUpdate) {

    var isHatching: Boolean = false
    var isFeeding: Boolean = false
    var hatchingItem: Item? = null
    var feedingPet: Pet? = null
    var fragment: ItemRecyclerFragment? = null
    private var existingPets: RealmResults<Pet>? = null
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

    fun getSellItemFlowable(): Flowable<OwnedItem> {
        return sellItemEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getQuestInvitationFlowable(): Flowable<QuestContent> {
        return questInvitationEvents.toFlowable(BackpressureStrategy.DROP)
    }
    fun getOpenMysteryItemFlowable(): Flowable<Item> {
        return openMysteryItemEvents.toFlowable(BackpressureStrategy.DROP)
    }

    val startHatchingEvents = startHatchingSubject.toFlowable(BackpressureStrategy.DROP)
    val hatchPetEvents = hatchPetSubject.toFlowable(BackpressureStrategy.DROP)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(ItemItemBinding.inflate(context?.layoutInflater, parent, false))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        data?.let {
            val ownedItem = it[position]
            holder.bind(ownedItem, items?.get(ownedItem.key))
        }
    }

    fun setExistingPets(pets: RealmResults<Pet>) {
        existingPets = pets
        notifyDataSetChanged()
    }

    fun setOwnedPets(ownedPets: Map<String, OwnedPet>) {
        this.ownedPets = ownedPets
        notifyDataSetChanged()
    }

    inner class ItemViewHolder(val binding: ItemItemBinding) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {
        var ownedItem: OwnedItem? = null
        var item: Item? = null

        var resources: Resources = itemView.resources

        private val canHatch: Boolean
            get() {
                val petKey: String = if (item is Egg) {
                    item?.key + "-" + hatchingItem?.key
                } else {
                    hatchingItem?.key + "-" + item?.key
                }
                val pet = existingPets?.where()?.equalTo("key", petKey)?.findFirst()
                return pet != null && ownedPets?.get(pet.key)?.trained ?: 0 <= 0
            }

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(ownedItem: OwnedItem, item: Item?) {
            this.ownedItem = ownedItem
            this.item = item
            binding.titleTextView.text = item?.text
            binding.ownedTextView.text = ownedItem.numberOwned.toString()

            var disabled = false
            val imageName: String?
            if (item is QuestContent) {
                imageName = "inventory_quest_scroll_" + item.getKey()
            } else if (item is SpecialItem) {
                val sdf = SimpleDateFormat("MM", Locale.getDefault())
                val month = sdf.format(Date())
                imageName = "inventory_present_$month"
            } else {
                val type = when (item) {
                    is Egg -> "Egg"
                    is Food -> "Food"
                    is HatchingPotion -> "HatchingPotion"
                    else -> ""
                }
                imageName = "Pet_" + type + "_" + item?.key

                if (isHatching) {
                    disabled = !this.canHatch
                }
            }
            DataBindingUtils.loadImage(binding.imageView, imageName)

            var alpha = 1.0f
            if (disabled) {
                alpha = 0.3f
            }
            binding.imageView.alpha = alpha
            binding.titleTextView.alpha = alpha
            binding.ownedTextView.alpha = alpha
        }

        override fun onClick(v: View) {
            val context = context ?: return
            if (!isHatching && !isFeeding) {
                val menu = BottomSheetMenu(context)
                if (item !is QuestContent && item !is SpecialItem) {
                    menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.sell, item?.value), true))
                }
                if (item is Egg) {
                    menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.hatch_with_potion)))
                } else if (item is HatchingPotion) {
                    menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.hatch_egg)))
                } else if (item is QuestContent) {
                    menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.details)))
                    menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.invite_party)))
                } else if (item is SpecialItem) {
                    val specialItem = item as SpecialItem
                    if (specialItem.isMysteryItem && ownedItem?.numberOwned ?: 0 > 0) {
                        menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.open)))
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
                            is Food -> {
                                val event = FeedCommand()
                                event.usingFood = selectedItem
                                EventBus.getDefault().post(event)
                            }
                            is HatchingPotion -> startHatchingSubject.onNext(selectedItem)
                            is QuestContent -> {
                                if (index == 0) {
                                    val dialog = DetailDialog(context)
                                    dialog.quest = selectedItem
                                    dialog.show()
                                } else {
                                    questInvitationEvents.onNext(selectedItem)
                                }
                            }
                            is SpecialItem -> openMysteryItemEvents.onNext(selectedItem)
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
                        (hatchingItem as? HatchingPotion)?.let {potion ->
                            hatchPetSubject.onNext(Pair(potion, firstItem))
                        }
                    } else if (firstItem is HatchingPotion) {
                        (hatchingItem as? Egg)?.let {egg ->
                            hatchPetSubject.onNext(Pair(firstItem, egg))
                        }
                    }
                    return@let
                }
            } else if (isFeeding) {
                val event = FeedCommand()
                event.usingPet = feedingPet
                event.usingFood = item as? Food
                EventBus.getDefault().post(event)
                fragment?.dismiss()
            }
        }
    }
}
