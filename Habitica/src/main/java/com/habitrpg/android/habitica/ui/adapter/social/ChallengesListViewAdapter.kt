package com.habitrpg.android.habitica.ui.adapter.social

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ChallengeItemBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.shared.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeFilterOptions
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.helpers.EmojiParser

class ChallengesListViewAdapter(data: OrderedRealmCollection<Challenge>?, autoUpdate: Boolean, private val viewUserChallengesOnly: Boolean, private val userId: String) : RealmRecyclerViewAdapter<Challenge, ChallengesListViewAdapter.ChallengeViewHolder>(data, autoUpdate) {
    private var unfilteredData: OrderedRealmCollection<Challenge>? = null
    private var challengeMemberships: OrderedRealmCollection<ChallengeMembership>? = null

    private val openChallengeFragmentEvents = PublishSubject.create<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        return ChallengeViewHolder(parent.inflate(R.layout.challenge_item), viewUserChallengesOnly)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        data?.get(position)?.let { challenge ->
            holder.bind(challenge, challengeMemberships?.first { challenge.id == it.challengeID } != null)
            holder.itemView.setOnClickListener {
                if (challenge.isManaged && challenge.isValid) {
                    challenge.id?.let {
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

        query?.let {
            this.updateData(it.findAll())
        }
    }

    fun getOpenDetailFragmentFlowable(): Flowable<String> {
        return openChallengeFragmentEvents.toFlowable(BackpressureStrategy.DROP)
    }

    class ChallengeViewHolder internal constructor(itemView: View, private val viewUserChallengesOnly: Boolean) : RecyclerView.ViewHolder(itemView) {
        private val binding = ChallengeItemBinding.bind(itemView)

        private var challenge: Challenge? = null

        init {
            binding.gemIcon.setImageBitmap(HabiticaIconsHelper.imageOfGem())
        }

        fun bind(challenge: Challenge, isParticipating: Boolean) {
            this.challenge = challenge

            binding.challengeName.text = EmojiParser.parseEmojis(challenge.name?.trim { it <= ' ' })
            binding.challengeShorttext.text = challenge.summary

            binding.officialChallengeView.visibility = if (challenge.official) View.VISIBLE else View.GONE

            if (viewUserChallengesOnly) {
                binding.isJoinedLabel.visibility = View.GONE
            } else {
                binding.isJoinedLabel.visibility = if (isParticipating) View.VISIBLE else View.GONE
            }
            binding.participantCount.text = challenge.memberCount.toString()

            binding.gemPrizeTextView.text = challenge.prize.toString()
        }
    }
}
