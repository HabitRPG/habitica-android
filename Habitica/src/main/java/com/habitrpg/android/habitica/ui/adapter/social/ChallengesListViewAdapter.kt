package com.habitrpg.android.habitica.ui.adapter.social

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.databinding.ChallengeItemBinding
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.social.ChallengeMembership
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.fragments.social.challenges.ChallengeFilterOptions
import com.habitrpg.android.habitica.ui.views.HabiticaIconsHelper
import com.habitrpg.common.habitica.helpers.EmojiParser
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.realm.OrderedRealmCollection

class ChallengesListViewAdapter(
    private val viewUserChallengesOnly: Boolean,
    private val userId: String
) : BaseRecyclerViewAdapter<Challenge, ChallengesListViewAdapter.ChallengeViewHolder>() {
    private var unfilteredData: List<Challenge>? = null
    private var challengeMemberships: List<ChallengeMembership>? = null

    private val openChallengeFragmentEvents = PublishSubject.create<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChallengeViewHolder {
        return ChallengeViewHolder(parent.inflate(R.layout.challenge_item), viewUserChallengesOnly)
    }

    override fun onBindViewHolder(holder: ChallengeViewHolder, position: Int) {
        data[position].let { challenge ->
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

    fun updateUnfilteredData(data: List<Challenge>?) {
        this.data = data ?: emptyList()
        unfilteredData = data
    }

    fun filter(filterOptions: ChallengeFilterOptions) {
        val unfilteredData = unfilteredData as? OrderedRealmCollection ?: return

        var query = unfilteredData.where()

        if (filterOptions.showByGroups.isNotEmpty()) {
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
            data = it.findAll()
        }
    }

    fun getOpenDetailFragmentFlowable(): Flowable<String> {
        return openChallengeFragmentEvents.toFlowable(BackpressureStrategy.DROP)
    }

    class ChallengeViewHolder internal constructor(
        itemView: View,
        private val viewUserChallengesOnly: Boolean
    ) : RecyclerView.ViewHolder(itemView) {
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
