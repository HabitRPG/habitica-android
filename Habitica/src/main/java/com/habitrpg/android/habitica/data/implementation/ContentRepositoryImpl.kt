package com.habitrpg.android.habitica.data.implementation

import android.content.Context
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.local.ContentLocalRepository
import com.habitrpg.android.habitica.helpers.AprilFoolsHandler
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import io.reactivex.rxjava3.core.Flowable
import java.util.Date

class ContentRepositoryImpl<T : ContentLocalRepository>(
    localRepository: T,
    apiClient: ApiClient,
    context: Context
) : BaseRepositoryImpl<T>(localRepository, apiClient), ContentRepository {

    private val mysteryItem = SpecialItem.makeMysteryItem(context)

    private var lastContentSync = 0L
    private var lastWorldStateSync = 0L

    override fun retrieveContent(forced: Boolean): Flowable<ContentResult> {
        val now = Date().time
        return if (forced || now - this.lastContentSync > 300000) {
            lastContentSync = now
            apiClient.content.doOnNext {
                it.special.add(mysteryItem)
                localRepository.saveContent(it)
            }
        } else {
            Flowable.just(ContentResult())
        }
    }

    override fun retrieveWorldState(): Flowable<WorldState> {
        val now = Date().time
        return if (now - this.lastWorldStateSync > 3600000) {
            lastWorldStateSync = now
            apiClient.worldState.doOnNext {
                localRepository.saveWorldState(it)
                for (event in it.events) {
                    if (event.aprilFools != null && event.isCurrentlyActive) {
                        AprilFoolsHandler.handle(event.aprilFools, event.end)
                    }
                }
            }
        } else {
            Flowable.just(WorldState())
        }
    }

    override fun getWorldState(): Flowable<WorldState> {
        return localRepository.getWorldState()
    }
}
