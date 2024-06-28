package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.liveData
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.data.UserRepository
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.common.habitica.helpers.ExceptionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.toFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InboxViewModel
@Inject
constructor(
    savedStateHandle: SavedStateHandle,
    userRepository: UserRepository,
    userViewModel: MainUserViewModel,
    val socialRepository: SocialRepository
) : BaseViewModel(userRepository, userViewModel) {
    val recipientID: String? = savedStateHandle["userID"]
    val recipientUsername: String? = savedStateHandle["username"]

    private var memberIDFlow = MutableStateFlow<String?>(null)
    val memberIDState: StateFlow<String?> = memberIDFlow

    private val config =
        PagingConfig(pageSize = 10, enablePlaceholders = false)
    val messages: LiveData<PagingData<ChatMessage>> =
        Pager(
            config,
            null
        ) {
            MessagesDataSource(socialRepository, recipientID, ChatMessage())
        }.liveData
    private val member =
        memberIDFlow
            .filterNotNull()
            .flatMapLatest { socialRepository.retrieveMember(it).toFlow() }
            .asLiveData()

    fun setMemberID(memberID: String) {
        if (memberID == memberIDState.value) return
        memberIDFlow.value = memberID
    }

    val memberID: String?
        get() = memberIDFlow.value

    fun invalidateDataSource() {
    }

    init {
        if (recipientID?.isNotBlank() == true) {
            setMemberID(recipientID)
        } else if (recipientUsername?.isNotBlank() == true) {
            viewModelScope.launch(ExceptionHandler.coroutine()) {
                val member = socialRepository.retrieveMember(recipientUsername, false)
                setMemberID(member?.id ?: "")
                invalidateDataSource()

                // dataSourceFactory.updateRecipientID(memberID)
            }
        }
    }
}

class MessagesDataSource(
    val socialRepository: SocialRepository,
    var recipientID: String?,
    var footer: ChatMessage?
) : PagingSource<Int, ChatMessage>() {
    private var lastFetchWasEnd = false

    override fun getRefreshKey(state: PagingState<Int, ChatMessage>): Int? {
        TODO("Not yet implemented")
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ChatMessage> {
        if (lastFetchWasEnd) {
            return LoadResult.Page(emptyList(), null, null)
        }
        if (recipientID?.isNotBlank() != true) {
            return LoadResult.Error(Exception("Recipient ID is blank"))
        }
        val page = params.key ?: 0
        val messages =
            socialRepository.retrieveInboxMessages(recipientID ?: "", page) ?: return LoadResult.Error(
                Exception("Failed to retrieve messages")
            )
        val nextPage = if (messages.size < 10) {
            null
        } else {
            page + 1
        }
        return LoadResult.Page(messages, if (page > 0) page - 1 else null, nextPage)
    }
}
