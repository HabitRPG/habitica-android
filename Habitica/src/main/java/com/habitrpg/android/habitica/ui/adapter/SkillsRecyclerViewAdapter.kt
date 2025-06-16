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
import com.habitrpg.android.habitica.databinding.SkillTransformationListItemBinding
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.models.user.OwnedItem
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.inflate
import com.habitrpg.common.habitica.extensions.isUsingNightModeResources
import com.habitrpg.common.habitica.extensions.loadImage
import io.realm.RealmList

class SkillsRecyclerViewAdapter :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

    companion object {
        private const val TYPE_NORMAL  = 0
        private const val TYPE_SPECIAL = 1
    }

    fun setSkillList(list: List<Skill>) {
        skillList = list
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (skillList[position].habitClass == "special") TYPE_SPECIAL
        else TYPE_NORMAL
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SPECIAL) {
            val view = parent.inflate(R.layout.skill_transformation_list_item)
            SpecialViewHolder(view)
        } else {
            val view = parent.inflate(R.layout.skill_list_item)
            NormalViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SpecialViewHolder -> holder.bind(skillList[position])
            is NormalViewHolder  -> holder.bind(skillList[position])
        }
    }

    override fun getItemCount(): Int = skillList.size

    private inner class NormalViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val binding = SkillListItemBinding.bind(itemView)
        private val context = itemView.context
        private val magicDrawable: Drawable =
            BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfMagic())
        private val lockDrawable: Drawable =
            BitmapDrawable(
                context.resources,
                HabiticaIconsHelper.imageOfLocked(
                    ContextCompat.getColor(context, R.color.text_dimmed)
                )
            )
        private var skill: Skill? = null

        init {
            binding.buttonWrapper.setOnClickListener(this)
        }

        fun bind(skill: Skill) {
            this.skill = skill
            binding.skillText.text = skill.text
            binding.skillNotes.text = skill.notes

            binding.skillText.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            binding.skillNotes.setTextColor(ContextCompat.getColor(context, R.color.text_ternary))
            binding.skillNotes.visibility = View.VISIBLE
            binding.priceLabel.visibility = View.VISIBLE

            binding.countLabel.visibility = View.GONE
            binding.priceLabel.text = skill.mana?.toString()

            val manaColor = if (context.isUsingNightModeResources())
                R.color.blue_500 else R.color.blue_10
            binding.priceLabel.setTextColor(ContextCompat.getColor(context, manaColor))

            binding.buttonIconView.setImageDrawable(magicDrawable)

            if ((skill.mana ?: 0) > mana) {
                binding.buttonWrapper.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.offset_background)
                )
                binding.buttonIconView.alpha = 0.3f
                binding.priceLabel.alpha = 0.3f
            } else {
                binding.buttonWrapper.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.blue_500_24)
                )
                binding.buttonIconView.alpha = 1.0f
                binding.priceLabel.alpha = 1.0f
            }

            if ((skill.lvl ?: 0) > level) {
                binding.buttonWrapper.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.offset_background)
                )
                binding.skillText.setTextColor(
                    ContextCompat.getColor(context, R.color.text_dimmed)
                )
                binding.skillText.text = context.getString(
                    R.string.skill_unlocks_at, skill.lvl
                )
                binding.skillNotes.visibility = View.GONE
                binding.buttonIconView.setImageDrawable(lockDrawable)
                binding.priceLabel.visibility = View.GONE
            }

            binding.skillImage.loadImage("shop_" + skill.key)
        }

        override fun onClick(v: View) {
            skill?.takeIf { (it.lvl ?: 0) <= level }?.also {
                onUseSkill?.invoke(it)
            }
        }
    }

    private inner class SpecialViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private val binding = SkillTransformationListItemBinding.bind(itemView)
        private val context = itemView.context

        init {
            binding.specialSkillContainer.setOnClickListener(this)
        }

        fun bind(skill: Skill) {
            binding.skillText.text = skill.text
            binding.skillNotes.text = skill.notes
            binding.skillText.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            binding.skillNotes.setTextColor(ContextCompat.getColor(context, R.color.text_ternary))

            binding.countLabel.text = getOwnedCount(skill.key).toString()
            binding.skillImage.loadImage("shop_" + skill.key)
        }

        override fun onClick(v: View) {
            onUseSkill?.invoke(skillList[bindingAdapterPosition])
        }
    }

    private fun getOwnedCount(key: String): Int =
        specialItems?.firstOrNull { it.key == key }?.numberOwned ?: 0
}
