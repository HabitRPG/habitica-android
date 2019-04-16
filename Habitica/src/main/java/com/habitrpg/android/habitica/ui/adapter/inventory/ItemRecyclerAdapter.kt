package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.content.res.Resources
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.OpenMysteryItemEvent
import com.habitrpg.android.habitica.events.commands.FeedCommand
import com.habitrpg.android.habitica.events.commands.HatchingCommand
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.models.inventory.*
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.models.user.OwnedPet
import com.habitrpg.android.habitica.ui.fragments.inventory.items.ItemRecyclerFragment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import io.realm.RealmResults
import org.greenrobot.eventbus.EventBus

class ItemRecyclerAdapter(data: OrderedRealmCollection<OwnedItem>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<OwnedItem, ItemRecyclerAdapter.ItemViewHolder>(data, autoUpdate) {

    var isHatching: Boolean = false
    var isFeeding: Boolean = false
    var hatchingItem: Item? = null
    var feedingPet: Pet? = null
    var fragment: ItemRecyclerFragment? = null
    private var existingPets: RealmResults<Pet>? = null
    private var ownedPets: Map<String, OwnedPet>? = null
    var context: Context? = null
    var items: Map<String, Item>? = null
    set(value) {
        field = value
        notifyDataSetChanged()
    }

    private val sellItemEvents = PublishSubject.create<Item>()
    private val questInvitationEvents = PublishSubject.create<QuestContent>()

    fun getSellItemFlowable(): Flowable<Item> {
        return sellItemEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getQuestInvitationFlowable(): Flowable<QuestContent> {
        return questInvitationEvents.toFlowable(BackpressureStrategy.DROP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        return ItemViewHolder(parent.inflate(R.layout.item_item))
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        data.notNull {
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

    inner class ItemViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var ownedItem: OwnedItem? = null
        var item: Item? = null

        private val titleTextView: TextView by bindView(R.id.titleTextView)
        private val ownedTextView: TextView by bindView(R.id.ownedTextView)
        private val imageView: SimpleDraweeView by bindView(R.id.imageView)

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
            titleTextView.text = item?.text
            ownedTextView.text = ownedItem.numberOwned.toString()

            var disabled = false
            val imageName: String?
            if (item is QuestContent) {
                imageName = "inventory_quest_scroll_" + item.getKey()
            } else if (item is SpecialItem) {
                imageName = item.getKey()
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
            DataBindingUtils.loadImage(imageView, imageName ?: "head_0")

            var alpha = 1.0f
            if (disabled) {
                alpha = 0.3f
            }
            imageView.alpha = alpha
            titleTextView.alpha = alpha
            ownedTextView.alpha = alpha
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
                    menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.invite_party)))
                } else if (item is SpecialItem) {
                    val specialItem = item as SpecialItem
                    if (specialItem.isMysteryItem && ownedItem?.numberOwned ?: 0 > 0) {
                        menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.open)))
                    }
                }
                menu.setSelectionRunnable { index ->
                    item.notNull { selectedItem ->
                        if (!(selectedItem is QuestContent || selectedItem is SpecialItem) && index == 0) {
                            sellItemEvents.onNext(selectedItem)
                            return@notNull
                        }
                        when (selectedItem) {
                            is Egg -> {
                                val event = HatchingCommand()
                                event.usingEgg = selectedItem
                                EventBus.getDefault().post(event)
                            }
                            is Food -> {
                                val event = FeedCommand()
                                event.usingFood = selectedItem
                                EventBus.getDefault().post(event)
                            }
                            is HatchingPotion -> {
                                val event = HatchingCommand()
                                event.usingHatchingPotion = selectedItem
                                EventBus.getDefault().post(event)
                            }
                            is QuestContent -> questInvitationEvents.onNext(selectedItem)
                            is SpecialItem -> EventBus.getDefault().post(OpenMysteryItemEvent())
                        }
                    }
                }
                menu.show()
            } else if (isHatching) {
                if (!this.canHatch) {
                    return
                }
                if (item is Egg) {
                    val event = HatchingCommand()
                    event.usingEgg = item as Egg
                    event.usingHatchingPotion = hatchingItem as HatchingPotion?
                    EventBus.getDefault().post(event)
                } else if (item is HatchingPotion) {
                    val event = HatchingCommand()
                    event.usingHatchingPotion = item as HatchingPotion
                    event.usingEgg = hatchingItem as Egg?
                    EventBus.getDefault().post(event)
                }
                fragment?.dismiss()
            } else if (isFeeding) {
                val event = FeedCommand()
                event.usingPet = feedingPet
                event.usingFood = item as Food
                EventBus.getDefault().post(event)
                fragment?.dismiss()
            }

        }
    }
}
