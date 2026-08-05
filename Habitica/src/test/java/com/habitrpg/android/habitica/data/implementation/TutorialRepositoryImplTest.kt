package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.TutorialRepository
import com.habitrpg.android.habitica.data.local.TutorialLocalRepository
import com.habitrpg.android.habitica.models.TutorialStep
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

class TutorialRepositoryImplTest :
    WordSpec({
        lateinit var repository: TutorialRepository
        val localRepository = mockk<TutorialLocalRepository>()
        val apiClient = mockk<ApiClient>()
        val authenticationHandler = mockk<AuthenticationHandler>()
        beforeEach {
            repository = TutorialRepositoryImpl(localRepository, apiClient, authenticationHandler)
        }
        afterEach { clearAllMocks() }
        "getTutorialStep" should {
            "delegate to the local repository" {
                val step = TutorialStep()
                every { localRepository.getTutorialStep("intro") } returns flowOf(step)
                repository.getTutorialStep("intro").collect { it shouldBe step }
            }
        }
        "getTutorialSteps" should {
            "delegate to the local repository with the given keys" {
                val steps = listOf(TutorialStep(), TutorialStep())
                every { localRepository.getTutorialSteps(listOf("intro", "tasks")) } returns flowOf(steps)
                repository.getTutorialSteps(listOf("intro", "tasks")).collect { it shouldBe steps }
            }
        }
    })
