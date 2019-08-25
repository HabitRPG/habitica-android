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
import com.habitrpg.android.habitica.helpers.RxErrorHandler
import com.habitrpg.android.habitica.models.social.ChatMessage
import io.reactivex.Flowable
import io.reactivex.functions.Consumer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil


class InboxViewModel(recipientID: String) : BaseViewModel() {
    @Inject
    lateinit var socialRepository: SocialRepository

    private val config = PagedList.Config.Builder()
            .setPageSize(10)
            .setEnablePlaceholders(false)
            .build()

    private val dataSourceFactory = MessagesDataSourceFactory(socialRepository, recipientID)
    val messages: LiveData<PagedList<ChatMessage>> = dataSourceFactory.toLiveData(config)

    override fun inject(component: UserComponent) {
        component.inject(this)
    }

    fun invalidateDataSource() {
        dataSourceFactory.sourceLiveData.value?.invalidate()
    }
}

private class MessagesDataSource(val socialRepository: SocialRepository, val recipientID: String):
        PositionalDataSource<ChatMessage>() {
    private var lastFetchWasEnd = false
    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<ChatMessage>) {
        if (lastFetchWasEnd) {
            callback.onResult(emptyList())
            return
        }
        GlobalScope.launch(Dispatchers.Main.immediate) {
            val page = ceil(params.startPosition.toFloat() / params.loadSize.toFloat()).toInt()
            socialRepository.retrieveInboxMessages(recipientID, page)
                    .subscribe(Consumer {
                        if (it.size != 10) lastFetchWasEnd = true
                        callback.onResult(it)
                    }, RxErrorHandler.handleEmptyError())
        }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<ChatMessage>) {
        lastFetchWasEnd = false
        GlobalScope.launch(Dispatchers.Main.immediate) {
            socialRepository.getInboxMessages(recipientID).firstElement()
                    .flatMapPublisher {
                        if (it.size == 0) {
                            socialRepository.retrieveInboxMessages(recipientID, 0)
                                    .doOnNext {
                                        messages -> if (messages.size != 10) lastFetchWasEnd = true
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

private class MessagesDataSourceFactory(val socialRepository: SocialRepository, val recipientID: String) :
        DataSource.Factory<Int, ChatMessage>() {
    val sourceLiveData = MutableLiveData<MessagesDataSource>()
    var latestSource: MessagesDataSource = MessagesDataSource(socialRepository, recipientID)
    override fun create(): DataSource<Int, ChatMessage> {
        latestSource = MessagesDataSource(socialRepository, recipientID)
        sourceLiveData.postValue(latestSource)
        return latestSource
    }
}

class InboxViewModelFactory(private val recipientID: String) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return InboxViewModel(recipientID) as T
    }
}