package com.habitrpg.android.habitica.data.implementation

import android.content.Context
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.local.ContentLocalRepository
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.inventory.SpecialItem
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.Date

class ContentRepositoryImpl<T : ContentLocalRepository>(
    localRepository: T,
    apiClient: ApiClient,
    context: Context,
    authenticationHandler: AuthenticationHandler,
    private val inventoryLocalRepository: InventoryLocalRepository,
) : BaseRepositoryImpl<T>(localRepository, apiClient, authenticationHandler),
    ContentRepository {
    private val mysteryItem = SpecialItem.makeMysteryItem(context)

    private var lastContentSync = 0L
    private var lastWorldStateSync = 0L

    override suspend fun retrieveContent(forced: Boolean): ContentResult? {
        val now = Date().time
        if (forced || now - this.lastContentSync > 300000) {
            val content = apiClient.getContent() ?: return null
            lastContentSync = now
            content.special.add(mysteryItem)
            preserveOwnedFlags(content)
            localRepository.saveContent(content)
            return content
        }
        return null
    }

    // The gear catalog doesn't carry ownership info of its own; a freshly-parsed item's `owned` is
    // left null (see EquipmentListDeserializer) so a catalog refresh doesn't wipe what's already known.
    private suspend fun preserveOwnedFlags(content: ContentResult) {
        val items = content.gear?.flat?.filter { it.owned == null } ?: return
        if (items.isEmpty()) return
        val known = inventoryLocalRepository.getEquipment(items.map { it.key ?: "" }).firstOrNull() ?: return
        items.forEach { item -> item.owned = known.firstOrNull { it.key == item.key }?.owned }
    }

    override suspend fun retrieveWorldState(forced: Boolean): WorldState? {
        val now = Date().time
        if (forced || now - this.lastWorldStateSync > 300000) {
            val state = apiClient.getWorldState() ?: return null
            lastWorldStateSync = now
            localRepository.save(state)
            return state
        }
        return null
    }

    override fun getWorldState(): Flow<WorldState> = localRepository.getWorldState()
}
