package com.habitrpg.android.habitica.ui.adapter.social

import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.viewHolders.GroupMemberViewHolder
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.PublishSubject

class PartyMemberRecyclerViewAdapter : BaseRecyclerViewAdapter<Member, GroupMemberViewHolder>() {

    var leaderID: String? = null

    private val userClickedEvents = PublishSubject.create<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberViewHolder {
        return GroupMemberViewHolder(parent.inflate(R.layout.party_member))
    }

    override fun onBindViewHolder(holder: GroupMemberViewHolder, position: Int) {
        holder.bind(data[position], leaderID, null)
        holder.onClickEvent = {
            userClickedEvents.onNext(data[position].id ?: "")
        }
    }

    fun getUserClickedEvents(): Flowable<String> {
        return userClickedEvents.toFlowable(BackpressureStrategy.DROP)
    }
}
