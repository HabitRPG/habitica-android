package com.habitrpg.android.habitica.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.CustomizationGridItemBinding
import com.habitrpg.android.habitica.models.inventory.CustomizationSet
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.models.shops.ShopItem
import com.habitrpg.common.habitica.extensionsCommon.loadImage

class CustomizationEquipmentRecyclerViewAdapter :
    androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {
    var gemBalance: Int? = null
    var equipmentList: MutableList<Equipment> =
        ArrayList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var activeEquipment: String? = null
        set(value) {
            field = value
            this.notifyDataSetChanged()
        }

    var onSelect: ((Equipment) -> Unit)? = null
    var onUnlock: ((Equipment) -> Unit)? = null
    var onShowPurchaseDialog: ((ShopItem) -> Unit)? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val viewID: Int = R.layout.customization_grid_item

        val view = LayoutInflater.from(parent.context).inflate(viewID, parent, false)
        return EquipmentViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        position: Int,
    ) {
        (holder as EquipmentViewHolder).bind(equipmentList[position])
    }

    override fun getItemCount(): Int {
        return equipmentList.size
    }

    override fun getItemViewType(position: Int): Int {
        if (equipmentList.size <= position) return 0
        return if (this.equipmentList[position].javaClass == CustomizationSet::class.java) {
            0
        } else {
            1
        }
    }

    fun setEquipment(newEquipmentList: List<Equipment>) {
        this.equipmentList = newEquipmentList.toMutableList()
        val emptyEquipment = Equipment()
        equipmentList.add(0, emptyEquipment)
        this.notifyDataSetChanged()
    }

    internal inner class EquipmentViewHolder(itemView: View) :
        androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val binding = CustomizationGridItemBinding.bind(itemView)
        var equipment: Equipment? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(equipment: Equipment) {
            this.equipment = equipment
            if (equipment.key?.isNotBlank() == true) {
                binding.imageView.loadImage("shop_" + equipment.key)
            } else {
                binding.imageView.bitmap = null
                binding.imageView.tag = null
                binding.imageView.setImageResource(R.drawable.empty_slot)
            }
            if (equipment.owned == true || equipment.value == 0.0) {
                binding.buyButton.visibility = View.GONE
            } else {
                binding.buyButton.visibility = View.VISIBLE
                binding.priceLabel.currency = "gems"
                binding.priceLabel.value =
                    if (equipment.gearSet == "animal") {
                        2.0
                    } else {
                        equipment.value
                    }
            }

            if (activeEquipment == equipment.key || (activeEquipment?.contains("base_0") == true && equipment.key?.isNotBlank() != true)) {
                binding.wrapper.background =
                    ContextCompat.getDrawable(
                        itemView.context,
                        R.drawable.layout_rounded_bg_window_tint_border,
                    )
            } else {
                binding.wrapper.background =
                    ContextCompat.getDrawable(itemView.context, R.drawable.layout_rounded_bg_window)
            }
        }

        override fun onClick(v: View) {
            val itemValue =
                if (equipment?.gearSet == "animal") {
                    2.0
                } else {
                    equipment?.value
                }
            if (equipment?.owned != true && (itemValue ?: 0.0) > 0.0) {
                onShowPurchaseDialog?.invoke(
                    ShopItem.fromAnimalEquipment(
                        equipment
                    ),
                )
                return
            }

            if (equipment?.key == activeEquipment) {
                return
            }

            equipment?.let {
                onSelect?.invoke(it)
            }
        }
    }
}
