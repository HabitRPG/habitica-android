package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PositionalDataSource
import androidx.paging.toLiveData
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.toFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil

@HiltViewModel
class InboxViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    userRepository: UserRepository,
    userViewModel: MainUserViewModel,
    val socialRepository: SocialRepository
) : BaseViewModel(userRepository, userViewModel) {
    val recipientID: String? = savedStateHandle.get("userID")
    val recipientUsername: String? = savedStateHandle.get("username")

    private var memberIDFlow = MutableStateFlow<String?>(null)
    val memberIDState: StateFlow<String?> = memberIDFlow

    private val config = PagedList.Config.Builder()
        .setPageSize(10)
        .setEnablePlaceholders(false)
        .build()

    private val dataSourceFactory = MessagesDataSourceFactory(socialRepository, recipientID, ChatMessage())
    val messages: LiveData<PagedList<ChatMessage>> = dataSourceFactory.toLiveData(config)
    private val member = memberIDFlow
        .filterNotNull()
        .flatMapLatest { socialRepository.retrieveMember(it).toFlow() }
        .asLiveData()
    fun getMemberData(): LiveData<Member?> = member

    fun setMemberID(memberID: String) {
        if (memberID == memberIDState.value) return
        memberIDFlow.value = memberID
    }

    val memberID: String?
        get() = memberIDFlow.value

    fun invalidateDataSource() {
        dataSourceFactory.sourceLiveData.value?.invalidate()
    }

    init {
        if (recipientID?.isNotBlank() == true) {
            setMemberID(recipientID)
        } else if (recipientUsername?.isNotBlank() == true) {
            viewModelScope.launch(ExceptionHandler.coroutine()) {
                val member = socialRepository.retrieveMemberWithUsername(recipientUsername, false)
                setMemberID(member?.id ?: "")
                invalidateDataSource()
                dataSourceFactory.updateRecipientID(memberID)
            }
        }
    }
}

class MessagesDataSource(
    val socialRepository: SocialRepository,
    var recipientID: String?,
    var footer: ChatMessage?
) :
    PositionalDataSource<ChatMessage>() {
    private var lastFetchWasEnd = false
    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ChatMessage>) {
        if (lastFetchWasEnd) {
            callback.onResult(emptyList())
            return
        }
        MainScope().launch(Dispatchers.Main.immediate) {
            if (recipientID?.isNotBlank() != true) { return@launch }
            val page = ceil(params.startPosition.toFloat() / params.loadSize.toFloat()).toInt()
            val messages = socialRepository.retrieveInboxMessages(recipientID ?: "", page) ?: return@launch
            if (messages.size < 10) {
                lastFetchWasEnd = true
                callback.onResult(messages)
            } else {
                callback.onResult(messages)
            }
        }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<ChatMessage>) {
        lastFetchWasEnd = false
        MainScope().launch(Dispatchers.Main.immediate) {
            socialRepository.getInboxMessages(recipientID)
                .map { socialRepository.getUnmanagedCopy(it) }
                .take(1)
                .flatMapLatest {
                    if (it.isEmpty()) {
                        if (recipientID?.isNotBlank() != true) { return@flatMapLatest flowOf(it) }
                        val messages = socialRepository.retrieveInboxMessages(recipientID ?: "", 0) ?: return@flatMapLatest emptyFlow()
                        if (messages.size < 10) {
                            lastFetchWasEnd = true
                        }
                        flowOf(messages)
                    } else {
                        flowOf(it)
                    }
                }
                .collect {
                    if (it.size < 10 && footer != null) {
                        callback.onResult(it.plusElement(footer!!), 0)
                    } else {
                        callback.onResult(it, 0)
                    }
                }
        }
    }
}

class MessagesDataSourceFactory(
    val socialRepository: SocialRepository,
    var recipientID: String?,
    val footer: ChatMessage?
) :
    DataSource.Factory<Int, ChatMessage>() {
    val sourceLiveData = MutableLiveData<MessagesDataSource>()
    var latestSource: MessagesDataSource = MessagesDataSource(socialRepository, recipientID, footer)

    fun updateRecipientID(newID: String?) {
        recipientID = newID
        latestSource.recipientID = newID
    }

    override fun create(): DataSource<Int, ChatMessage> {
        latestSource = MessagesDataSource(socialRepository, recipientID, footer)
        sourceLiveData.postValue(latestSource)
        return latestSource
    }
}
