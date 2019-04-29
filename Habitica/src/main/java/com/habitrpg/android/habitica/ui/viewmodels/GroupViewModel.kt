package com.habitrpg.android.habitica.ui.viewmodels

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.habitrpg.android.habitica.components.AppComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.*
import com.habitrpg.android.habitica.extensions.Optional
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.android.habitica.models.social.Group
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import io.realm.RealmResults
import java.util.*
import javax.inject.Inject


enum class GroupViewType(internal val order: String) {
    PARTY("party"),
    GUILD("guild"),
    TAVERN("tavern")
}

open class GroupViewModel : BaseViewModel() {

    @Inject
    lateinit var socialRepository: SocialRepository

    var groupViewType: GroupViewType? = null

    private val group: MutableLiveData<Group?> by lazy {
        loadGroupFromLocal()
        MutableLiveData<Group?>()
    }

    private val leader: MutableLiveData<Member?> by lazy {
        loadLeaderFromLocal()
        MutableLiveData<Member?>()
    }

    private val isMemberData: MutableLiveData<Boolean?> by lazy {
        loadMembershipFromLocal()
        MutableLiveData<Boolean?>()
    }

    protected val groupIDSubject = BehaviorSubject.create<Optional<String>>()
    var gotNewMessages: Boolean = false

    override fun inject(component: AppComponent) {
        component.inject(this)
    }

    override fun onCleared() {
        socialRepository.close()
        super.onCleared()
    }

    fun setGroupID(groupID: String) {
        groupIDSubject.onNext(groupID.asOptional())
    }

    val groupID: String?
    get() = groupIDSubject.value?.value
    val isMember: Boolean
    get() = isMemberData.value ?: false

    fun getGroupData(): LiveData<Group?> = group
    fun getLeaderData(): LiveData<Member?> = leader
    fun getIsMemberData(): LiveData<Boolean?> = isMemberData

    private fun loadGroupFromLocal() {
        disposable.add(groupIDSubject.toFlowable(BackpressureStrategy.LATEST)
                .filterOptionalDoOnEmpty { group.value = null }
                .flatMap { socialRepository.getGroup(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { group.value = it }, RxErrorHandler.handleEmptyError()))
    }

    private fun loadLeaderFromLocal() {
        disposable.add(groupIDSubject.toFlowable(BackpressureStrategy.LATEST)
                .filterOptionalDoOnEmpty { group.value = null }
                .flatMap { socialRepository.getGroup(it) }
                .distinctUntilChanged { group1, group2 -> group1.id == group2.id }
                .flatMap { socialRepository.getMember(it.leaderID) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { leader.value = it }, RxErrorHandler.handleEmptyError()))
    }

    private fun loadMembershipFromLocal() {
        disposable.add(groupIDSubject.toFlowable(BackpressureStrategy.LATEST)
                .filterOptionalDoOnEmpty { group.value = null }
                .flatMap { socialRepository.getGroupMemberships() }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer {
                    isMemberData.value = it.firstOrNull { membership -> membership.groupID == groupID } != null
                }, RxErrorHandler.handleEmptyError()))
    }

    fun getChatMessages(): Flowable<RealmResults<ChatMessage>> {
        return groupIDSubject.toFlowable(BackpressureStrategy.BUFFER)
                .filterMapEmpty()
                .flatMapMaybe { socialRepository.getGroupChat(it).firstElement() }
    }

    fun retrieveGroup(function: (() -> Unit)?) {
        disposable.add(socialRepository.retrieveGroup("party")
                .filter { groupViewType == GroupViewType.PARTY }
                .flatMap { group1 ->
                    socialRepository.retrieveGroupMembers(group1.id, true)
                }
                .doOnComplete { function?.invoke() }
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }

    fun inviteToGroup(inviteData: HashMap<String, Any>) {
        disposable.add(socialRepository.inviteToGroup(group.value?.id ?: "", inviteData)
                .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }

    fun updateOrCreateGroup(bundle: Bundle?) {
        if (group.value == null) {
            socialRepository.createGroup(bundle?.getString("name"),
                    bundle?.getString("description"),
                    bundle?.getString("leader"),
                    bundle?.getString("groupType"),
                    bundle?.getString("privacy"),
                    bundle?.getBoolean("leaderCreateChallenge"))
        } else {
            disposable.add(socialRepository.updateGroup(group.value, bundle?.getString("name"),
                    bundle?.getString("description"),
                    bundle?.getString("leader"),
                    bundle?.getBoolean("leaderCreateChallenge"))
                    .subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        }
    }

    fun leaveGroup(function: (() -> Unit)? = null) {
        disposable.add(socialRepository.leaveGroup(this.group.value?.id ?: "")
                .flatMap { userRepository.retrieveUser(false, true) }
                .subscribe(Consumer {
                    function?.invoke()
                }, RxErrorHandler.handleEmptyError()))
    }

    fun joinGroup(id: String? = null, function: (() -> Unit)? = null) {
        disposable.add(socialRepository.joinGroup(id ?: groupID).subscribe(Consumer {
            function?.invoke()
        }, RxErrorHandler.handleEmptyError()))
    }

    fun rejectGroupInvite(id: String? = null) {
        groupID?.let {
            disposable.add(socialRepository.rejectGroupInvite(id ?: it).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
        }
    }

    fun markMessagesSeen() {
        groupIDSubject.value?.value.notNull {
            if (groupViewType != GroupViewType.TAVERN && it.isNotEmpty() && gotNewMessages) {
                socialRepository.markMessagesSeen(it)
            }
        }
    }

    fun likeMessage(message: ChatMessage) {
        disposable.add(socialRepository.likeMessage(message).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }

    fun deleteMessage(chatMessage: ChatMessage) {
        disposable.add(socialRepository.deleteMessage(chatMessage).subscribe(Consumer { }, RxErrorHandler.handleEmptyError()))
    }

    fun postGroupChat(chatText: String, onComplete: () -> Unit?) {
        groupIDSubject.value?.value.notNull {
            socialRepository.postGroupChat(it, chatText).subscribe(Consumer {
                onComplete()
            }, RxErrorHandler.handleEmptyError())
        }
    }

    fun retrieveGroupChat(onComplete: () -> Unit) {
        val groupID = groupIDSubject.value?.value
        if (groupID.isNullOrEmpty()) {
            onComplete()
            return
        }
        disposable.add(socialRepository.retrieveGroupChat(groupID).subscribe(Consumer {
            onComplete()
        }, RxErrorHandler.handleEmptyError()))
    }

    fun updateGroup(bundle: Bundle?) {
        disposable.add(socialRepository.updateGroup(group.value,
                bundle?.getString("name"),
                bundle?.getString("description"),
                bundle?.getString("leader"),
                bundle?.getBoolean("leaderCreateChallenge"))
                .subscribe(Consumer {}, RxErrorHandler.handleEmptyError()))
    }
}