package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.local.BaseLocalRepository
import com.habitrpg.android.habitica.models.Tag
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class BaseRepositoryImplTest :
    WordSpec({
        val localRepository = mockk<BaseLocalRepository>(relaxed = true)
        val authenticationHandler = mockk<AuthenticationHandler>()
        val repository = object : BaseRepositoryImpl<BaseLocalRepository>(localRepository, mockk<ApiClient>(), authenticationHandler) {}

        afterEach { clearAllMocks() }

        "currentUserID" should {
            "fall back to an empty id without an authenticated user" {
                every { authenticationHandler.currentUserID } returns null
                repository.currentUserID shouldBe ""
            }
        }

        "local repository" should {
            "be closed and refreshed with the repository" {
                every { localRepository.isClosed } returns true
                repository.close()
                repository.refreshLocalData()
                repository.isClosed shouldBe true
                verify { localRepository.close() }
                verify { localRepository.refreshLocalData() }
            }

            "provide unmanaged copies" {
                val tag = Tag()
                val copy = Tag()
                every { localRepository.getUnmanagedCopy(tag) } returns copy
                every { localRepository.getUnmanagedCopy(listOf(tag)) } returns listOf(copy)
                repository.getUnmanagedCopy(tag) shouldBe copy
                repository.getUnmanagedCopy(listOf(tag)) shouldBe listOf(copy)
            }
        }
    })
