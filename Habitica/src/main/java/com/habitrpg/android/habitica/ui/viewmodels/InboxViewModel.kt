package com.habitrpg.android.habitica.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.paging.DataSource
import androidx.paging.PagedList
import androidx.paging.PositionalDataSource
import androidx.paging.toLiveData
import com.habitrpg.android.habitica.components.UserComponent
import com.habitrpg.android.habitica.data.SocialRepository
import com.habitrpg.android.habitica.extensions.Optional
import com.habitrpg.android.habitica.extensions.asOptional
import com.habitrpg.android.habitica.extensions.filterOptionalDoOnEmpty
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.shared.habitica.models.members.Member
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil


class InboxViewModel(recipientID: String?, recipientUsername: String?) : BaseViewModel() {
    @Inject
    lateinit var socialRepository: SocialRepository

    private val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setEnablePlaceholders(false)
            .build()

    private val dataSourceFactory = MessagesDataSourceFactory(socialRepository, recipientID)
    val messages: LiveData<PagedList<ChatMessage>> = dataSourceFactory.toLiveData(config)
    private val member: MutableLiveData<Member?> by lazy {
        MutableLiveData<Member?>()
    }

    fun getMemberData(): LiveData<Member?> = member

    private fun loadMemberFromLocal() {
        disposable.add(memberIDFlowable
                .filterOptionalDoOnEmpty { member.value = null }
                .flatMap { socialRepository.getMember(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(Consumer { member.value = it }, RxErrorHandler.handleEmptyError()))
    }

    protected var memberIDSubject = BehaviorSubject.create<Optional<String>>()
    val memberIDFlowable = memberIDSubject.toFlowable(BackpressureStrategy.BUFFER)

    fun setMemberID(groupID: String) {
        if (groupID == memberIDSubject.value?.value) return
        memberIDSubject.onNext(groupID.asOptional())
    }

    val memberID: String?
        get() = memberIDSubject.value?.value

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    fun invalidateDataSource() {
        dataSourceFactory.sourceLiveData.value?.invalidate()
    }

    init {
        if (recipientID?.isNotBlank() == true) {
            setMemberID(recipientID)
            loadMemberFromLocal()
        } else if (recipientUsername?.isNotBlank() == true) {
            socialRepository.getMemberWithUsername(recipientUsername).subscribe(Consumer {
                setMemberID(it.id ?: "")
                member.value = it
                dataSourceFactory.updateRecipientID(memberIDSubject.value?.value)
                invalidateDataSource()
            }, RxErrorHandler.handleEmptyError())
        }
    }
}

private class MessagesDataSource(val socialRepository: SocialRepository, var recipientID: String?) :
        PositionalDataSource<ChatMessage>() {
    private var lastFetchWasEnd = false
    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ChatMessage>) {
        if (lastFetchWasEnd) {
            callback.onResult(emptyList())
            return
        }
        GlobalScope.launch(Dispatchers.Main.immediate) {
            if (recipientID?.isNotBlank() != true) {
                return@launch
            }
            val page = ceil(params.startPosition.toFloat() / params.loadSize.toFloat()).toInt()
            socialRepository.retrieveInboxMessages(recipientID ?: "", page)
                    .subscribe(Consumer {
                        if (it.size != 10) lastFetchWasEnd = true
                        callback.onResult(it)
                    }, RxErrorHandler.handleEmptyError())
        }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<ChatMessage>) {
        lastFetchWasEnd = false
        GlobalScope.launch(Dispatchers.Main.immediate) {
            socialRepository.getInboxMessages(recipientID)
                    .map { socialRepository.getUnmanagedCopy(it) }
                    .firstElement()
                    .flatMapPublisher {
                        if (it.isEmpty()) {
                            if (recipientID?.isNotBlank() != true) {
                                return@flatMapPublisher Flowable.just(it)
                            }
                            socialRepository.retrieveInboxMessages(recipientID ?: "", 0)
                                    .doOnNext { messages ->
                                        if (messages.size != 10) lastFetchWasEnd = true
                                    }
                        } else {
                            Flowable.just(it)
                        }
                    }
                    .subscribe(Consumer {
                        callback.onResult(it, 0)
                    }, RxErrorHandler.handleEmptyError())
        }
    }
}

private class MessagesDataSourceFactory(val socialRepository: SocialRepository, var recipientID: String?) :
        DataSource.Factory<Int, ChatMessage>() {
    val sourceLiveData = MutableLiveData<MessagesDataSource>()
    var latestSource: MessagesDataSource = MessagesDataSource(socialRepository, recipientID)

    fun updateRecipientID(newID: String?) {
        recipientID = newID
        latestSource.recipientID = newID
    }

    override fun create(): DataSource<Int, ChatMessage> {
        latestSource = MessagesDataSource(socialRepository, recipientID)
        sourceLiveData.postValue(latestSource)
        return latestSource
    }
}

class InboxViewModelFactory(private val recipientID: String?, private val recipientUsername: String?) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InboxViewModel(recipientID, recipientUsername) as T
    }
}