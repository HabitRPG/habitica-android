package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.DialogPurchaseContentQuestBinding
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.QuestDropItem
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.extensions.layoutInflater
import com.habitrpg.common.habitica.extensions.loadImage
import com.habitrpg.common.habitica.views.PixelArtView

class PurchaseDialogQuestContent(context: Context) : PurchaseDialogContent(context) {
    private val binding = DialogPurchaseContentQuestBinding.inflate(context.layoutInflater, this)
    override val imageView: PixelArtView
        get() = binding.imageView
    override val titleTextView: TextView
        get() = binding.titleTextView

    override fun setQuestContentItem(questContent: QuestContent) {
        super.setQuestContentItem(questContent)
        binding.rageMeterView.visibility = View.GONE
        if (questContent.isBossQuest) {
            binding.questTypeTextView.setText(R.string.boss_quest)
            binding.questCollectView.visibility = View.GONE
            binding.bossHealthText.text = questContent.boss?.hp.toString()
            if (questContent.boss?.hasRage == true) {
                binding.rageMeterView.visibility = View.VISIBLE
            }
            binding.questDifficultyView.rating = questContent.boss?.str ?: 1f
        } else {
            binding.questTypeTextView.setText(R.string.collection_quest)
            val collectionList = questContent.collect?.map { it.count.toString() + " " + it.text }
            binding.questCollectText.text = TextUtils.join(", ", collectionList ?: listOf<String>())

            binding.bossHealthView.visibility = View.GONE

            binding.questDifficultyView.rating = 1f
        }

        binding.questDetailView.visibility = View.VISIBLE

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

            if ((questContent.drop?.exp ?: 0) > 0) {
                val view =
                    inflater?.inflate(
                        R.layout.row_quest_reward_imageview,
                        binding.rewardsList,
                        false
                    ) as? ViewGroup
                val imageView = view?.findViewById<ImageView>(R.id.imageView)
                imageView?.scaleType = ImageView.ScaleType.CENTER
                imageView?.setImageBitmap(HabiticaIconsHelper.imageOfExperienceReward())
                val titleTextView = view?.findViewById<TextView>(R.id.titleTextView)
                titleTextView?.text =
                    context.getString(R.string.experience_reward, questContent.drop?.exp)
                binding.rewardsList.addView(view)
            }

            if ((questContent.drop?.gp ?: 0) > 0) {
                val view =
                    inflater?.inflate(
                        R.layout.row_quest_reward_imageview,
                        binding.rewardsList,
                        false
                    ) as? ViewGroup
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
            titleTextView?.text =
                context.getString(R.string.quest_reward_count, item.text, item.count)
        } else {
            titleTextView?.text = item.text
        }
        containerView?.addView(view)
    }
}
