package com.habitrpg.android.habitica.data.implementation

import com.habitrpg.android.habitica.data.ApiClient
import com.habitrpg.android.habitica.data.ChallengeRepository
import com.habitrpg.android.habitica.data.local.ChallengeLocalRepository
import com.habitrpg.android.habitica.models.social.Challenge
import com.habitrpg.android.habitica.models.tasks.Task
import com.habitrpg.android.habitica.models.tasks.TaskList
import com.habitrpg.android.habitica.modules.AuthenticationHandler
import com.habitrpg.shared.habitica.models.tasks.TaskType
import io.kotest.core.spec.style.WordSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

class ChallengeRepositoryImplTest :
    WordSpec({
        lateinit var repository: ChallengeRepository
        val localRepository = mockk<ChallengeLocalRepository>()
        val apiClient = mockk<ApiClient>()
        val authenticationHandler = mockk<AuthenticationHandler>()
        beforeEach {
            every { authenticationHandler.currentUserID } returns "user-1"
            repository = ChallengeRepositoryImpl(localRepository, apiClient, authenticationHandler)
        }
        afterEach { clearAllMocks() }
        "retrieveChallenge" should {
            "return null and save nothing when the API returns nothing" {
                coEvery { apiClient.getChallenge("challenge-1") } returns null
                val result = repository.retrieveChallenge("challenge-1")
                result shouldBe null
                verify(exactly = 0) { localRepository.save(any<Challenge>()) }
            }

            "save and return the challenge from the API" {
                val challenge = Challenge().apply { id = "challenge-1" }
                coEvery { apiClient.getChallenge("challenge-1") } returns challenge
                every { localRepository.save(challenge) } returns Unit
                val result = repository.retrieveChallenge("challenge-1")
                result shouldBe challenge
                verify { localRepository.save(challenge) }
            }
        }
        "retrieveChallengeTasks" should {
            "tag every task with the challenge id and save them" {
                val task1 = Task().apply { id = "task-1" }
                val task2 = Task().apply { id = "task-2" }
                val taskList = TaskList().apply { tasks = mutableMapOf("task-1" to task1, "task-2" to task2) }
                coEvery { apiClient.getChallengeTasks("challenge-1") } returns taskList
                every { localRepository.save(any<List<Task>>()) } returns Unit
                val result = repository.retrieveChallengeTasks("challenge-1")
                result shouldBe taskList
                task1.ownerID shouldBe "challenge-1"
                task2.ownerID shouldBe "challenge-1"
                verify { localRepository.save(match<List<Task>> { it.toSet() == setOf(task1, task2) }) }
            }

            "do nothing when the API returns nothing" {
                coEvery { apiClient.getChallengeTasks("challenge-1") } returns null
                val result = repository.retrieveChallengeTasks("challenge-1")
                result shouldBe null
                verify(exactly = 0) { localRepository.save(any<List<Task>>()) }
            }
        }
        "createChallenge" should {
            "build the tasks order, create the challenge, and add a single task via the single-task endpoint" {
                val challenge = Challenge().apply { id = "challenge-1" }
                val task = Task().apply { id = "task-1"; type = TaskType.HABIT }
                coEvery { apiClient.createChallenge(challenge) } returns challenge
                coEvery { apiClient.createChallengeTask("challenge-1", task) } returns task
                every { localRepository.save(any<List<Task>>()) } returns Unit
                val result = repository.createChallenge(challenge, listOf(task))
                result shouldBe challenge
                challenge.tasksOrder?.habits shouldBe listOf("task-1")
                coVerify { apiClient.createChallengeTask("challenge-1", task) }
                coVerify(exactly = 0) { apiClient.createChallengeTasks(any(), any()) }
            }

            "create multiple tasks via the bulk endpoint" {
                val challenge = Challenge().apply { id = "challenge-1" }
                val task1 = Task().apply { id = "task-1"; type = TaskType.TODO }
                val task2 = Task().apply { id = "task-2"; type = TaskType.TODO }
                coEvery { apiClient.createChallenge(challenge) } returns challenge
                coEvery { apiClient.createChallengeTasks("challenge-1", listOf(task1, task2)) } returns listOf(task1, task2)
                every { localRepository.save(any<List<Task>>()) } returns Unit
                repository.createChallenge(challenge, listOf(task1, task2))
                coVerify { apiClient.createChallengeTasks("challenge-1", listOf(task1, task2)) }
            }

            "not add tasks when challenge creation fails" {
                val challenge = Challenge().apply { id = "challenge-1" }
                val task = Task().apply { id = "task-1"; type = TaskType.HABIT }
                coEvery { apiClient.createChallenge(challenge) } returns null
                val result = repository.createChallenge(challenge, listOf(task))
                result shouldBe null
                coVerify(exactly = 0) { apiClient.createChallengeTask(any(), any()) }
            }
        }
        "leaveChallenge" should {
            "leave remotely and mark not participating" {
                val challenge = Challenge().apply { id = "challenge-1" }
                coEvery { apiClient.leaveChallenge("challenge-1", any()) } returns null
                every { localRepository.setParticipating("user-1", "challenge-1", false) } returns Unit
                repository.leaveChallenge(challenge, "keep-all")
                verify { localRepository.setParticipating("user-1", "challenge-1", false) }
            }
        }
        "joinChallenge" should {
            "return null and not mark participating when the API returns nothing" {
                val challenge = Challenge().apply { id = "challenge-1" }
                coEvery { apiClient.joinChallenge("challenge-1") } returns null
                val result = repository.joinChallenge(challenge)
                result shouldBe null
                verify(exactly = 0) { localRepository.setParticipating(any(), any(), any()) }
            }

            "mark participating on success" {
                val challenge = Challenge().apply { id = "challenge-1" }
                val joined = Challenge().apply { id = "challenge-1" }
                coEvery { apiClient.joinChallenge("challenge-1") } returns joined
                every { localRepository.setParticipating("user-1", "challenge-1", true) } returns Unit
                val result = repository.joinChallenge(challenge)
                result shouldBe joined
                verify { localRepository.setParticipating("user-1", "challenge-1", true) }
            }
        }
        "getUserChallenges" should {
            "fall back to the current user id when none is given" {
                every { localRepository.getUserChallenges("user-1") } returns kotlinx.coroutines.flow.flowOf(listOf())
                repository.getUserChallenges(null).collect { }
                verify { localRepository.getUserChallenges("user-1") }
            }
        }
    })
