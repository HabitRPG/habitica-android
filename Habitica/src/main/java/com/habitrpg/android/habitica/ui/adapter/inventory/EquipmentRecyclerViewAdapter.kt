package com.habitrpg.android.habitica.ui.adapter.inventory

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.GearListItemBinding
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class EquipmentRecyclerViewAdapter(data: OrderedRealmCollection<Equipment>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<Equipment, EquipmentRecyclerViewAdapter.GearViewHolder>(data, autoUpdate) {

    var equippedGear: String? = null
    var isCostume: Boolean? = null
    var type: String? = null

    val equipEvents: PublishSubject<String> = PublishSubject.create()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GearViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gear_list_item, parent, false)
        return GearViewHolder(view)
    }

    override fun onBindViewHolder(holder: GearViewHolder, position: Int) {
        data?.let {
            holder.bind(it[position])
        }
    }

    inner class GearViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val binding = GearListItemBinding.bind(itemView)

        var gear: Equipment? = null
        var context: Context = itemView.context

        init {
            context = itemView.context
            binding.twoHandedView.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfTwoHandedIcon()), null, null, null)
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
            binding.gearText.text = this.gear?.text
            binding.gearNotes.text = this.gear?.notes

            if (gear.key == equippedGear) {
                binding.equippedIndicator.visibility = View.VISIBLE
                binding.gearContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.brand_700))
                binding.gearIconBackgroundView .background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_content)
            } else {
                binding.equippedIndicator.visibility = View.GONE
                binding.gearContainer.setBackgroundResource(R.drawable.selection_highlight)
                binding.gearIconBackgroundView.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_window)
            }
            binding.twoHandedView.visibility = if (gear.twoHanded) View.VISIBLE else View.GONE
            DataBindingUtils.loadImage(binding.gearImage, "shop_"+gear.key)

            set(binding.strLabel, binding.strValue, gear.str)
            set(binding.conLabel, binding.conValue, gear.con)
            set(binding.intLabel, binding.intValue, gear._int)
            set(binding.perLabel, binding.perValue, gear.per)
        }

        private fun set(label: TextView, valueTextView: TextView, value: Int) {
            if (value > 0) {
                label.visibility = View.VISIBLE
                valueTextView.visibility = View.VISIBLE
                @SuppressLint("SetTextI18n")
                valueTextView.text = "+$value"
            } else {
                label.visibility = View.GONE
                valueTextView.visibility = View.GONE
            }
        }
    }
}
