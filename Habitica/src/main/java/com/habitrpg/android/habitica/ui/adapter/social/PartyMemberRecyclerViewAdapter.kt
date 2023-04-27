package com.habitrpg.android.habitica.ui.adapter.social

import android.view.ViewGroup
import com.habitrpg.android.habitica.R
import com.habitrpg.android.habitica.extensions.inflate
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.adapter.BaseRecyclerViewAdapter
import com.habitrpg.android.habitica.ui.viewHolders.GroupMemberViewHolder

class PartyMemberRecyclerViewAdapter : BaseRecyclerViewAdapter<Member, GroupMemberViewHolder>() {

    var leaderID: String? = null

    var onUserClicked: ((String) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupMemberViewHolder {
        return GroupMemberViewHolder(parent.inflate(R.layout.party_member))
    }

    override fun onBindViewHolder(holder: GroupMemberViewHolder, position: Int) {
        holder.bind(data[position], leaderID, null)
        holder.onClickEvent = {
            onUserClicked?.invoke(data[position].id)
        }
    }
}
