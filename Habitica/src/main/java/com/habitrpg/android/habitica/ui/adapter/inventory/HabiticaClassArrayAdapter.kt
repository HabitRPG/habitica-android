package com.habitrpg.android.habitica.ui.adapter.inventory

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper


class HabiticaClassArrayAdapter(context: Context, resource: Int, objects: List<CharSequence>) : ArrayAdapter<CharSequence>(context, resource, R.id.textView, objects) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View =
            createView(position, convertView ?: parent?.inflate(R.layout.class_spinner_dropdown_item, false))

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View =
            createView(position, convertView ?: parent?.inflate(R.layout.class_spinner_dropdown_item_selected, false))

    private fun createView(position: Int, row: View?): View {
        val textView: TextView? = row?.findViewById(R.id.textView)
        val imageView: ImageView? = row?.findViewById(R.id.classIconView)

        when (getItem(position)) {
            Stats.WARRIOR -> {
                textView?.text = context.getString(R.string.warrior)
                textView?.setTextColor(ContextCompat.getColor(context, R.color.red_10))
                imageView?.setImageBitmap(HabiticaIconsHelper.imageOfWarriorLightBg())
            }
            Stats.MAGE -> {
                textView?.text = context.getString(R.string.mage)
                textView?.setTextColor(ContextCompat.getColor(context, R.color.blue_10))
                imageView?.setImageBitmap(HabiticaIconsHelper.imageOfMageLightBg())
            }
            Stats.HEALER -> {
                textView?.text = context.getString(R.string.healer)
                textView?.setTextColor(ContextCompat.getColor(context, R.color.yellow_10))
                imageView?.setImageBitmap(HabiticaIconsHelper.imageOfHealerLightBg())
            }
            Stats.ROGUE -> {
                textView?.text = context.getString(R.string.rogue)
                textView?.setTextColor(ContextCompat.getColor(context, R.color.brand_300))
                imageView?.setImageBitmap(HabiticaIconsHelper.imageOfRogueLightBg())
            }
            else -> {
                textView?.text = context.getString(R.string.classless)
                textView?.setTextColor(ContextCompat.getColor(context, R.color.textColorLight))
                imageView?.setImageBitmap(null)
            }
        }
        return row ?: View(context)
    }
}