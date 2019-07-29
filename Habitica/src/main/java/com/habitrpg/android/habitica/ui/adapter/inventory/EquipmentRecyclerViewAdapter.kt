package com.habitrpg.android.habitica.ui.adapter.inventory

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.ui.helpers.bindView
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

    val equipEvents: PublishSubject<String> = PublishSubject.create<String>()

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

        private val gearContainer: View by bindView(itemView, R.id.gear_container)
        private val gearNameTextView: TextView by bindView(itemView, R.id.gear_text)
        private val gearNotesTextView: TextView by bindView(itemView, R.id.gear_notes)
        private val imageView: SimpleDraweeView by bindView(itemView, R.id.gear_image)
        private val imageViewWrapper: FrameLayout by bindView(itemView, R.id.gear_icon_background_view)
        private val equippedIndicator: View by bindView(itemView, R.id.equippedIndicator)
        private val twoHandedView: TextView by bindView(R.id.two_handed_view)
        private val strLabel: TextView by bindView(R.id.str_label)
        private val strValue: TextView by bindView(R.id.str_value)
        private val conLabel: TextView by bindView(R.id.con_label)
        private val conValue: TextView by bindView(R.id.con_value)
        private val intLabel: TextView by bindView(R.id.int_label)
        private val intValue: TextView by bindView(R.id.int_value)
        private val perLabel: TextView by bindView(R.id.per_label)
        private val perValue: TextView by bindView(R.id.per_value)

        var gear: Equipment? = null
        var context: Context = itemView.context

        init {
            context = itemView.context
            twoHandedView.setCompoundDrawablesWithIntrinsicBounds(BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfTwoHandedIcon()), null, null, null)
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
                imageViewWrapper.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_white)
            } else {
                this.equippedIndicator.visibility = View.GONE
                this.gearContainer.setBackgroundResource(R.drawable.selection_highlight)
                imageViewWrapper.background = ContextCompat.getDrawable(context, R.drawable.layout_rounded_bg_gray_700)
            }
            twoHandedView.visibility = if (gear.twoHanded) View.VISIBLE else View.GONE
            DataBindingUtils.loadImage(imageView, "shop_"+gear.key)

            set(strLabel, strValue, gear.str)
            set(conLabel, conValue, gear.con)
            set(intLabel, intValue, gear._int)
            set(perLabel, perValue, gear.per)
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
