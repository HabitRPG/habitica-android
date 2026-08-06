package com.habitrpg.android.habitica.data.implementation

import android.content.Context
import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ContentRepository
import com.habitrpg.android.habitica.data.local.ContentLocalRepository
import com.habitrpg.android.habitica.data.local.InventoryLocalRepository
import com.habitrpg.android.habitica.models.ContentGear
import com.habitrpg.android.habitica.models.ContentResult
import com.habitrpg.android.habitica.models.WorldState
import com.habitrpg.android.habitica.models.inventory.Equipment
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class ContentRepositoryImplTest :
    WordSpec({
        lateinit var repository: ContentRepository
        val localRepository = mockk<ContentLocalRepository>(relaxed = true)
        val apiClient = mockk<ApiClient>()
        val authenticationHandler = mockk<AuthenticationHandler>()
        val context = mockk<Context>(relaxed = true)
        val inventoryLocalRepository = mockk<InventoryLocalRepository>()
        beforeEach {
            every { authenticationHandler.currentUserID } returns "user-1"
            repository = ContentRepositoryImpl(localRepository, apiClient, context, authenticationHandler, inventoryLocalRepository)
        }
        afterEach { clearAllMocks() }
        "retrieveContent" should {
            "fetch, add the mystery item, and save on first call even when not forced" {
                val content = ContentResult()
                coEvery { apiClient.getContent() } returns content
                val result = repository.retrieveContent(false)
                result shouldBe content
                content.special.isNotEmpty() shouldBe true
            }

            "return null and save nothing when the API returns nothing" {
                coEvery { apiClient.getContent() } returns null
                val result = repository.retrieveContent(false)
                result shouldBe null
            }

            "skip a second unforced call within the cache window" {
                val content = ContentResult()
                coEvery { apiClient.getContent() } returns content
                repository.retrieveContent(false)
                val result = repository.retrieveContent(false)
                result shouldBe null
                coVerify(exactly = 1) { apiClient.getContent() }
            }

            "always refetch when forced" {
                val content = ContentResult()
                coEvery { apiClient.getContent() } returns content
                repository.retrieveContent(true)
                val result = repository.retrieveContent(true)
                result shouldBe content
                coVerify(exactly = 2) { apiClient.getContent() }
            }

            "inherit the previously known owned flag for gear whose catalog entry doesn't carry one" {
                val freshGear = Equipment().apply { key = "sword_1"; owned = null }
                val content = ContentResult().apply { gear = ContentGear().apply { flat = io.realm.RealmList(freshGear) } }
                val knownGear = Equipment().apply { key = "sword_1"; owned = true }
                coEvery { apiClient.getContent() } returns content
                coEvery { inventoryLocalRepository.getEquipment(listOf("sword_1")) } returns flowOf(listOf(knownGear))
                repository.retrieveContent(true)
                freshGear.owned shouldBe true
            }
        }
        "retrieveWorldState" should {
            "return null and save nothing when the API returns nothing" {
                coEvery { apiClient.getWorldState() } returns null
                val result = repository.retrieveWorldState(false)
                result shouldBe null
            }

            "save and return the state from the API" {
                val state = WorldState()
                coEvery { apiClient.getWorldState() } returns state
                val result = repository.retrieveWorldState(true)
                result shouldBe state
            }
        }
        "getWorldState" should {
            "delegate to the local repository" {
                val state = WorldState()
                every { localRepository.getWorldState() } returns flowOf(state)
                repository.getWorldState().collect { it shouldBe state }
            }
        }
    })
