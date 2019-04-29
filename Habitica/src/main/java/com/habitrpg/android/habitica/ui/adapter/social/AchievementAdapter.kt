package com.habitrpg.android.habitica.ui.adapter.social

import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.controller.BaseControllerListener
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.image.ImageInfo
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.Achievement
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.activities.MainActivity
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.viewHolders.SectionViewHolder

class AchievementAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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
            (holder as SectionViewHolder).bind(obj as String)
        } else {
            (holder as AchievementViewHolder).bind(itemList[position] as Achievement)
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
        private var achievement: Achievement? = null

        private val draweeView: SimpleDraweeView by bindView(R.id.achievement_drawee)
        private val titleView: TextView by bindView(R.id.achievement_text)
        private val countText: TextView by bindView(R.id.achievement_count_label)

        init {
            itemView.isClickable = true
            itemView.setOnClickListener(this)
        }

        fun bind(item: Achievement) {
            val iconUrl = AvatarView.IMAGE_URI_ROOT + (if (!item.earned) "achievement-unearned" else item.icon) + "2x.png"

            draweeView.controller = Fresco.newDraweeControllerBuilder()
                    .setUri(iconUrl)
                    .setControllerListener(object : BaseControllerListener<ImageInfo>() {
                        override fun onFailure(id: String?, throwable: Throwable?) {}
                    })
                    .build()

            this.achievement = item
            titleView.text = item.title

            if (item.optionalCount == null) {
                countText.visibility = View.GONE
            } else {
                countText.visibility = View.VISIBLE
                countText.text = item.optionalCount.toString()
            }
        }

        override fun onClick(view: View) {
            val context = itemView.context
            val b = AlertDialog.Builder(context)

            val customView = LayoutInflater.from(context)
                    .inflate(R.layout.dialog_achievement_details, null)
            val achievementImage = customView.findViewById<View>(R.id.achievement_image) as ImageView?
            achievementImage?.setImageDrawable(draweeView.drawable)

            val titleView = customView.findViewById<View>(R.id.achievement_title) as TextView?
            titleView?.text = achievement?.title

            val textView = customView.findViewById<View>(R.id.achievement_text) as TextView?
            textView?.text = achievement?.text

            b.setView(customView)
            b.setPositiveButton(R.string.profile_achievement_ok) { _, _ -> }

            b.show()
        }
    }
}
