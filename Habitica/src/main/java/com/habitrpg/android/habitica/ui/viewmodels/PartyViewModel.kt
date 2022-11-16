package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.helpers.ExceptionHandler
import com.habitrpg.android.habitica.helpers.launchCatching
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class PartyViewModel(initializeComponent: Boolean) : GroupViewModel(initializeComponent) {
    constructor() : this(true)

    internal val isQuestActive: Boolean
        get() = getGroupData().value?.quest?.active == true

    internal val isUserOnQuest: Boolean
        get() = !(
            getGroupData().value?.quest?.members?.none { it.key == user.value?.id }
                ?: true
            )

    private val membersFlow = groupIDFlow
        .filterNotNull()
        .flatMapLatest { socialRepository.getPartyMembers(it) }
    private val members = membersFlow.asLiveData()

    init {
        groupViewType = GroupViewType.PARTY
    }

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    fun getMembersData() = members

    fun acceptQuest() {
        groupID?.let { groupID ->
            viewModelScope.launchCatching {
                socialRepository.acceptQuest(null, groupID)
                socialRepository.retrieveGroup(groupID)
                userRepository.retrieveUser()
            }
        }
    }

    fun rejectQuest() {
        groupID?.let { groupID ->
            viewModelScope.launchCatching {
                socialRepository.rejectQuest(null, groupID)
                socialRepository.retrieveGroup(groupID)
                userRepository.retrieveUser()
            }
        }
    }

    fun showParticipantButtons(): Boolean {
        val user = user.value
        return !(user?.party == null || user.party?.quest == null) && !isQuestActive && user.party?.quest?.RSVPNeeded == true
    }

    fun loadPartyID() {
        viewModelScope.launch(ExceptionHandler.coroutine()) {
            userRepository.getUser()
                .map { it?.party?.id }
                .distinctUntilChanged()
                .filterNotNull()
                .collect {
                    setGroupID(it)
                }
        }
    }
}
