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
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.loadImage

class EquipmentRecyclerViewAdapter :
    BaseRecyclerViewAdapter<Equipment, EquipmentRecyclerViewAdapter.GearViewHolder>() {
    var equippedGear: String? = null
    var isCostume: Boolean? = null
    var type: String? = null

    var onEquip: ((String) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GearViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.gear_list_item, parent, false)
        return GearViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: GearViewHolder,
        position: Int
    ) {
        holder.bind(data[position])
    }

    inner class GearViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        private val binding = GearListItemBinding.bind(itemView)

        var gear: Equipment? = null
        var context: Context = itemView.context

        init {
            context = itemView.context
            binding.twoHandedView.setCompoundDrawablesWithIntrinsicBounds(
                BitmapDrawable(
                    context.resources,
                    HabiticaIconsHelper.imageOfTwoHandedIcon()
                ),
                null,
                null,
                null
            )
            itemView.setOnClickListener {
                val key = gear?.key
                if (key != null) {
                    onEquip?.invoke(key)
                    equippedGear =
                        if (key == equippedGear) {
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
                binding.gearContainer.setBackgroundColor(
                    ContextCompat.getColor(
                        context,
                        R.color.lightly_tinted_background
                    )
                )
                binding.gearIconBackgroundView.background =
                    ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_content)
            } else {
                binding.equippedIndicator.visibility = View.GONE
                binding.gearContainer.setBackgroundResource(R.drawable.selection_highlight)
                binding.gearIconBackgroundView.background =
                    ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_window)
            }
            binding.twoHandedView.visibility = if (gear.twoHanded) View.VISIBLE else View.GONE
            binding.gearImage.loadImage("shop_" + gear.key)

            set(binding.strLabel, binding.strValue, gear.str)
            set(binding.conLabel, binding.conValue, gear.con)
            set(binding.intLabel, binding.intValue, gear.intelligence)
            set(binding.perLabel, binding.perValue, gear.per)
        }

        private fun set(
            label: TextView,
            valueTextView: TextView,
            value: Int
        ) {
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
