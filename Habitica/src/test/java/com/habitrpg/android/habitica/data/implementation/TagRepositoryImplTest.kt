package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TagRepository
import com.habitrpg.android.habitica.data.local.TagLocalRepository
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf

class TagRepositoryImplTest :
    WordSpec({
        lateinit var repository: TagRepository
        val localRepository = mockk<TagLocalRepository>()
        val apiClient = mockk<ApiClient>()
        val authenticationHandler = mockk<AuthenticationHandler>()
        beforeEach {
            every { authenticationHandler.currentUserID } returns "user-1"
            repository = TagRepositoryImpl(localRepository, apiClient, authenticationHandler)
        }
        afterEach { clearAllMocks() }
        "getTags" should {
            "delegate to the local repository for the current user" {
                every { authenticationHandler.userIDFlow } returns flowOf("user-1")
                every { localRepository.getTags("user-1") } returns flowOf(listOf())
                repository.getTags().collect { }
                verify { localRepository.getTags("user-1") }
            }

            "delegate to the local repository for an explicit user" {
                val tag = Tag().apply { id = "tag-1" }
                every { localRepository.getTags("user-2") } returns flowOf(listOf(tag))
                repository.getTags("user-2").collect { tags -> tags shouldBe listOf(tag) }
            }
        }
        "createTag" should {
            "save the tag returned by the API with the current user ID" {
                val tag = Tag().apply { id = "tag-1" }
                val savedTag = Tag().apply { id = "tag-1" }
                coEvery { apiClient.createTag(tag) } returns savedTag
                every { localRepository.save(savedTag) } returns Unit
                val result = repository.createTag(tag)
                result shouldBe savedTag
                result?.userId shouldBe "user-1"
                verify { localRepository.save(savedTag) }
            }

            "return null and skip saving when the API returns nothing" {
                val tag = Tag().apply { id = "tag-1" }
                coEvery { apiClient.createTag(tag) } returns null
                val result = repository.createTag(tag)
                result shouldBe null
                verify(exactly = 0) { localRepository.save(any<Tag>()) }
            }
        }
        "updateTag" should {
            "save the tag returned by the API with the current user ID" {
                val tag = Tag().apply { id = "tag-1" }
                val savedTag = Tag().apply { id = "tag-1" }
                coEvery { apiClient.updateTag("tag-1", tag) } returns savedTag
                every { localRepository.save(savedTag) } returns Unit
                val result = repository.updateTag(tag)
                result shouldBe savedTag
                result?.userId shouldBe "user-1"
                verify { localRepository.save(savedTag) }
            }
        }
        "deleteTag" should {
            "delete remotely and locally" {
                coEvery { apiClient.deleteTag("tag-1") } returns null
                every { localRepository.deleteTag("tag-1") } returns Unit
                repository.deleteTag("tag-1")
                coVerify { apiClient.deleteTag("tag-1") }
                verify { localRepository.deleteTag("tag-1") }
            }
        }
        "createTags" should {
            "create every tag and drop nulls" {
                val tag1 = Tag().apply { id = "tag-1" }
                val tag2 = Tag().apply { id = "tag-2" }
                coEvery { apiClient.createTag(tag1) } returns tag1
                coEvery { apiClient.createTag(tag2) } returns null
                every { localRepository.save(any<Tag>()) } returns Unit
                val result = repository.createTags(listOf(tag1, tag2))
                result shouldBe listOf(tag1)
            }
        }
        "deleteTags" should {
            "delete every tag id" {
                coEvery { apiClient.deleteTag(any()) } returns null
                every { localRepository.deleteTag(any()) } returns Unit
                repository.deleteTags(listOf("tag-1", "tag-2"))
                coVerify { apiClient.deleteTag("tag-1") }
                coVerify { apiClient.deleteTag("tag-2") }
            }
        }
    })
