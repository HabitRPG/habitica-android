package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.apiclient.ApiClient
import com.habitrpg.android.habitica.data.CustomizationRepository
import com.habitrpg.android.habitica.data.local.CustomizationLocalRepository
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import kotlinx.coroutines.flow.Flow

class CustomizationRepositoryImpl(
    localRepository: CustomizationLocalRepository,
    apiClient: ApiClient,
    authenticationHandler: AuthenticationHandler,
) : BaseRepositoryImpl<CustomizationLocalRepository>(
        localRepository,
        apiClient,
        authenticationHandler,
    ),
    CustomizationRepository {
    override fun getCustomizations(
        type: String,
        category: String?,
        onlyAvailable: Boolean,
    ): Flow<List<Customization>> {
        return localRepository.getCustomizations(type, category, onlyAvailable)
    }
}
