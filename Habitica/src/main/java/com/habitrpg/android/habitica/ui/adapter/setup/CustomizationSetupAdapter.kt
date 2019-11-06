package com.habitrpg.android.habitica.ui.adapter.setup

import android.content.Context
import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.android.habitica.ui.helpers.bindView
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject

internal class CustomizationSetupAdapter : RecyclerView.Adapter<CustomizationSetupAdapter.CustomizationViewHolder>() {

    var userSize: String? = null
    var user: User? = null
    private var customizationList: List<SetupCustomization> = emptyList()

    private val equipGearEventSubject = PublishSubject.create<String>()
    val equipGearEvents = equipGearEventSubject.toFlowable(BackpressureStrategy.DROP)
    private val updateUserEventsSubject = PublishSubject.create<HashMap<String, Any>>()
    val updateUserEvents = updateUserEventsSubject.toFlowable(BackpressureStrategy.DROP)

    fun setCustomizationList(newCustomizationList: List<SetupCustomization>) {
        this.customizationList = newCustomizationList
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomizationViewHolder {
        return CustomizationViewHolder(parent.inflate(R.layout.setup_customization_item))
    }

    override fun onBindViewHolder(holder: CustomizationViewHolder, position: Int) {
        holder.bind(customizationList[position])
    }

    override fun getItemCount(): Int {
        return customizationList.size
    }

    private fun isCustomizationActive(customization: SetupCustomization): Boolean {
        val prefs = this.user?.preferences ?: return false
        return when (customization.category) {
            SetupCustomizationRepository.CATEGORY_BODY -> {
                when (customization.subcategory) {
                    SetupCustomizationRepository.SUBCATEGORY_SIZE -> customization.key == prefs.size
                    SetupCustomizationRepository.SUBCATEGORY_SHIRT -> customization.key == prefs.shirt
                    else -> false
                }
            }
            SetupCustomizationRepository.CATEGORY_SKIN -> customization.key == prefs.skin
            SetupCustomizationRepository.CATEGORY_HAIR -> {
                when (customization.subcategory) {
                    SetupCustomizationRepository.SUBCATEGORY_BANGS -> Integer.parseInt(customization.key) == prefs.hair?.bangs
                    SetupCustomizationRepository.SUBCATEGORY_PONYTAIL -> Integer.parseInt(customization.key) == prefs.hair?.base
                    SetupCustomizationRepository.SUBCATEGORY_COLOR -> customization.key == prefs.hair?.color
                    SetupCustomizationRepository.SUBCATEGORY_FLOWER -> Integer.parseInt(customization.key) == prefs.hair?.flower
                    else -> false
                }
            }
            SetupCustomizationRepository.CATEGORY_EXTRAS -> {
                when (customization.subcategory) {
                    SetupCustomizationRepository.SUBCATEGORY_GLASSES -> customization.key == this.user?.items?.gear?.equipped?.eyeWear || "eyewear_base_0" == this.user?.items?.gear?.equipped?.eyeWear && customization.key.isEmpty()
                    SetupCustomizationRepository.SUBCATEGORY_FLOWER -> Integer.parseInt(customization.key) == prefs.hair?.flower
                    SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR -> "chair_" + customization.key == prefs.chair || customization.key == prefs.chair || customization.key == "none" && prefs.chair == null
                    else -> false
                }
            }
            else -> false
        }
    }

    internal inner class CustomizationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val imageView: ImageView by bindView(R.id.imageView)
        private val textView: TextView by bindView(R.id.textView)

        var customization: SetupCustomization? = null

        var context: Context

        init {
            itemView.setOnClickListener(this)

            context = itemView.context
        }

        fun bind(customization: SetupCustomization) {
            this.customization = customization

            when {
                customization.drawableId != null -> imageView.setImageResource(customization.drawableId ?: 0)
                customization.colorId != null -> {
                    val drawable = ContextCompat.getDrawable(context, R.drawable.setup_customization_circle)
                    drawable?.setColorFilter(ContextCompat.getColor(context, customization.colorId ?: 0), PorterDuff.Mode.MULTIPLY)
                    imageView.setImageDrawable(drawable)
                }
                else -> imageView.setImageDrawable(null)
            }
            textView.text = customization.text
            if ("0" != customization.key && "flower" == customization.subcategory) {
                if (isCustomizationActive(customization)) {
                    imageView.setBackgroundResource(R.drawable.setup_customization_flower_bg_selected)
                } else {
                    imageView.setBackgroundResource(R.drawable.setup_customization_flower_bg)
                }
            } else {
                if (isCustomizationActive(customization)) {
                    imageView.setBackgroundResource(R.drawable.setup_customization_bg_selected)
                    textView.setTextColor(ContextCompat.getColor(context, R.color.white))
                } else {
                    imageView.setBackgroundResource(R.drawable.setup_customization_bg)
                    textView.setTextColor(ContextCompat.getColor(context, R.color.white_50_alpha))
                }
            }
        }

        override fun onClick(v: View) {
            customization?.let { selectedCustomization ->
                if (selectedCustomization.path == "glasses") {
                    val key = if (selectedCustomization.key.isEmpty()) {
                        user?.items?.gear?.equipped?.eyeWear
                    } else {
                        selectedCustomization.key
                    }
                    key?.let { equipGearEventSubject.onNext(it) }
                } else {
                    val updateData = HashMap<String, Any>()
                    val updatePath = "preferences." + selectedCustomization.path
                    updateData[updatePath] = selectedCustomization.key
                    updateUserEventsSubject.onNext(updateData)
                }
            }

        }
    }
}
