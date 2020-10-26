package com.habitrpg.android.habitica.data.implementation

import android.content.Context
import androidx.preference.PreferenceManager
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.local.ContentLocalRepository
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import io.reactivex.Flowable
import io.realm.RealmList
import java.util.*

abstract class ContentRepositoryImpl<T : ContentLocalRepository>(localRepository: T, apiClient: ApiClient) : BaseRepositoryImpl<T>(localRepository, apiClient), ContentRepository {

    private var lastContentSync = 0L
    private var lastWorldStateSync = 0L

    override fun retrieveContent(context: Context?): Flowable<ContentResult> {
        return retrieveContent(context,false)
    }

    override fun retrieveContent(context: Context?, forced: Boolean): Flowable<ContentResult> {
        val now = Date().time
        return if (forced || now - this.lastContentSync > 300000) {
            lastContentSync = now
            apiClient.content.doOnNext {
                context?.let {context ->
                    it.special = RealmList()
                    it.special.add(SpecialItem.makeMysteryItem(context))
                }
                localRepository.saveContent(it)
            }
        } else {
            Flowable.empty()
        }
    }

    override fun retrieveWorldState(context: Context?): Flowable<WorldState> {
        val now = Date().time
        return if (now - this.lastWorldStateSync > 3600000) {
            lastWorldStateSync = now
            apiClient.worldState.doOnNext {
                localRepository.saveWorldState(it)

                val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
                editor.putString("currentEvent", it.currentEventKey)
                editor.putLong("currentEventStartDate", it.currentEventStartDate?.time ?: 0)
                editor.putLong("currentEventEndDate", it.currentEventEndDate?.time ?: 0)
                editor.apply()
            }
        } else {
            Flowable.empty()
        }
    }
}
