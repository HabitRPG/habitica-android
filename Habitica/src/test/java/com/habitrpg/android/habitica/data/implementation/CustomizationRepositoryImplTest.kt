package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.CustomizationRepository
import com.habitrpg.android.habitica.data.local.CustomizationLocalRepository
import com.habitrpg.android.habitica.models.inventory.Customization
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class CustomizationRepositoryImplTest :
    WordSpec({
        lateinit var repository: CustomizationRepository
        val localRepository = mockk<CustomizationLocalRepository>()
        val apiClient = mockk<ApiClient>()
        val authenticationHandler = mockk<AuthenticationHandler>()
        beforeEach {
            repository = CustomizationRepositoryImpl(localRepository, apiClient, authenticationHandler)
        }
        afterEach { clearAllMocks() }
        "getCustomizations" should {
            "delegate to the local repository with the given filters" {
                val customization = Customization()
                every { localRepository.getCustomizations("hair", "hairColor", true) } returns flowOf(listOf(customization))
                repository.getCustomizations("hair", "hairColor", true).collect { it shouldBe listOf(customization) }
            }
        }
    })
