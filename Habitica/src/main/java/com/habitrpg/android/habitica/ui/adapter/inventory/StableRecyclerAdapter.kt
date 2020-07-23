package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.events.commands.FeedCommand
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.models.inventory.StableSection
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.StableFragmentDirections
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import com.habitrpg.android.habitica.ui.views.NPCBannerView
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import org.greenrobot.eventbus.EventBus


class StableRecyclerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var itemType: String? = null
    var context: Context? = null
    var activity: MainActivity? = null
    private val equipEvents = PublishSubject.create<String>()

    fun getEquipFlowable(): Flowable<String> {
        return equipEvents.toFlowable(BackpressureStrategy.DROP)
    }

    private var itemList: List<Any> = ArrayList()

    fun setItemList(itemList: List<Any>) {
        this.itemList = itemList
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder =
            when (viewType) {
                0 -> {
                    val view = parent.inflate(R.layout.shop_header)
                    StableHeaderViewHolder(view)
                }
                1 -> {
                    val view = parent.inflate(R.layout.customization_section_header)
                    SectionViewHolder(view)
                }
                2 -> {
                    val view = parent.inflate(R.layout.pet_overview_item)
                    StableViewHolder(view)
                }
                else -> {
                    val view = parent.inflate(R.layout.mount_overview_item)
                    StableViewHolder(view)
                }
            }
    
    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val obj = this.itemList[position]
        when {
            obj == "header" -> {
                (holder as? StableHeaderViewHolder)?.bind()
            }
            obj.javaClass == StableSection::class.java -> {
                if (obj == "Standard") {
                    val params = holder.itemView.layoutParams as GridLayoutManager.LayoutParams
                    params.height = 135
                    holder.itemView.layoutParams = params
                }
                (holder as? SectionViewHolder)?.bind(obj as StableSection)
            }
            else -> {
                (obj as? Animal)?.let { (holder as? StableViewHolder)?.bind(it) }

            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        var item = itemList[position]

        return if (item == "header") {
            0
        }
        else if (item.javaClass == StableSection::class.java) {
            1
        }
        else if (itemType == "pets") {
            2
        }
        else {
            3
        }
    }

    override fun getItemCount(): Int = itemList.size

    internal class StableHeaderViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {

        private val npcBannerView: NPCBannerView by bindView(itemView, R.id.npcBannerView)
        private val namePlate: TextView by bindView(itemView, R.id.namePlate)
        private val descriptionView: TextView by bindView(itemView, R.id.descriptionView)

        fun bind() {
            npcBannerView.identifier = "stable"
            namePlate.setText(R.string.stable_owner)
            descriptionView.visibility = View.GONE
        }
    }
    
    internal inner class StableViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private var animal: Animal? = null

        private val imageView: SimpleDraweeView by bindView(itemView, R.id.imageView)
        private val titleView: TextView by bindView(itemView, R.id.titleTextView)
        private val ownedTextView: TextView by bindView(itemView, R.id.ownedTextView)

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(item: Animal) {
            this.animal = item
            val isIndividualAnimal = item.type == "special" || animal?.type == "wacky"
            titleView.text = if (isIndividualAnimal) {
                item.text
            } else {
                item.animal
            }
            ownedTextView.visibility = View.VISIBLE
            this.imageView.alpha = 1.0f
            this.titleView.alpha = 1.0f
            this.ownedTextView.alpha = 1.0f

            val imageName = if (itemType == "pets") {
                if (isIndividualAnimal) {
                    "social_Pet-" + animal?.key
                } else {
                    "Pet_Egg_" + item.animal
                }
            } else {
                "Mount_Icon_" + item.key
            }

            context?.let {
                val owned = item.numberOwned
                val totalNum = item.totalNumber

                this.ownedTextView.text = context?.getString(R.string.pet_ownership_fraction, owned, totalNum)
                this.ownedTextView.background = context?.getDrawable(R.drawable.layout_rounded_bg_shopitem_price)

                this.ownedTextView.setTextColor(ContextCompat.getColor(it, R.color.gray_200) )

                ownedTextView.visibility = if (isIndividualAnimal) View.GONE else View.VISIBLE
                imageView.background = null
                val numberOwned = item.numberOwned == 0
                DataBindingUtils.loadImage(imageName) {bitmap ->
                    val drawable = if (isIndividualAnimal) {
                        BitmapDrawable(context?.resources, if (numberOwned) bitmap.extractAlpha() else bitmap)
                    } else {
                        BitmapDrawable(context?.resources, bitmap)
                    }
                    Observable.just(drawable)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(Consumer {
                                imageView.background = drawable
                            }, RxErrorHandler.handleEmptyError())
                }
                if (item.numberOwned <= 0) {
                    this.imageView.alpha = 0.2f
                    this.titleView.alpha = 0.2f
                    this.ownedTextView.alpha = 0.2f
                }

                if (item.numberOwned == item.totalNumber) {
                    this.ownedTextView.background = context?.getDrawable(R.drawable.layout_rounded_bg_animalitem_complete)
                    this.ownedTextView.setTextColor(ContextCompat.getColor(it, R.color.white))
                }
            }

        }

        override fun onClick(v: View) {
            val animal = this.animal
            if (animal != null) {
                if (animal.type == "special" || animal.type == "wacky") {
                    if (animal.numberOwned == 0) return
                    val context = context ?: return
                    val menu = BottomSheetMenu(context)
                    menu.setTitle(animal.text)
                    menu.addMenuItem(BottomSheetMenuItem(itemView.resources.getString(R.string.equip)))
                    menu.setSelectionRunnable {
                        animal.let {
                            equipEvents.onNext(it.key)
                        }
                    }
                    menu.show()
                    return
                }
                val color = if (animal.type == "special") animal.color else null
                if (animal.numberOwned > 0) {
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
