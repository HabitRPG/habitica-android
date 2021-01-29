package com.habitrpg.android.habitica.ui.adapter.social

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.imagepipeline.image.ImageInfo
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ProfileAchievementItemBinding
import com.habitrpg.android.habitica.extensions.addOkButton
import com.habitrpg.android.habitica.extensions.fromHtml
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder
import com.habitrpg.android.habitica.ui.views.dialogs.AchievementDetailDialog
import com.habitrpg.android.habitica.ui.views.dialogs.HabiticaAlertDialog

class AchievementProfileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var itemType: String? = null
    var activity: MainActivity? = null
    private var itemList: List<Any> = emptyList()

    fun setItemList(itemList: List<Any>) {
        this.itemList = itemList
        this.notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == 0) {
            SectionViewHolder(parent.inflate(R.layout.profile_achievement_category))
        } else {
            AchievementViewHolder(parent.inflate(R.layout.profile_achievement_item))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val obj = this.itemList[position]
        if (obj.javaClass == String::class.java) {
            (holder as? SectionViewHolder)?.bind(obj as String)
        } else {
            (holder as? AchievementViewHolder)?.bind(itemList[position] as Achievement)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (this.itemList[position].javaClass == String::class.java) {
            0
        } else {
            1
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    internal class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        private val binding = ProfileAchievementItemBinding.bind(itemView)
        private var achievement: Achievement? = null

        init {
            itemView.isClickable = true
            itemView.setOnClickListener(this)
        }

        fun bind(item: Achievement) {
            val iconUrl = AvatarView.IMAGE_URI_ROOT + (if (!item.earned) "achievement-unearned" else item.icon) + "2x.png"

            binding.achievementDrawee.controller = Fresco.newDraweeControllerBuilder()
                    .setUri(iconUrl)
                    .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                        override fun onFailure(id: String?, throwable: Throwable?) { /* no-on */ }
                    })
                    .build()

            this.achievement = item
            binding.achievementText.text = item.title

            if (item.optionalCount == null || item.optionalCount == 0) {
                binding.achievementCountLabel.visibility = View.GONE
            } else {
                binding.achievementCountLabel.visibility = View.VISIBLE
                binding.achievementCountLabel.text = item.optionalCount.toString()
            }
        }

        override fun onClick(view: View) {
            achievement?.let {
                AchievementDetailDialog(it, itemView.context).show()
            }
        }
    }
}
