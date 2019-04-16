package com.habitrpg.android.habitica.ui.adapter.inventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.inventory.Animal
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.fragments.inventory.stable.StableFragmentDirections
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder

class StableRecyclerAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var itemType: String? = null
    var activity: MainActivity? = null
    private var itemList: List<Any> = ArrayList()

    fun setItemList(itemList: List<Any>) {
        this.itemList = itemList
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder =
            when (viewType) {
                0 -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.customization_section_header, parent, false)
                    SectionViewHolder(view)
                }
                else -> {
                    val view = LayoutInflater.from(parent.context).inflate(R.layout.animal_overview_item, parent, false)
                    StableViewHolder(view)
                }
            }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val obj = this.itemList[position]
        if (obj.javaClass == String::class.java) {
            (holder as? SectionViewHolder)?.bind(obj as? String ?: "")
        } else {
            (obj as? Animal).notNull { (holder as? StableViewHolder)?.bind(it) }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (this.itemList[position].javaClass == String::class.java) {
            0
        } else {
            1
        }
    }

    override fun getItemCount(): Int = itemList.size

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
            titleView.text = item.animal
            ownedTextView.visibility = View.VISIBLE
            this.imageView.alpha = 1.0f
            if (item.numberOwned > 0) {
                this.ownedTextView.text = animal?.numberOwned?.toString()
                if (itemType == "pets") {
                    DataBindingUtils.loadImage(this.imageView, "Pet-" + item.key)
                } else {
                    DataBindingUtils.loadImage(this.imageView, "Mount_Icon_" + item.key)
                }
            } else {
                ownedTextView.visibility = View.GONE
                DataBindingUtils.loadImage(this.imageView, "PixelPaw")
                this.imageView.alpha = 0.4f
            }
        }

        override fun onClick(v: View) {
            val animal = this.animal
            if (animal != null) {
                if (animal.numberOwned > 0) {
                    if (itemType == "pets") {
                        MainNavigationController.navigate(StableFragmentDirections.openPetDetail(animal.animal, animal.type))
                    } else {
                        MainNavigationController.navigate(StableFragmentDirections.openMountDetail(animal.animal, animal.type))
                    }
                }
            }
        }
    }
}
