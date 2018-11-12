package com.habitrpg.android.habitica.ui.adapter.social

import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.user.Stats
import com.habitrpg.android.habitica.ui.AvatarView
import com.habitrpg.android.habitica.ui.AvatarWithBarsViewModel
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.android.habitica.ui.views.ValueBar
import com.habitrpg.android.habitica.ui.views.social.UsernameLabel
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class PartyMemberRecyclerViewAdapter(data: OrderedRealmCollection<Member>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<Member, PartyMemberRecyclerViewAdapter.MemberViewHolder>(data, autoUpdate) {

    var leaderID: String? = null

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

    inner class MemberViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
        
        private val avatarView: AvatarView by bindView(R.id.avatarView)
        private val displayNameTextView: UsernameLabel by bindView(R.id.display_name_textview)
        private val sublineTextView: TextView by bindView(R.id.subline_textview)
        private val buffIconView: ImageView by bindView(R.id.buff_icon_view)
        private val classIconView: ImageView by bindView(R.id.class_icon_view)
        private val hpBar: ValueBar by bindView(R.id.hp_bar)
        private val expBar: ValueBar by bindView(R.id.exp_bar)
        private val mpBar: ValueBar by bindView(R.id.mp_bar)
        private val leaderTextView: TextView by bindView(R.id.leader_textview)
        
        init {
            hpBar.setLightBackground(true)
            hpBar.setIcon(HabiticaIconsHelper.imageOfHeartLightBg())
            expBar.setLightBackground(true)
            expBar.setIcon(HabiticaIconsHelper.imageOfExperience())
            mpBar.setLightBackground(true)
            mpBar.setIcon(HabiticaIconsHelper.imageOfMagic())

            buffIconView.setImageBitmap(HabiticaIconsHelper.imageOfBuffIcon())
        }

        fun bind(user: Member) {
            avatarView.setAvatar(user)

            user.stats.notNull {
                AvatarWithBarsViewModel.setHpBarData(hpBar, it)
                expBar.set(it.exp ?: 0.0, it.toNextLevel?.toDouble() ?: 0.0)
                mpBar.set(it.mp ?: 0.0, it.maxMP?.toDouble() ?: 0.0)
            }
            displayNameTextView.username = user.profile?.name
            displayNameTextView.tier = user.contributor?.level ?: 0

            if (user.username != null) {
                sublineTextView.text = itemView.context.getString(R.string.username_level, user.username, user.stats?.lvl)
            } else {
                sublineTextView.text = itemView.context.getString(R.string.user_level, user.stats?.lvl)
            }

            if (user.stats?.isBuffed == true) {
                buffIconView.visibility = View.VISIBLE
            } else {
                buffIconView.visibility = View.GONE
            }

            classIconView.visibility = View.VISIBLE
            when (user.stats?.habitClass) {
                Stats.HEALER -> {
                    classIconView.setImageBitmap(HabiticaIconsHelper.imageOfHealerLightBg())
                }
                Stats.WARRIOR -> {
                    classIconView.setImageBitmap(HabiticaIconsHelper.imageOfWarriorLightBg())
                }
                Stats.ROGUE -> {
                    classIconView.setImageBitmap(HabiticaIconsHelper.imageOfRogueLightBg())
                }
                Stats.MAGE -> {
                    classIconView.setImageBitmap(HabiticaIconsHelper.imageOfMageLightBg())
                }
                else -> {
                    classIconView.visibility = View.INVISIBLE
                }
            }

            itemView.isClickable = true
            itemView.setOnClickListener { userClickedEvents.onNext(user.id ?: "") }

            leaderTextView.visibility = if (user.id == leaderID) View.VISIBLE else View.GONE
        }
    }
}
