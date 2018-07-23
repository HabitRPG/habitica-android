package com.habitrpg.android.habitica.ui.adapter.social

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeFilterOptions
import com.habitrpg.android.habitica.ui.helpers.bindView
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.playseeds.android.sdk.inappmessaging.Log
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import net.pherth.android.emoji_library.EmojiParser
import net.pherth.android.emoji_library.EmojiTextView
import org.greenrobot.eventbus.EventBus

class ChallengesListViewAdapter(data: OrderedRealmCollection<Challenge>?, autoUpdate: Boolean, private val viewUserChallengesOnly: Boolean, private val userId: String) : RealmRecyclerViewAdapter<Challenge, ChallengesListViewAdapter.ChallengeViewHolder>(data, autoUpdate) {
    private var unfilteredData: OrderedRealmCollection<Challenge>? = null

    private val openChallengeFragmentEvents = PublishSubject.create<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        return ChallengeViewHolder(parent.inflate(R.layout.challenge_item), viewUserChallengesOnly)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        data?.get(position).notNull { challenge ->
            holder.bind(challenge)
            holder.itemView.setOnClickListener {
                if (challenge.isManaged) {
                    challenge.id.notNull {
                        openChallengeFragmentEvents.onNext(it)
                    }
                }
            }
        }
    }

    fun updateUnfilteredData(data: OrderedRealmCollection<Challenge>?) {
        super.updateData(data)
        unfilteredData = data
    }

    fun filter(filterOptions: ChallengeFilterOptions) {
        if (unfilteredData == null) {
            return
        }

        var query = unfilteredData?.where()

        if (filterOptions.showByGroups != null && filterOptions.showByGroups.size > 0) {
            val groupIds = arrayOfNulls<String>(filterOptions.showByGroups.size)
            var index = 0
            for (group in filterOptions.showByGroups) {
                groupIds[index] = group.id
                index += 1
            }
            query = query?.`in`("groupId", groupIds)
        }

        if (filterOptions.showOwned != filterOptions.notOwned) {
            query = if (filterOptions.showOwned) {
                query?.equalTo("leaderId", userId)
            } else {
                query?.notEqualTo("leaderId", userId)
            }
        }

        query.notNull {
            this.updateData(it.findAll())
        }
    }

    fun getOpenDetailFragmentFlowable(): Flowable<String> {
        return openChallengeFragmentEvents.toFlowable(BackpressureStrategy.DROP)
    }

    class ChallengeViewHolder internal constructor(itemView: View, private val viewUserChallengesOnly: Boolean) : RecyclerView.ViewHolder(itemView) {

        private val challengeName: EmojiTextView by bindView(R.id.challenge_name)
        private val challengeDescription: TextView by bindView(R.id.challenge_group_name)
        private val leaderParticipantLayout: LinearLayout by bindView(R.id.leaderParticipantLayout)
        private val leaderName: TextView by bindView(R.id.leaderName)
        private val participantCount: TextView by bindView(R.id.participantCount)
        private val officialChallengeLayout: LinearLayout by bindView(R.id.officialHabiticaChallengeLayout)
        private val challengeParticipatingTextView: View by bindView(R.id.challenge_is_participating)
        private val memberCountTextView: TextView by bindView(R.id.memberCountTextView)
        private val arrowImage: LinearLayout by bindView(R.id.arrowImage)
        private val gemPrizeTextView: TextView by bindView(R.id.gemPrizeTextView)
        private val gemIconView: ImageView by bindView(R.id.gem_icon)

        private var challenge: Challenge? = null

        init {
            gemIconView.setImageBitmap(HabiticaIconsHelper.imageOfGem())

            if (!viewUserChallengesOnly) {
                challengeName.setTextColor(ContextCompat.getColor(itemView.context, R.color.brand_200))
            }
        }

        fun bind(challenge: Challenge) {
            this.challenge = challenge

            Log.e(challenge.id + challenge.name)
            challengeName.text = EmojiParser.parseEmojis(challenge.name?.trim { it <= ' ' })
            challengeDescription.text = challenge.groupName

            officialChallengeLayout.visibility = if (challenge.official) View.VISIBLE else View.GONE

            if (viewUserChallengesOnly) {
                leaderParticipantLayout.visibility = View.GONE
                challengeParticipatingTextView.visibility = View.GONE
                arrowImage.visibility = View.VISIBLE
            } else {
                //challengeParticipatingTextView.visibility = if (challenge.isParticipating) View.VISIBLE else View.GONE

                leaderName.text = itemView.context.getString(R.string.byLeader, challenge.leaderName)
                participantCount.text = challenge.memberCount.toString()
                leaderParticipantLayout.visibility = View.VISIBLE
                arrowImage.visibility = View.GONE
            }

            gemPrizeTextView.text = challenge.prize.toString()
        }
    }
}
