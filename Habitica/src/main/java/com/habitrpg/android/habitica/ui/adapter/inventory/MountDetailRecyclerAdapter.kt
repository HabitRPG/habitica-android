package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.inventory.Mount
import com.habitrpg.android.habitica.models.inventory.Pet
import com.habitrpg.android.habitica.models.user.OwnedMount
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenu
import com.habitrpg.android.habitica.ui.menu.BottomSheetMenuItem
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class MountDetailRecyclerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var itemType: String? = null
    var context: Context? = null
    private var ownedMounts: Map<String, OwnedMount>? = null

    private val equipEvents = PublishSubject.create<String>()

    private var itemList: List<Any> = ArrayList()

    fun setItemList(itemList: List<Any>) {
        this.itemList = itemList
        this.notifyDataSetChanged()
    }

    fun getEquipFlowable(): Flowable<String> {
        return equipEvents.toFlowable(BackpressureStrategy.DROP)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder =
            when (viewType) {
                0 -> {
                    val view = parent.inflate(R.layout.shop_header)
                    StableRecyclerAdapter.StableHeaderViewHolder(view)
                }
                1 -> {
                    val view = parent.inflate(R.layout.customization_section_header)
                    SectionViewHolder(view)
                }
                else -> {
                    MountViewHolder(parent.inflate(R.layout.mount_overview_item))
                }
            }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val obj = this.itemList[position]
        when {
            obj.javaClass == String::class.java -> {
                (holder as? SectionViewHolder)?.bind(obj as? String ?: "")
            }
            else -> {
                (obj as? Mount)?.let { (holder as? MountViewHolder)?.bind(it, ownedMounts?.get(it.key ?: "")) }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = itemList[position]

        return if (item.javaClass == String::class.java) {
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

    fun setOwnedMounts(ownedMounts: Map<String, OwnedMount>) {
        this.ownedMounts = ownedMounts
        notifyDataSetChanged()
    }

    inner class MountViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {
        var animal: Mount? = null
        private var ownedMount: OwnedMount? = null

        private val imageView: SimpleDraweeView by bindView(R.id.imageView)
        private val titleView: TextView by bindView(R.id.titleTextView)
        private val ownedTextView: TextView by bindView(R.id.ownedTextView)

        var resources: Resources = itemView.resources

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(item: Mount, ownedMount: OwnedMount?) {
            animal = item
            this.ownedMount = ownedMount
            titleView.text = when {
                item.color == "Veggie" -> context?.getString(R.string.garden)
                item.type == "special" ->item.text
                else -> item.color
            }
            ownedTextView.visibility = View.GONE
            val imageName = "Mount_Icon_" + itemType + "-" + item.color
            this.imageView.alpha = 1.0f
            if (ownedMount?.owned != true) {
                this.imageView.alpha = 0.1f
            }
            imageView.background = null
            val owned = ownedMount?.owned ?: false
            DataBindingUtils.loadImage(imageName) {
                val drawable = BitmapDrawable(context?.resources, if (owned) it else it.extractAlpha())

                Observable.just(drawable)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(Consumer {
                            imageView.background = drawable
                        }, RxErrorHandler.handleEmptyError())
            }
        }

        override fun onClick(v: View) {
            if (ownedMount?.owned != true) {
                return
            }
            val menu = BottomSheetMenu(itemView.context)
            menu.setTitle(animal?.text)
            menu.addMenuItem(BottomSheetMenuItem(resources.getString(R.string.equip)))
            menu.setSelectionRunnable {
                animal?.let { equipEvents.onNext(it.key) }
            }
            menu.show()
        }
    }
}
