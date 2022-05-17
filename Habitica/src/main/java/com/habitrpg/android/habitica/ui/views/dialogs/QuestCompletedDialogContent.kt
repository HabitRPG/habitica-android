package com.habitrpg.android.habitica.ui.views.dialogs

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogCompletedQuestContentBinding
import com.habitrpg.android.habitica.extensions.fromHtml
import com.habitrpg.android.habitica.extensions.layoutInflater
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.QuestDropItem
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.views.PixelArtView

class QuestCompletedDialogContent : LinearLayout {

    private lateinit var binding: DialogCompletedQuestContentBinding

    constructor(context: Context) : super(context) {
        setupView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupView()
    }

    private fun setupView() {
        orientation = VERTICAL
        gravity = Gravity.CENTER

        binding = DialogCompletedQuestContentBinding.inflate(context.layoutInflater, this)
    }

    fun setQuestContent(questContent: QuestContent) {
        binding.titleTextView.setText(questContent.text.fromHtml(), TextView.BufferType.SPANNABLE)
        binding.notesTextView.setText(questContent.completion.fromHtml(), TextView.BufferType.SPANNABLE)
        binding.imageView.loadImage("quest_" + questContent.key)

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater

        if (questContent.drop != null && questContent.drop?.items != null) {
            questContent.drop?.items
                ?.filterNot { it.onlyOwner }
                ?.forEach { addRewardsRow(inflater, it, binding.rewardsList) }

            var hasOwnerRewards = false
            for (item in questContent.drop?.items ?: emptyList<QuestDropItem>()) {
                if (item.onlyOwner) {
                    addRewardsRow(inflater, item, binding.ownerRewardsList)
                    hasOwnerRewards = true
                }
            }
            if (!hasOwnerRewards) {
                binding.ownerRewardsTitle.visibility = View.GONE
                binding.ownerRewardsList.visibility = View.GONE
            }

            if (questContent.drop?.exp ?: 0 > 0) {
                val view = inflater?.inflate(R.layout.row_quest_reward_imageview, binding.rewardsList, false) as? ViewGroup
                val imageView = view?.findViewById<ImageView>(R.id.imageView)
                imageView?.scaleType = ImageView.ScaleType.CENTER
                imageView?.setImageBitmap(HabiticaIconsHelper.imageOfExperienceReward())
                val titleTextView = view?.findViewById<TextView>(R.id.titleTextView)
                titleTextView?.text = context.getString(R.string.experience_reward, questContent.drop?.exp)
                binding.rewardsList.addView(view)
            }

            if (questContent.drop?.gp ?: 0 > 0) {
                val view = inflater?.inflate(R.layout.row_quest_reward_imageview, binding.rewardsList, false) as? ViewGroup
                val imageView = view?.findViewById<ImageView>(R.id.imageView)
                imageView?.scaleType = ImageView.ScaleType.CENTER
                imageView?.setImageBitmap(HabiticaIconsHelper.imageOfGoldReward())
                val titleTextView = view?.findViewById<TextView>(R.id.titleTextView)
                titleTextView?.text = context.getString(R.string.gold_reward, questContent.drop?.gp)
                binding.rewardsList.addView(view)
            }
        }
    }

    private fun addRewardsRow(
        inflater: LayoutInflater?,
        item: QuestDropItem,
        containerView: ViewGroup?
    ) {
        val view = inflater?.inflate(R.layout.row_quest_reward, containerView, false) as? ViewGroup
        val imageView = view?.findViewById(R.id.imageView) as? PixelArtView
        val titleTextView = view?.findViewById(R.id.titleTextView) as? TextView
        imageView?.loadImage(item.imageName)
        if (item.count > 1) {
            titleTextView?.text = context.getString(R.string.quest_reward_count, item.text, item.count)
        } else {
            titleTextView?.text = item.text
        }
        containerView?.addView(view)
    }
}
