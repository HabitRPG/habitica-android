package com.habitrpg.android.habitica.ui.adapter.social

import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.viewHolders.GroupMemberViewHolder.GroupMemberViewHolder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject
import io.realm.OrderedRealmCollection
import io.realm.RealmRecyclerViewAdapter

class PartyMemberRecyclerViewAdapter(data: OrderedRealmCollection<Member>?, autoUpdate: Boolean) : RealmRecyclerViewAdapter<Member, GroupMemberViewHolder>(data, autoUpdate) {

    var leaderID: String? = null

    private val userClickedEvents = PublishSubject.create<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberViewHolder {
        return GroupMemberViewHolder(parent.inflate(R.layout.party_member))
    }

    override fun onBindViewHolder(holder: GroupMemberViewHolder, position: Int) {
        data?.let {
            holder.bind(it[position], leaderID)
            holder.onClickEvent = {
                userClickedEvents.onNext(it[position].id ?: "")
            }
        }
    }

    fun getUserClickedEvents(): Flowable<String> {
        return userClickedEvents.toFlowable(BackpressureStrategy.DROP)
    }
}
