package com.habitrpg.android.habitica.ui.adapter.social

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel
import com.habitrpg.android.habitica.ui.helpers.ViewHelper
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.ValueBar
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class PartyMemberRecyclerViewAdapter(data: OrderedRealmCollection<Member>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<Member, PartyMemberRecyclerViewAdapter.MemberViewHolder>(data, autoUpdate) {

    private val userClickedEvents = PublishSubject.create<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        return MemberViewHolder(parent.inflate(R.layout.party_member))
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        data.notNull {
            holder.bind(it[position])
        }
    }

    fun getUserClickedEvents(): Flowable<String> {
        return userClickedEvents.toFlowable(BackpressureStrategy.DROP)
    }

    inner class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        
        private val avatarView: AvatarView by bindView(R.id.avatarView)
        private val userName: TextView by bindView(R.id.username)
        private val lvl: TextView by bindView(R.id.user_lvl)
        private val classLabel: TextView by bindView(R.id.class_label)
        private val classBackground: View by bindView(R.id.class_background_layout)
        private val hpBar: ValueBar by bindView(R.id.hpBar)
        
        init {
            hpBar.setLightBackground(true)
            hpBar.setIcon(HabiticaIconsHelper.imageOfHeartLightBg())
        }

        fun bind(user: Member) {
            avatarView.setAvatar(user)

            user.stats.notNull { AvatarWithBarsViewModel.setHpBarData(hpBar, it) }

            lvl.text = itemView.context.getString(R.string.user_level, user.stats?.lvl)

            classLabel.text = user.stats?.getTranslatedClassName(itemView.context)

            val colorResourceID: Int = when (user.stats?.habitClass) {
                Stats.HEALER -> {
                    R.color.class_healer
                }
                Stats.WARRIOR -> {
                    R.color.class_warrior
                }
                Stats.ROGUE -> {
                    R.color.class_rogue
                }
                Stats.MAGE -> {
                    R.color.class_wizard
                }
                else -> R.color.task_gray
            }
            ViewHelper.SetBackgroundTint(classBackground, ContextCompat.getColor(itemView.context, colorResourceID))
            userName.text = user.profile?.name

            itemView.isClickable = true
            itemView.setOnClickListener { userClickedEvents.onNext(user.id) }
        }
    }
}
