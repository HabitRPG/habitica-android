package com.habitrpg.android.habitica.ui.viewmodels

import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.extensions.notNull
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import io.reactivex.functions.Consumer

class PartyViewModel: GroupViewModel() {

    internal val isQuestActive: Boolean
        get() = getGroupData().value?.quest?.active == true

    init {
        groupViewType = GroupViewType.PARTY
    }

    override fun inject(component: AppComponent) {
        component.inject(this)
    }

    fun acceptQuest() {
        groupIDSubject.value?.value.notNull {
            disposable.add(socialRepository.acceptQuest(null, it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        }
    }

    fun rejectQuest() {
        groupIDSubject.value?.value.notNull {
            disposable.add(socialRepository.rejectQuest(null, it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        }
    }

    fun showParticipantButtons(): Boolean {
        val user = getUserData().value
        return !(user?.party == null || user.party?.quest == null) && !isQuestActive && user.party?.quest?.RSVPNeeded == true
    }

    fun loadPartyID() {
        disposable.add(userRepository.getUser()
                .map { it.party?.id ?: "" }
                .distinctUntilChanged()
                .subscribe(Consumer { groupID ->
                    setGroupID(groupID)
                }, RxErrorHandler.handleEmptyError()))
    }
}