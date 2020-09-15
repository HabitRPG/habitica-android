package com.habitrpg.android.habitica.ui.adapter

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import com.habitrpg.shared.habitica.models.user.SpecialItems
import io.reactivex.BackpressureStrategy
import io.reactivex.subjects.PublishSubject

class SkillsRecyclerViewAdapter : RecyclerView.Adapter<SkillsRecyclerViewAdapter.SkillViewHolder>() {

    private val useSkillSubject = PublishSubject.create<Skill>()
    val useSkillEvents = useSkillSubject.toFlowable(BackpressureStrategy.DROP)

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
    var specialItems: SpecialItems? = null
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
        private val lockDrawable: Drawable
        private val skillImageView: SimpleDraweeView by bindView(R.id.skill_image)
        private val skillNameTextView: TextView by bindView(R.id.skill_text)
        private val skillNotesTextView: TextView by bindView(R.id.skill_notes)
        private val buttonWrapper: ViewGroup by bindView(itemView, R.id.button_wrapper)
        private val priceLabel: TextView by bindView(itemView, R.id.price_label)
        private val buttonIconView: ImageView by bindView(itemView, R.id.button_icon_view)
        private val countLabel: TextView by bindView(itemView, R.id.count_label)

        var skill: Skill? = null

        var context: Context = itemView.context

        init {
            buttonWrapper.setOnClickListener(this)
            magicDrawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfMagic())
            lockDrawable = BitmapDrawable(context.resources, HabiticaIconsHelper.imageOfLocked(ContextCompat.getColor(context, R.color.gray_400)))
        }

        fun bind(skill: Skill) {
            this.skill = skill
            skillNameTextView.text = skill.text
            skillNotesTextView.text = skill.notes

            skillNameTextView.setTextColor(ContextCompat.getColor(context, R.color.gray_50))
            skillNotesTextView.setTextColor(ContextCompat.getColor(context, R.color.gray_200))
            skillNotesTextView.visibility = View.VISIBLE
            priceLabel.visibility = View.VISIBLE

            if ("special" == skill.habitClass) {
                countLabel.visibility = View.VISIBLE
                countLabel.text = getOwnedCount(skill.key).toString()
                priceLabel.setText(R.string.skill_transformation_use)
                priceLabel.setTextColor(ContextCompat.getColor(context, R.color.brand_400))
                buttonIconView.setImageDrawable(null)
                buttonWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_600))
                buttonIconView.alpha = 1.0f
                priceLabel.alpha = 1.0f
            } else {
                countLabel.visibility = View.GONE
                priceLabel.text = skill.mana?.toString()
                priceLabel.setTextColor(ContextCompat.getColor(context, R.color.blue_10))
                buttonIconView.setImageDrawable(magicDrawable)

                if (skill.mana ?: 0 > mana) {
                    buttonWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_600))
                    buttonIconView.alpha = 0.3f
                    priceLabel.alpha = 0.3f
                } else {
                    buttonWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.blue_500_24))
                    buttonIconView.alpha = 1.0f
                    priceLabel.alpha = 1.0f
                }
                if ((skill.lvl ?: 0) > level) {
                    buttonWrapper.setBackgroundColor(ContextCompat.getColor(context, R.color.gray_600))
                    skillNameTextView.setTextColor(ContextCompat.getColor(context, R.color.task_gray))
                    skillNameTextView.text = context.getString(R.string.skill_unlocks_at, skill.lvl)
                    skillNotesTextView.visibility = View.GONE
                    buttonIconView.setImageDrawable(lockDrawable)
                    priceLabel.visibility = View.GONE
                }
            }
            DataBindingUtils.loadImage(skillImageView, "shop_" + skill.key)

        }

        override fun onClick(v: View) {
            if ((skill?.lvl ?: 0) <= level) {
                skill?.let { useSkillSubject.onNext(it) }
            }
        }

        private fun getOwnedCount(key: String): Int {
            return when (key) {
                "snowball" -> specialItems?.snowball
                "shinySeed" -> specialItems?.shinySeed
                "seafoam" -> specialItems?.seafoam
                "spookySparkles" -> specialItems?.spookySparkles
                else -> 0
            } ?: 0
        }
    }
}
