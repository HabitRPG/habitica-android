package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.events.ShowSnackbarEvent
import com.habitrpg.android.habitica.extensions.filterOptionalDoOnEmpty
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.ui.views.HabiticaSnackbar
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.realm.RealmResults
import org.greenrobot.eventbus.EventBus

class PartyViewModel: GroupViewModel() {

    internal val isQuestActive: Boolean
        get() = getGroupData().value?.quest?.active == true

    internal val isUserOnQuest: Boolean
        get() = !(getGroupData().value?.quest?.members?.none { it.key == getUserData().value?.id }
                ?: true)

    private val members: MutableLiveData<RealmResults<Member>?> by lazy {
        MutableLiveData<RealmResults<Member>?>()
    }

    init {
        groupViewType = GroupViewType.PARTY
        loadMembersFromLocal()
    }

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    fun getMembersData(): LiveData<RealmResults<Member>?> = members

    private fun loadMembersFromLocal() {
        disposable.add(groupIDSubject.toFlowable(BackpressureStrategy.LATEST)
                .filterOptionalDoOnEmpty { members.value = null }
                .flatMap { socialRepository.getGroupMembers(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ members.value = it }, RxErrorHandler.handleEmptyError()))
    }

    fun acceptQuest() {
        groupIDSubject.value?.value?.let { groupID ->
            disposable.add(socialRepository.acceptQuest(null, groupID)
                    .flatMap { userRepository.retrieveUser() }
                    .flatMap { socialRepository.retrieveGroup(groupID) }
                    .subscribe({
                        val event = ShowSnackbarEvent()
                        event.type = HabiticaSnackbar.SnackbarDisplayType.SUCCESS
                        event.text = "Quest invitation accepted"
                        EventBus.getDefault().post(event)
                    }, RxErrorHandler.handleEmptyError()))
        }
    }

    fun rejectQuest() {
        groupIDSubject.value?.value?.let { groupID ->
            disposable.add(socialRepository.rejectQuest(null, groupID)
                    .flatMap { userRepository.retrieveUser() }
                    .flatMap { socialRepository.retrieveGroup(groupID) }
                    .subscribe({
                        val event = ShowSnackbarEvent()
                        event.type = HabiticaSnackbar.SnackbarDisplayType.FAILURE
                        event.text = "Quest invitation rejected"
                        EventBus.getDefault().post(event)
                    }, RxErrorHandler.handleEmptyError()))
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
                .subscribe({ groupID ->
                    setGroupID(groupID)
                }, RxErrorHandler.handleEmptyError()))
    }
}