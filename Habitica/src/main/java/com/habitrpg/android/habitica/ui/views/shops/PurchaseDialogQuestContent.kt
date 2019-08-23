package com.habitrpg.android.habitica.ui.views.shops

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.facebook.drawee.view.SimpleDraweeView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.models.inventory.QuestContent
import com.habitrpg.android.habitica.models.inventory.QuestDropItem
import com.habitrpg.android.habitica.ui.helpers.DataBindingUtils
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper

class PurchaseDialogQuestContent : PurchaseDialogContent {

    private val questDetailView: View by bindView(R.id.questDetailView)
    private val questTypeTextView: TextView by bindView(R.id.questTypeTextView)
    private val bossHealthView: View by bindView(R.id.boss_health_view)
    private val bossHealthTextView: TextView by bindView(R.id.boss_health_text)
    private val questCollectView: View by bindView(R.id.quest_collect_view)
    private val questCollectTextView: TextView by bindView(R.id.quest_collect_text)
    private val questDifficultyView: RatingBar by bindView(R.id.quest_difficulty_view)
    private val rageMeterView: View by bindView(R.id.rage_meter_view)
    private val rewardsList: ViewGroup by bindView(R.id.rewardsList)
    private val ownerRewardsTitle: View by bindView(R.id.ownerRewardsTitle)
    private val ownerRewardsList: ViewGroup by bindView(R.id.ownerRewardsList)

    override val viewId: Int
        get() = R.layout.dialog_purchase_content_quest

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    fun setQuestContent(questContent: QuestContent) {
        rageMeterView.visibility = View.GONE
        if (questContent.isBossQuest) {
            questTypeTextView.setText(R.string.boss_quest)
            questCollectView.visibility = View.GONE
            bossHealthTextView.text = questContent.boss?.hp.toString()
            if (questContent.boss?.hasRage() == true) {
                rageMeterView.visibility = View.VISIBLE
            }
            questDifficultyView.rating = questContent.boss?.str ?: 1f
        } else {
            questTypeTextView.setText(R.string.collection_quest)
            val collectionList = questContent.collect?.map { it.count.toString() + " " + it.text }
            questCollectTextView.text = TextUtils.join(", ", collectionList ?: listOf<String>())

            bossHealthView.visibility = View.GONE

            questDifficultyView.rating = 1f
        }

        questDetailView.visibility = View.VISIBLE

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as? LayoutInflater

        if (questContent.drop != null && questContent.drop?.items != null) {
            questContent.drop?.items
                    ?.filterNot { it.isOnlyOwner }
                    ?.forEach { addRewardsRow(inflater, it, rewardsList) }

            var hasOwnerRewards = false
            for (item in questContent.drop?.items ?: emptyList<QuestDropItem>()) {
                if (item.isOnlyOwner) {
                    addRewardsRow(inflater, item, ownerRewardsList)
                    hasOwnerRewards = true
                }
            }
            if (!hasOwnerRewards) {
                ownerRewardsTitle.visibility = View.GONE
                ownerRewardsList.visibility = View.GONE
            }

            if (questContent.drop?.exp ?: 0 > 0) {
                val view = inflater?.inflate(R.layout.row_quest_reward_imageview, rewardsList, false) as? ViewGroup
                val imageView = view?.findViewById<ImageView>(R.id.imageView)
                imageView?.scaleType = ImageView.ScaleType.CENTER
                imageView?.setImageBitmap(HabiticaIconsHelper.imageOfExperienceReward())
                val titleTextView = view?.findViewById<TextView>(R.id.titleTextView)
                titleTextView?.text = context.getString(R.string.experience_reward, questContent.drop?.exp)
                rewardsList.addView(view)
            }

            if (questContent.drop?.gp ?: 0 > 0) {
                val view = inflater?.inflate(R.layout.row_quest_reward_imageview, rewardsList, false) as? ViewGroup
                val imageView = view?.findViewById<ImageView>(R.id.imageView)
                imageView?.scaleType = ImageView.ScaleType.CENTER
                imageView?.setImageBitmap(HabiticaIconsHelper.imageOfGoldReward())
                val titleTextView = view?.findViewById<TextView>(R.id.titleTextView)
                titleTextView?.text = context.getString(R.string.gold_reward, questContent.drop?.gp)
                rewardsList.addView(view)
            }
        }
    }

    private fun addRewardsRow(inflater: LayoutInflater?, item: QuestDropItem, containerView: ViewGroup?) {
        val view = inflater?.inflate(R.layout.row_quest_reward, containerView, false) as? ViewGroup
        val imageView = view?.findViewById(R.id.imageView) as? SimpleDraweeView
        val titleTextView = view?.findViewById(R.id.titleTextView) as? TextView
        DataBindingUtils.loadImage(imageView, item.imageName)
        if (item.count > 1) {
            titleTextView?.text = context.getString(R.string.quest_reward_count, item.text, item.count)
        } else {
            titleTextView?.text = item.text
        }
        containerView?.addView(view)
    }
}
