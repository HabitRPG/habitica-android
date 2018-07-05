package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.bindView
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class EquipmentRecyclerViewAdapter(data: OrderedRealmCollection<Equipment>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<Equipment, EquipmentRecyclerViewAdapter.GearViewHolder>(data, autoUpdate) {

    var equippedGear: String? = null
    var isCostume: Boolean? = null
    var type: String? = null

    val equipEvents: PublishSubject<String> = PublishSubject.create<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GearViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gear_list_item, parent, false)
        return GearViewHolder(view)
    }

    override fun onBindViewHolder(holder: GearViewHolder, position: Int) {
        if (data != null) {
            holder.bind(data!![position])
        }
    }

    inner class GearViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val gearContainer: View by bindView(itemView, R.id.gear_container)
        private val gearNameTextView: TextView by bindView(itemView, R.id.gear_text)
        private val gearNotesTextView: TextView by bindView(itemView, R.id.gear_notes)
        private val imageView: SimpleDraweeView by bindView(itemView, R.id.gear_image)
        private val equippedIndicator: View by bindView(itemView, R.id.equippedIndicator)

        var gear: Equipment? = null
        var context: Context = itemView.context

        init {
            context = itemView.context
            itemView.setOnClickListener {
                val key = gear?.key
                if (key != null) {
                    equipEvents.onNext(key)
                    equippedGear = if (key == equippedGear) {
                        type + "_base_0"
                    } else {
                        key
                    }
                    notifyDataSetChanged()
                }
            }
        }

        fun bind(gear: Equipment) {
            this.gear = gear
            this.gearNameTextView.text = this.gear?.text
            this.gearNotesTextView.text = this.gear?.notes

            if (gear.key == equippedGear) {
                this.equippedIndicator.visibility = View.VISIBLE
                this.gearContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.brand_700))
            } else {
                this.equippedIndicator.visibility = View.GONE
                this.gearContainer.setBackgroundResource(R.drawable.selection_highlight)
            }
            DataBindingUtils.loadImage(imageView, "shop_"+gear.key)
        }
    }
}
