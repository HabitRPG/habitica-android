package com.habitrpg.android.habitica.ui.adapter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.Skill
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject

class SkillsRecyclerViewAdapter : RecyclerView.Adapter<SkillsRecyclerViewAdapter.SkillViewHolder>() {

    private val useSkillSubject = PublishSubject.create<Skill>()
    val useSkillEvents = useSkillSubject.toFlowable(BackpressureStrategy.DROP)

    var mana: Double = 0.toDouble()
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

        private val magicDrawable: Drawable
        private val skillImageView: SimpleDraweeView by bindView(R.id.skill_image)
        private val skillNameTextView: TextView by bindView(R.id.skill_text)
        private val skillNotesTextView: TextView by bindView(R.id.skill_notes)
        private val priceButton: Button by bindView(itemView, R.id.price_button)

        var skill: Skill? = null

        var context: Context = itemView.context

        init {
            priceButton.setOnClickListener(this)
            magicDrawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfMagic())
        }

        fun bind(skill: Skill) {
            this.skill = skill
            skillNameTextView.text = skill.text
            skillNotesTextView.text = skill.notes

            if ("special" == skill.habitClass) {
                priceButton.setText(R.string.skill_transformation_use)

                priceButton.setCompoundDrawables(null, null, null, null)
            } else {
                priceButton.text = skill.mana?.toString()

                priceButton.setCompoundDrawablesWithIntrinsicBounds(magicDrawable, null, null, null)
            }
            DataBindingUtils.loadImage(skillImageView, "shop_" + skill.key)

            if (skill.mana ?: 0 > mana) {
                priceButton.isEnabled = false
                priceButton.setBackgroundResource(R.color.task_gray)
                skillNameTextView.setTextColor(ContextCompat.getColor(context, R.color.task_gray))
                skillNotesTextView.setTextColor(ContextCompat.getColor(context, R.color.task_gray))
            } else {
                skillNameTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                skillNotesTextView.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                priceButton.isEnabled = true
            }
        }

        override fun onClick(v: View) {
            skill?.let { useSkillSubject.onNext(it) }
        }
    }
}
