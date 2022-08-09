package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.extensions.filterOptionalDoOnEmpty
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.members.Member
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
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

    private val members: MutableLiveData<List<Member>?> by lazy {
        MutableLiveData<List<Member>?>()
    }

    init {
        groupViewType = GroupViewType.PARTY
        loadMembersFromLocal()
    }

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    fun getMembersData(): LiveData<List<Member>?> = members

    private fun loadMembersFromLocal() {
        disposable.add(
            groupIDSubject.toFlowable(BackpressureStrategy.LATEST)
                .distinctUntilChanged()
                .filterOptionalDoOnEmpty { members.value = null }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    viewModelScope.launch {
                        socialRepository.getGroupMembers(it)
                            .collect {
                                members.value = it
                            }
                    }}, RxErrorHandler.handleEmptyError())
        )
    }

    fun acceptQuest() {
        groupIDSubject.value?.value?.let { groupID ->
            disposable.add(
                socialRepository.acceptQuest(null, groupID)
                    .flatMap { userRepository.retrieveUser() }
                    .flatMap { socialRepository.retrieveGroup(groupID) }
                    .subscribe(
                        {},
                        RxErrorHandler.handleEmptyError()
                    )
            )
        }
    }

    fun rejectQuest() {
        groupIDSubject.value?.value?.let { groupID ->
            disposable.add(
                socialRepository.rejectQuest(null, groupID)
                    .flatMap { userRepository.retrieveUser() }
                    .flatMap { socialRepository.retrieveGroup(groupID) }
                    .subscribe(
                        {},
                        RxErrorHandler.handleEmptyError()
                    )
            )
        }
    }

    fun showParticipantButtons(): Boolean {
        val user = user.value
        return !(user?.party == null || user.party?.quest == null) && !isQuestActive && user.party?.quest?.RSVPNeeded == true
    }

    fun loadPartyID() {
        disposable.add(
            userRepository.getUserFlowable()
                .map { it.party?.id ?: "" }
                .distinctUntilChanged()
                .subscribe(
                    { groupID ->
                        setGroupID(groupID)
                    },
                    RxErrorHandler.handleEmptyError()
                )
        )
    }
}
