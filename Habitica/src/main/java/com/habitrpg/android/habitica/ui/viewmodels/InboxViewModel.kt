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
import com.habitrpg.android.habitica.extensions.filterOptionalDoOnEmpty
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.members.Member
import com.habitrpg.android.habitica.models.social.ChatMessage
import com.habitrpg.common.habitica.extensions.Optional
import com.habitrpg.common.habitica.extensions.asOptional
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
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

    val dataSourceFactory = MessagesDataSourceFactory(socialRepository, recipientID, ChatMessage())
    val messages: LiveData<PagedList<ChatMessage>> = dataSourceFactory.toLiveData(config)
    private val member: MutableLiveData<Member?> by lazy {
        MutableLiveData<Member?>()
    }
    fun getMemberData(): LiveData<Member?> = member

    private fun loadMemberFromLocal() {
        disposable.add(
            memberIDFlowable
                .filterOptionalDoOnEmpty { member.value = null }
                .flatMap { socialRepository.getMember(it) }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ member.value = it }, RxErrorHandler.handleEmptyError())
        )
    }

    protected var memberIDSubject = BehaviorSubject.create<Optional<String>>()
    val memberIDFlowable: Flowable<Optional<String>> = memberIDSubject.toFlowable(BackpressureStrategy.BUFFER)

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
            socialRepository.getMemberWithUsername(recipientUsername).subscribe(
                {
                    setMemberID(it.id ?: "")
                    member.value = it
                    dataSourceFactory.updateRecipientID(memberIDSubject.value?.value)
                    invalidateDataSource()
                },
                RxErrorHandler.handleEmptyError()
            )
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
            socialRepository.retrieveInboxMessages(recipientID ?: "", page)
                .subscribe(
                    {
                        if (it.size < 10) {
                            lastFetchWasEnd = true
                            callback.onResult(it)
                        } else
                            callback.onResult(it)
                    },
                    RxErrorHandler.handleEmptyError()
                )
        }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<ChatMessage>) {
        lastFetchWasEnd = false
        MainScope().launch(Dispatchers.Main.immediate) {
            socialRepository.getInboxMessages(recipientID)
                .map { socialRepository.getUnmanagedCopy(it) }
                .firstElement()
                .flatMapPublisher {
                    if (it.isEmpty()) {
                        if (recipientID?.isNotBlank() != true) { return@flatMapPublisher Flowable.just(it) }
                        socialRepository.retrieveInboxMessages(recipientID ?: "", 0)
                            .doOnNext { messages ->
                                if (messages.size < 10) {
                                    lastFetchWasEnd = true
                                }
                            }
                    } else {
                        Flowable.just(it)
                    }
                }
                .subscribe(
                    {
                        if (it.size < 10 && footer != null)
                            callback.onResult(it.plusElement(footer!!), 0)
                        else
                            callback.onResult(it, 0)
                    },
                    RxErrorHandler.handleEmptyError()
                )
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

class InboxViewModelFactory(
    private val recipientID: String?,
    private val recipientUsername: String?
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InboxViewModel(recipientID, recipientUsername) as T
    }
}
