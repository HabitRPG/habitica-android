package com.habitrpg.android.habitica.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.CustomizationGridItemBinding
import com.habitrpg.android.habitica.databinding.DialogPurchaseCustomizationBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.inventory.CustomizationSet
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.views.PixelArtView

class CustomizationEquipmentRecyclerViewAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var gemBalance: Int = 0
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val viewID: Int = R.layout.customization_grid_item

        val view = LayoutInflater.from(parent.context).inflate(viewID, parent, false)
        return EquipmentViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        position: Int
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

    internal inner class EquipmentViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val binding = CustomizationGridItemBinding.bind(itemView)
        var equipment: Equipment? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(equipment: Equipment) {
            this.equipment = equipment
            binding.imageView.loadImage("shop_" + this.equipment?.key)
            if (equipment.owned == true || equipment.value == 0.0) {
                binding.buyButton.visibility = View.GONE
            } else {
                binding.buyButton.visibility = View.VISIBLE
                binding.priceLabel.currency = "gems"
                binding.priceLabel.value = if (equipment.gearSet == "animal") {
                    2.0
                } else {
                    equipment.value
                }
            }

            if (activeEquipment == equipment.key || (activeEquipment?.contains("base_0") == true && equipment.key?.isNotBlank() != true)) {
                binding.wrapper.background = ContextCompat.getDrawable(itemView.context, R.drawable.layout_rounded_bg_window_tint_border)
            } else {
                binding.wrapper.background = ContextCompat.getDrawable(itemView.context, R.drawable.layout_rounded_bg_window)
            }
        }

        override fun onClick(v: View) {
            if (equipment?.owned != true && (equipment?.value ?: 0.0) > 0.0) {
                val dialogContent = LinearLayout(itemView.context)
                DialogPurchaseCustomizationBinding.inflate(LayoutInflater.from(itemView.context), dialogContent)

                val imageView = dialogContent.findViewById<PixelArtView>(R.id.imageView)
                imageView.loadImage("shop_" + this.equipment?.key)

                val priceLabel = dialogContent.findViewById<TextView>(R.id.priceLabel)
                priceLabel.text = if (equipment?.gearSet == "animal") {
                    2.0
                } else {
                    equipment?.value ?: 0
                }.toString()

                (dialogContent.findViewById<View>(R.id.gem_icon) as? ImageView)?.setImageBitmap(
                    HabiticaIconsHelper.imageOfGem())

                val dialog = HabiticaAlertDialog(itemView.context)
                dialog.addButton(R.string.purchase_button, true) { _, _ ->
                    if ((equipment?.value ?: 0.0) > gemBalance) {
                        MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", false)))
                        return@addButton
                    }

                    equipment?.let {
                        onUnlock?.invoke(it)
                    }
                }
                dialog.setTitle(R.string.purchase_customization)
                dialog.setAdditionalContentView(dialogContent)
                dialog.addButton(R.string.reward_dialog_dismiss, false)
                dialog.show()
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