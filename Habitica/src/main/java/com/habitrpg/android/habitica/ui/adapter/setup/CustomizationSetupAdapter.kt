package com.habitrpg.android.habitica.ui.adapter.setup

import android.graphics.PorterDuff
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.data.SetupCustomizationRepository
import com.habitrpg.android.habitica.databinding.SetupCustomizationItemBinding
import com.habitrpg.android.habitica.models.SetupCustomization
import com.habitrpg.android.habitica.models.user.User
import com.habitrpg.common.habitica.extensions.inflate
import com.habitrpg.common.habitica.extensions.setTintWith

internal class CustomizationSetupAdapter :
    RecyclerView.Adapter<CustomizationSetupAdapter.CustomizationViewHolder>() {
    var userSize: String? = null
    var user: User? = null
    private var customizationList: List<SetupCustomization> = emptyList()

    var onEquipGear: ((String) -> Unit)? = null
    var onUpdateUser: ((Map<String, Any>) -> Unit)? = null

    fun setCustomizationList(newCustomizationList: List<SetupCustomization>) {
        this.customizationList = newCustomizationList
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomizationViewHolder {
        return CustomizationViewHolder(parent.inflate(R.layout.setup_customization_item))
    }

    override fun onBindViewHolder(
        holder: CustomizationViewHolder,
        position: Int
    ) {
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
                    SetupCustomizationRepository.SUBCATEGORY_PONYTAIL ->
                        Integer.parseInt(
                            customization.key
                        ) == prefs.hair?.base

                    SetupCustomizationRepository.SUBCATEGORY_COLOR -> customization.key == prefs.hair?.color
                    SetupCustomizationRepository.SUBCATEGORY_FLOWER ->
                        Integer.parseInt(
                            customization.key
                        ) == prefs.hair?.flower

                    else -> false
                }
            }

            SetupCustomizationRepository.CATEGORY_EXTRAS -> {
                when (customization.subcategory) {
                    SetupCustomizationRepository.SUBCATEGORY_GLASSES -> customization.key == this.user?.items?.gear?.equipped?.eyeWear || "eyewear_base_0" == this.user?.items?.gear?.equipped?.eyeWear && customization.key.isEmpty()
                    SetupCustomizationRepository.SUBCATEGORY_FLOWER ->
                        Integer.parseInt(
                            customization.key
                        ) == prefs.hair?.flower

                    SetupCustomizationRepository.SUBCATEGORY_WHEELCHAIR -> "chair_" + customization.key == prefs.chair || customization.key == prefs.chair || customization.key == "none" && prefs.chair == null
                    else -> false
                }
            }

            else -> false
        }
    }

    internal inner class CustomizationViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val binding = SetupCustomizationItemBinding.bind(itemView)

        var customization: SetupCustomization? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(customization: SetupCustomization) {
            this.customization = customization

            when {
                customization.drawableId != null ->
                    binding.imageView.setImageResource(
                        customization.drawableId ?: 0
                    )

                customization.colorId != null -> {
                    val drawable =
                        ContextCompat.getDrawable(
                            itemView.context,
                            R.drawable.setup_customization_circle
                        )
                    drawable?.setTintWith(
                        ContextCompat.getColor(
                            itemView.context,
                            customization.colorId ?: 0
                        ),
                        PorterDuff.Mode.MULTIPLY
                    )
                    binding.imageView.setImageDrawable(drawable)
                }

                else -> binding.imageView.setImageDrawable(null)
            }
            binding.textView.text = customization.text
            if ("0" != customization.key && "flower" == customization.subcategory) {
                if (isCustomizationActive(customization)) {
                    binding.imageView.setBackgroundResource(R.drawable.setup_customization_flower_bg_selected)
                } else {
                    binding.imageView.setBackgroundResource(R.drawable.setup_customization_flower_bg)
                }
            } else {
                if (isCustomizationActive(customization)) {
                    binding.imageView.setBackgroundResource(R.drawable.setup_customization_bg_selected)
                    binding.textView.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.white
                        )
                    )
                } else {
                    binding.imageView.setBackgroundResource(R.drawable.setup_customization_bg)
                    binding.textView.setTextColor(
                        ContextCompat.getColor(
                            itemView.context,
                            R.color.white_50_alpha
                        )
                    )
                }
            }
        }

        override fun onClick(v: View) {
            customization?.let { selectedCustomization ->
                if (selectedCustomization.path == "glasses") {
                    val key =
                        selectedCustomization.key.ifEmpty {
                            user?.items?.gear?.equipped?.eyeWear
                        }
                    key?.let { onEquipGear?.invoke(it) }
                } else {
                    val updateData = HashMap<String, Any>()
                    val updatePath = "preferences." + selectedCustomization.path
                    updateData[updatePath] = selectedCustomization.key
                    onUpdateUser?.invoke(updateData)
                }
            }
        }
    }
}
