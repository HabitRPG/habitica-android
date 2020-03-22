package com.habitrpg.android.habitica.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.CustomizationGridItemBinding
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.inventory.CustomizationSet
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import java.util.*

class CustomizationEquipmentRecyclerViewAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var gemBalance: Int = 0
    var equipmentList
            : MutableList<Equipment> = ArrayList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    var activeEquipment: String? = null
    set(value) {
        field = value
        this.notifyDataSetChanged()
    }

    private val selectCustomizationEvents = PublishSubject.create<Equipment>()
    private val unlockCustomizationEvents = PublishSubject.create<Equipment>()
    private val unlockSetEvents = PublishSubject.create<CustomizationSet>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        val viewID: Int = R.layout.customization_grid_item

        val view = LayoutInflater.from(parent.context).inflate(viewID, parent, false)
        return EquipmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        (holder as EquipmentViewHolder).bind(equipmentList[position])
    }

    override fun getItemCount(): Int {
        return equipmentList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (this.equipmentList[position].javaClass == CustomizationSet::class.java) {
            0
        } else {
            1
        }
    }

    fun setEquipment(newEquipmentList: List<Equipment>) {
        this.equipmentList = newEquipmentList.toMutableList()
        this.notifyDataSetChanged()
    }

    fun getSelectCustomizationEvents(): Flowable<Equipment> {
        return selectCustomizationEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getUnlockCustomizationEvents(): Flowable<Equipment> {
        return unlockCustomizationEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getUnlockSetEvents(): Flowable<CustomizationSet> {
        return unlockSetEvents.toFlowable(BackpressureStrategy.DROP)
    }

    internal inner class EquipmentViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val binding = CustomizationGridItemBinding.bind(itemView)
        var equipment: Equipment? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(equipment: Equipment) {
            this.equipment = equipment
            DataBindingUtils.loadImage(binding.imageView, "shop_" + this.equipment?.key)
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

            if (activeEquipment == equipment.key) {
                binding.wrapper.background = itemView.context.getDrawable(R.drawable.layout_rounded_bg_gray_700_brand_border)
            } else {
                binding.wrapper.background = itemView.context.getDrawable(R.drawable.layout_rounded_bg_gray_700)
            }
        }

        override fun onClick(v: View) {
            if (equipment?.owned != true && (equipment?.value ?: 0.0) > 0.0) {
                val dialogContent = LayoutInflater.from(itemView.context).inflate(R.layout.dialog_purchase_customization, null) as LinearLayout

                val imageView = dialogContent.findViewById<SimpleDraweeView>(R.id.imageView)
                DataBindingUtils.loadImage(imageView, "shop_" + this.equipment?.key)

                val priceLabel = dialogContent.findViewById<TextView>(R.id.priceLabel)
                priceLabel.text = if (equipment?.gearSet == "animal") {
                    2.0
                } else {
                    equipment?.value ?: 0
                }.toString()

                (dialogContent.findViewById<View>(R.id.gem_icon) as? ImageView)?.setImageBitmap(HabiticaIconsHelper.imageOfGem())

                val dialog = HabiticaAlertDialog(itemView.context)
                dialog.addButton(R.string.purchase_button, true) { _, _ ->
                    if (equipment?.value ?: 0.0 > gemBalance) {
                        MainNavigationController.navigate(R.id.gemPurchaseActivity, bundleOf(Pair("openSubscription", false)))
                        return@addButton
                    }

                    equipment?.let {
                        unlockCustomizationEvents.onNext(it)
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
                selectCustomizationEvents.onNext(it)
            }
        }
    }
}
