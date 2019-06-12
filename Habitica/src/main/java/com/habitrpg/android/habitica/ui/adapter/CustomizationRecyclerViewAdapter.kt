package com.habitrpg.android.habitica.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.helpers.MainNavigationController
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.models.inventory.CustomizationSet
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import java.util.*

class CustomizationRecyclerViewAdapter : androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder>() {

    var userSize: String? = null
    var hairColor: String? = null
    var gemBalance: Int = 0
    var customizationList: MutableList<Any> = ArrayList()
    set(value) {
        field = value
        notifyDataSetChanged()
    }
    var additionalSetItems: List<Customization> = ArrayList()
    var activeCustomization: String? = null
    set(value) {
        field = value
        this.notifyDataSetChanged()
    }

    private val selectCustomizationEvents = PublishSubject.create<Customization>()
    private val unlockCustomizationEvents = PublishSubject.create<Customization>()
    private val unlockSetEvents = PublishSubject.create<CustomizationSet>()

    fun updateOwnership(ownedCustomizations: List<String>) {
        for ((position, obj) in customizationList.withIndex()) {
            if (obj.javaClass == Customization::class.java) {
                val customization = obj as? Customization ?: return
                if (customization.purchased != ownedCustomizations.contains(customization.id)) {
                    customization.purchased = ownedCustomizations.contains(customization.id)
                    notifyItemChanged(position)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder {
        return if (viewType == 0) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.customization_section_header, parent, false)
            SectionViewHolder(view)
        } else {
            val viewID: Int = if (viewType == 1) {
                R.layout.customization_grid_item
            } else {
                R.layout.customization_grid_background_item
            }

            val view = LayoutInflater.from(parent.context).inflate(viewID, parent, false)
            CustomizationViewHolder(view)
        }

    }

    override fun onBindViewHolder(holder: androidx.recyclerview.widget.RecyclerView.ViewHolder, position: Int) {
        val obj = customizationList[position]
        if (obj.javaClass == CustomizationSet::class.java) {
            (holder as SectionViewHolder).bind(obj as CustomizationSet)
        } else {
            (holder as CustomizationViewHolder).bind(customizationList[position] as Customization)

        }
    }

    override fun getItemCount(): Int {
        return customizationList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (this.customizationList[position].javaClass == CustomizationSet::class.java) {
            0
        } else {
            val customization = customizationList[position] as Customization
            if (customization.type == "background") {
                2
            } else 1
        }
    }

    fun setCustomizations(newCustomizationList: List<Customization>) {
        this.customizationList = ArrayList()
        var lastSet = CustomizationSet()
        for (customization in newCustomizationList) {
            if (customization.customizationSet != null && customization.customizationSet != lastSet.identifier) {
                val set = CustomizationSet()
                set.identifier = customization.customizationSet
                set.text = customization.customizationSetName
                set.price = customization.setPrice
                set.hasPurchasable = !customization.isUsable
                lastSet = set
                customizationList.add(set)
            }
            customizationList.add(customization)
            if (!customization.isUsable && !lastSet.hasPurchasable) {
                lastSet.hasPurchasable = true
            }
        }
        this.notifyDataSetChanged()
    }

    fun getSelectCustomizationEvents(): Flowable<Customization> {
        return selectCustomizationEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getUnlockCustomizationEvents(): Flowable<Customization> {
        return unlockCustomizationEvents.toFlowable(BackpressureStrategy.DROP)
    }

    fun getUnlockSetEvents(): Flowable<CustomizationSet> {
        return unlockSetEvents.toFlowable(BackpressureStrategy.DROP)
    }

    internal inner class CustomizationViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val cardView: androidx.cardview.widget.CardView by bindView(itemView, R.id.card_view)
        private val linearLayout: RelativeLayout by bindView(itemView, R.id.linearLayout)
        private val imageView: SimpleDraweeView by bindView(itemView, R.id.imageView)
        private val purchaseOverlay: View by bindView(itemView, R.id.purchaseOverlay)

        var customization: Customization? = null

        init {
            linearLayout.setOnClickListener(this)
        }

        fun bind(customization: Customization) {
            this.customization = customization


            DataBindingUtils.loadImage(this.imageView, customization.getImageName(userSize, hairColor))
            cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, android.R.color.white))
            if (customization.isUsable) {
                imageView.alpha = 1.0f
                purchaseOverlay.alpha = 0.0f
                if (customization.identifier == activeCustomization) {
                    cardView.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.brand_500))
                }
            } else {
                imageView.alpha = 0.3f
                purchaseOverlay.alpha = 0.8f
            }
        }

        override fun onClick(v: View) {
            if (customization?.isUsable == false) {

                val dialogContent = LayoutInflater.from(itemView.context).inflate(R.layout.dialog_purchase_customization, null) as LinearLayout

                val imageView = dialogContent.findViewById<SimpleDraweeView>(R.id.imageView)
                DataBindingUtils.loadImage(imageView, customization?.getImageName(userSize, hairColor))

                val priceLabel = dialogContent.findViewById<TextView>(R.id.priceLabel)
                priceLabel.text = customization?.price.toString()

                (dialogContent.findViewById<View>(R.id.gem_icon) as ImageView).setImageBitmap(HabiticaIconsHelper.imageOfGem())

                val dialog = HabiticaAlertDialog(itemView.context)
                dialog.addButton(R.string.purchase_button, true) { _, _ ->
                            if (customization?.price ?: 0 > gemBalance) {
                                MainNavigationController.navigate(R.id.gemPurchaseActivity)
                                return@addButton
                            }

                            customization?.let {
                                unlockCustomizationEvents.onNext(it)
                            }
                        }
                dialog.setTitle(itemView.context.getString(R.string.purchase_customization))
                dialog.setAdditionalContentView(dialogContent)
                dialog.addButton(R.string.reward_dialog_dismiss, false)
                dialog.show()
                return
            }

            if (customization?.identifier == activeCustomization) {
                return
            }

            customization?.let {
                selectCustomizationEvents.onNext(it)
            }
        }
    }

    internal inner class SectionViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val label: TextView by bindView(itemView, R.id.label)
        private val purchaseSetButton: Button by bindView(itemView, R.id.purchaseSetButton)
        var context: Context = itemView.context
        private var set: CustomizationSet? = null

        init {
            purchaseSetButton.setOnClickListener(this)
        }

        fun bind(set: CustomizationSet) {
            this.set = set
            this.label.text = set.text
            if (set.hasPurchasable) {
                this.purchaseSetButton.visibility = View.VISIBLE
                this.purchaseSetButton.text = context.getString(R.string.purchase_set_button, set.price)
            } else {
                this.purchaseSetButton.visibility = View.GONE
            }
        }

        override fun onClick(v: View) {
            val dialogContent = LayoutInflater.from(context).inflate(R.layout.dialog_purchase_customization, null) as LinearLayout

            val priceLabel = dialogContent.findViewById<TextView>(R.id.priceLabel)
            priceLabel.text = set?.price.toString()

            val dialog = HabiticaAlertDialog(context)
            dialog.addButton(R.string.purchase_button, true) { _, _ ->
                        if (set?.price ?: 0 > gemBalance) {
                            MainNavigationController.navigate(R.id.gemPurchaseActivity)
                            return@addButton
                        }
                        set?.customizations = ArrayList()
                        customizationList
                                .filter { Customization::class.java.isAssignableFrom(it.javaClass) }
                                .map { it as Customization }
                                .filter { !it.isUsable && it.customizationSet != null && it.customizationSet == set?.identifier }
                                .forEach { set?.customizations?.add(it) }
                        if (additionalSetItems.isNotEmpty()) {
                            additionalSetItems
                                    .filter { !it.isUsable && it.customizationSet != null && it.customizationSet == set?.identifier }
                                    .forEach { set?.customizations?.add(it) }
                        }
                        set?.let {
                            unlockSetEvents.onNext(it)
                        }
                    }
            dialog.setTitle(context.getString(R.string.purchase_set_title, set?.text))
            dialog.setAdditionalContentView(dialogContent)
            dialog.addButton(R.string.reward_dialog_dismiss, false)
            dialog.show()
        }
    }
}
