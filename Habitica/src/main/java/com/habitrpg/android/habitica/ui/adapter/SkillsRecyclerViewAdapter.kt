package com.habitrpg.android.habitica.ui.adapter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.SkillListItemBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.extensions.loadImage
import io.realm.RealmList

class SkillsRecyclerViewAdapter : RecyclerView.Adapter<SkillsRecyclerViewAdapter.SkillViewHolder>() {

    var onUseSkill: ((Skill) -> Unit)? = null

    var mana: Double = 0.0
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var level: Int = 0
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    var specialItems: RealmList<OwnedItem>? = null
        set(value) {
            field = value
            notifyDataSetChanged()
        }
    private var skillList: List<Skill> = emptyList()

    fun setSkillList(skillList: List<Skill>) {
        this.skillList = skillList
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
        return SkillViewHolder(parent.inflate(R.layout.skill_list_item))
    }

    override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
        holder.bind(skillList[position])
    }

    override fun getItemCount(): Int {
        return skillList.size
    }

    inner class SkillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val binding = SkillListItemBinding.bind(itemView)
        private val magicDrawable: Drawable
        private val lockDrawable: Drawable

        var skill: Skill? = null

        var context: Context = itemView.context

        init {
            binding.buttonWrapper.setOnClickListener(this)
            magicDrawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfMagic())
            lockDrawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfLocked(ContextCompat.getColor(context, R.color.text_dimmed)))
        }

        fun bind(skill: Skill) {
            this.skill = skill
            binding.skillText.text = skill.text
            binding.skillNotes.text = skill.notes

            binding.skillText.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            binding.skillNotes.setTextColor(ContextCompat.getColor(context, R.color.text_ternary))
            binding.skillNotes.visibility = View.VISIBLE
            binding.priceLabel.visibility = View.VISIBLE

            if ("special" == skill.habitClass) {
                binding.countLabel.visibility = View.VISIBLE
                binding.countLabel.text = getOwnedCount(skill.key).toString()
                binding.priceLabel.setText(R.string.skill_transformation_use)
                if (context.isUsingNightModeResources()) {
                    binding.priceLabel.setTextColor(ContextCompat.getColor(context, R.color.brand_500))
                } else {
                    binding.priceLabel.setTextColor(ContextCompat.getColor(context, R.color.color_accent))
                }
                binding.buttonIconView.setImageDrawable(null)
                binding.buttonWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.offset_background))
                binding.buttonIconView.alpha = 1.0f
                binding.priceLabel.alpha = 1.0f
            } else {
                binding.countLabel.visibility = View.GONE
                binding.priceLabel.text = skill.mana?.toString()
                if (context.isUsingNightModeResources()) {
                    binding.priceLabel.setTextColor(ContextCompat.getColor(context, R.color.blue_500))
                } else {
                    binding.priceLabel.setTextColor(ContextCompat.getColor(context, R.color.blue_10))
                }
                binding.buttonIconView.setImageDrawable(magicDrawable)

                if (skill.mana ?: 0 > mana) {
                    binding.buttonWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.offset_background))
                    binding.buttonIconView.alpha = 0.3f
                    binding.priceLabel.alpha = 0.3f
                } else {
                    binding.buttonWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_500_24))
                    binding.buttonIconView.alpha = 1.0f
                    binding.priceLabel.alpha = 1.0f
                }
                if ((skill.lvl ?: 0) > level) {
                    binding.buttonWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.offset_background))
                    binding.skillText.setTextColor(ContextCompat.getColor(context, R.color.text_dimmed))
                    binding.skillText.text = context.getString(R.string.skill_unlocks_at, skill.lvl)
                    binding.skillNotes.visibility = View.GONE
                    binding.buttonIconView.setImageDrawable(lockDrawable)
                    binding.priceLabel.visibility = View.GONE
                }
            }
            binding.skillImage.loadImage("shop_" + skill.key)
        }

        override fun onClick(v: View) {
            if ((skill?.lvl ?: 0) <= level) {
                skill?.let { onUseSkill?.invoke(it) }
            }
        }

        private fun getOwnedCount(key: String): Int {
            return specialItems?.firstOrNull() { it.key == key }?.numberOwned ?: 0
        }
    }
}
